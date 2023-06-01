package com.chainmaker.jobservice.api.builder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.model.bo.job.Job;
import com.chainmaker.jobservice.api.model.bo.job.JobTemplate;
import com.chainmaker.jobservice.api.model.bo.job.task.*;
import com.chainmaker.jobservice.api.model.bo.job.task.Module;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskInputDataPo;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;
import com.chainmaker.jobservice.api.response.ParserException;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.MPCMetadata;
import com.chainmaker.jobservice.core.calcite.relnode.MPCFilter;
import com.chainmaker.jobservice.core.calcite.relnode.MPCJoin;
import com.chainmaker.jobservice.core.calcite.relnode.MPCProject;
import com.chainmaker.jobservice.core.calcite.relnode.MPCTableScan;
import com.chainmaker.jobservice.core.calcite.utils.parserWithOptimizerReturnValue;
import com.chainmaker.jobservice.core.optimizer.model.FL.FlInputData;
import com.chainmaker.jobservice.core.optimizer.model.InputData;
import com.chainmaker.jobservice.core.optimizer.model.OutputData;
import com.chainmaker.jobservice.core.optimizer.model.TeeModel;
import com.chainmaker.jobservice.core.optimizer.nodes.DAG;
import com.chainmaker.jobservice.core.optimizer.plans.*;
import com.chainmaker.jobservice.core.parser.plans.FederatedLearning;
import com.chainmaker.jobservice.core.parser.plans.LogicalHint;
import com.chainmaker.jobservice.core.parser.plans.LogicalPlan;
import com.chainmaker.jobservice.core.parser.plans.LogicalProject;
import com.chainmaker.jobservice.core.parser.tree.*;
import com.google.gson.Gson;
import io.lettuce.core.output.StatusOutput;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.volcano.RelSubset;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlExplainFormat;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.commons.lang3.SerializationUtils;
import org.chainmaker.pb.tee.EnclaveOutcall;
import org.springframework.beans.BeanUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.chainmaker.jobservice.core.calcite.utils.ConstExprJudgement.isNumeric;

public class JobBuilderWithOptimizer extends PhysicalPlanVisitor{
    private enum JobType {
        FQ, FQS, FL, FLS, CC, CCS
    }
    private enum TaskType {
        QUERY, LOCALFILTER, LOCALJOIN, OTPSI, RSAPSI, TEEPSI, AGG, MPCEXP, FL, TEE, MERGE, LOCALEXP, LOCALAGG
    }

    private class TaskNode {
        int taskID;
        boolean isMerged;
        HashSet<String> partyIds;
        List<TaskNode> inputs;
        List<TaskNode> fathers;

        TaskNode (int id) {
            taskID = id;
            isMerged = false;
            partyIds = new HashSet<>();
            inputs = new ArrayList<>();
            fathers = new ArrayList<>();
        }

        public boolean isLocal() {
            if (tasks.get(taskID).getModule().getModuleName().equals(TaskType.FL.name()) ||
                    tasks.get(taskID).getModule().getModuleName().equals(TaskType.TEE.name())) {
                return false;
            }
            return partyIds.size() == 1;
        }
    }

    private final Integer modelType;
    private final Integer isStream;
    private final RelNode phyPlan;
    private final String createTime, jobID;
    private final MPCMetadata metadata;
    private int cnt;
    private Job job = new Job();
    private List<ServiceVo> services = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private List<Task> mergedTasks = new ArrayList<>();
    private HashSet<String> jobParties = new HashSet<>();
    private LogicalPlan OriginPlan;
    private LogicalHint hint;

    public JobBuilderWithOptimizer(Integer modelType, Integer isStream, parserWithOptimizerReturnValue value) {
        this.modelType = modelType;
        this.isStream = isStream;
        this.phyPlan = value.getPhyPlan();
        this.OriginPlan = value.getOriginPlan();
        this.createTime = String.valueOf(System.currentTimeMillis());
        this.jobID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        this.metadata = MPCMetadata.getInstance();
        if (OriginPlan instanceof LogicalHint) {
            hint = (LogicalHint) OriginPlan;
            OriginPlan = (LogicalPlan) OriginPlan.getChildren().get(0);
        } else {
            hint = null;
        }
    }

    public JobTemplate getJobTemplate() {
        JobTemplate jobTemplate = new JobTemplate();
        jobTemplate.setJob(job);
        jobTemplate.setServices(services);
        jobTemplate.setTasks(tasks);
//        jobTemplate.setTasks(mergedTasks);
        return jobTemplate;
    }

    public void build() {
        String jobStatus = "WAITING";
        String taskDAG = "taskDAG";
        String jobType = "";
        if (modelType == 0 && isStream == 0) {
            jobType = JobType.FQ.name();
        } else if (modelType == 0 && isStream == 1) {
            jobType = JobType.FQS.name();
        } else if (modelType == 1 && isStream == 0) {
            jobType = JobType.FL.name();
        } else if (modelType == 1 && isStream == 1) {
            jobType = JobType.FLS.name();
        } else if (modelType == 2 && isStream == 0) {
            jobType = JobType.CC.name();
        } else if (modelType == 2 && isStream == 1) {
            jobType = JobType.CCS.name();
        } else {
            throw new ParserException("暂不支持的任务类型");
        }

        tasks.addAll(PhyPlan2Task());

        generateFLTasks(OriginPlan);

        mergeLocalTasks();

        job.setJobID(jobID);
        job.setJobType(jobType);
        job.setStatus(jobStatus);
        job.setCreateTime(createTime);
        job.setUpdateTime(createTime);
        job.setTasksDAG(taskDAG);
        job.setParties(new ArrayList<>(jobParties));
    }

    /**
     * 将LocalJoin中output数量超过一个的都改成一个，并修改上位依赖
     * @param root
     */
    public void LocalJoincorrect(TaskNode root) {
        Queue<TaskNode> q = new ArrayDeque<>();
        for (TaskNode node : root.inputs) {
            q.add(node);
        }
        List<Boolean> vis = new ArrayList<>(tasks.size());
        for (int i = 0; i < tasks.size(); i++) {
            vis.add(false);
        }
        while (!q.isEmpty()) {
            TaskNode tn = q.remove();
            if (vis.get(tn.taskID)) {
                continue;
            }
            vis.set(tn.taskID, true);
            Task t = tasks.get(tn.taskID);
            for (int i = 0; i < tn.inputs.size(); i++) {
                q.add(tn.inputs.get(i));
            }
            if (t.getModule().getModuleName().equals(TaskType.LOCALJOIN.name()) && tn.partyIds.size() == 1) {
                // 记录旧的outputName
                HashSet<String> outputNames = new HashSet<>();
                int n = t.getOutput().getData().size();
                for (int i = 0; i < n; i++) {
                    outputNames.add(t.getOutput().getData().get(i).getDataName());
                }
                // 删除多余的output，修改outputName
                for (int i = 1; i < n; i++) {
                    t.getOutput().getData().remove(i);
                }
                String outName = "LOCAL-" + t.getTaskName();
                t.getOutput().getData().get(0).setDataName(outName);

                // 修改上位依赖项
                for (int i = 0; i < tn.fathers.size(); i++) {
                    Task ft = tasks.get(tn.fathers.get(i).taskID);
                    for (TaskInputData inputData : ft.getInput().getData()) {
                        String inputName = inputData.getDataName();
                        if (outputNames.contains(inputName)) {
                            inputData.setDataName(outName);
                        }
                    }
                }
            }
        }
    }

    /**
     * 合并本地Task，将它们批量交给spark进行处理
     */
    public void mergeLocalTasks() {
        // 1、构建Task调用关系树
        TaskNode root = buildTaskTree();
        // 2、修正LocalJoin的output数量
        LocalJoincorrect(root);
        // 3、遍历该树，对LocalTask节点进行merge
        dfsTaskTree(root);
        // 4、排序mergedTasks
        mergedTasks.sort(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return Integer.parseInt(o1.getTaskName()) - Integer.parseInt(o2.getTaskName());
            }
        });
    }

    /**
     * 具体merge操作的实现
     * @param root
     * @return
     */
    public Task mergeTaskTree(TaskNode root) {
        Task ori = tasks.get(root.taskID);
        Gson gson = new Gson();
        Task ans = gson.fromJson(gson.toJson(ori), Task.class);
//        TaskCp(ori, ans);
        String sql = "select ProjectString from TableJoinString where PredicateString";
        String ProjectString = "", TableJoinString = "", PredicateString = "";
        HashSet<String> inputTables = new HashSet<>();
        Queue<TaskNode> q = new ArrayDeque<>();
        q.add(root);
        List<Boolean> vis = new ArrayList<>(root.taskID+1);
        for (int i = 0; i <= root.taskID; i++) {
            vis.add(false);
        }
        while (!q.isEmpty()) {
            TaskNode tn = q.remove();
            if (vis.get(tn.taskID)) {
                continue;
            }
            vis.set(tn.taskID, true);
            Task t = tasks.get(tn.taskID);
            for (int i = 0; i < tn.inputs.size(); i++) {
                q.add(tn.inputs.get(i));
            }
            switch (t.getModule().getModuleName()) {
                case "LOCALFILTER": {
                    String op = t.getModule().getParams().getString("operator");
                    String constant = t.getModule().getParams().getString("constant");
                    String table = t.getInput().getData().get(0).getParams().getString("table");
                    String field = t.getInput().getData().get(0).getParams().getString("field");
                    inputTables.add(table);
                    String predicate = table + "." + field + op + constant;
                    if (PredicateString.equals("")) {
                        PredicateString += predicate;
                    } else {
                        PredicateString += " and " + predicate;
                    }
                    break;
                }
                case "LOCALJOIN": {
                    String joinType = t.getModule().getParams().getString("joinType");
                    String joinOp = t.getModule().getParams().getString("operator");
                    String leftTable = t.getInput().getData().get(0).getParams().getString("table");
                    String leftField = t.getInput().getData().get(0).getParams().getString("field");
                    String rightTable = t.getInput().getData().get(1).getParams().getString("table");
                    String rightField = t.getInput().getData().get(1).getParams().getString("field");
                    String joinCond = leftTable + "." + leftField + joinOp + rightTable + "." + rightField;
                    inputTables.add(leftTable);
                    inputTables.add(rightTable);
                    if (TableJoinString.equals("")) {
                        TableJoinString += "(" + leftTable + " join " + rightTable + " on " + joinCond + ")";
                    } else {
                        // 还需要考虑四个Table，先两两Join，再Join的情况，暂时没有支持
                        TableJoinString = "(" + TableJoinString;
                        boolean isLeftLocalJoin = tasks.get(Integer.parseInt(t.getInput().getData().get(0).getTaskSrc())).getModule().getModuleName().equals(TaskType.LOCALJOIN.name());
                        if (isLeftLocalJoin) {
                            TableJoinString += " join " + rightTable + " on " + joinCond + ")";
                        } else {
                            TableJoinString += " join " + leftTable + " on " + joinCond + ")";
                        }
                    }
                    break;
                }
                case "QUERY": {
                    String table = t.getInput().getData().get(0).getParams().getString("table");
                    String field = t.getInput().getData().get(0).getParams().getString("field");
                    String proj = table + "." + field;
                    inputTables.add(table);
                    if (ProjectString.equals("")) {
                        ProjectString += proj;
                    } else {
                        ProjectString += ", " + proj;
                    }
                    break;
                }
                case "LOCALEXP": {
                    String exp = t.getModule().getParams().getString("expression");
                    String proj = "";
                    int pos = 0;
                    int xnum = 0;
                    for (int i = 0; i < exp.length(); i++) {
                        if (exp.charAt(i) == 'x') {
                            xnum++;
                        }
                    }
                    int xpos = exp.indexOf('x', 0);
                    List<TaskInputData> list = t.getInput().getData();
                    for (int i = 0; i < xnum; i++) {
                        List<Integer> index = (List<Integer>) list.get(i).getParams().get("index");
                        if (!index.contains(i)) {
                            continue;
                        }
                        while (pos < xpos) {
                            proj += exp.charAt(pos);
                            pos++;
                        }
                        pos++;
                        String table = list.get(i).getParams().getString("table");
                        String field = list.get(i).getParams().getString("field");
                        proj += table + "." + field;
                        xpos = exp.indexOf('x', xpos+1);
                    }
                    if (ProjectString.equals("")) {
                        ProjectString += proj;
                    } else {
                        ProjectString += ", " + proj;
                    }
                    break;
                }
                case "LOCALAGG": {
                    String exp = t.getModule().getParams().getString("expression");
                    String proj = "";
                    int pos = 0;
                    int xnum = 0;
                    for (int i = 0; i < exp.length(); i++) {
                        if (exp.charAt(i) == 'x') {
                            xnum++;
                        }
                    }
                    int xpos = exp.indexOf('x', 0);
                    List<TaskInputData> list = t.getInput().getData();
                    for (int i = 0; i < xnum; i++) {
                        List<Integer> index = (List<Integer>) list.get(i).getParams().get("index");
                        if (!index.contains(i)) {
                            continue;
                        }
                        while (pos < xpos) {
                            proj += exp.charAt(pos);
                            pos++;
                        }
                        pos++;
                        String table = list.get(i).getParams().getString("table");
                        String field = list.get(i).getParams().getString("field");
                        proj += table + "." + field;
                        xpos = exp.indexOf('x', xpos+1);
                    }
                    proj = t.getModule().getParams().getString("function") + "(" + proj + ")";
                    if (ProjectString.equals("")) {
                        ProjectString += proj;
                    } else {
                        ProjectString += ", " + proj;
                    }
                    break;
                }
                default:

                    break;
            }
        }

        // 重新填写input内容
        List<TaskInputData> inputDataList = new ArrayList<>();
        for (String tbName : inputTables) {
            TaskInputData inputData = new TaskInputData();
            inputData.setDataName(tbName);
            inputDataList.add(inputData);
        }
        ans.getInput().setData(inputDataList);

        // 反向生成sql并填写相关module信息
        ans.getModule().setModuleName(TaskType.MERGE.name());
        ans.getModule().getParams().clear();
        if (ProjectString.equals("")) {
            ProjectString = "*";
        }
        if (PredicateString.equals("")) {
            sql = "select " + ProjectString + " from " + TableJoinString;
        } else {
            sql = "select " + ProjectString + " from " + TableJoinString + " where " + PredicateString;
        }
        ans.getModule().getParams().put("sql", sql);
        return ans;
    }

    /**
     * 深拷贝一个Task，最好是调用深拷贝方法或者实现clone，这是暂时解决方法
     * @param source
     * @param target
     */
    private void TaskCp(Task source, Task target) {
        target.setVersion(source.getVersion());
        target.setJobID(source.getJobID());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        target.setStatus(source.getStatus());
        target.setTaskName(source.getTaskName());
        target.setParties(source.getParties());
        target.setModule(new Module());
        target.getModule().setParams(new JSONObject());
        target.setInput(new Input());
        target.setOutput(new Output());
        target.getOutput().setData(source.getOutput().getData());
    }

    /**
     * 遍历Task调用关系树，对Local节点进行merge
     * @param root
     */
    public void dfsTaskTree(TaskNode root) {
        if (root.isMerged) {
            return;
        }
        root.isMerged = true;
        if (root.taskID != -1 && root.isLocal() && root.inputs.size() > 0) {
            System.out.println("mergedTask ID = " + root.taskID);
            mergedTasks.add(mergeTaskTree(root));
            return;
        }
        if (root.taskID != -1) {
            System.out.println("notMergedTask ID = " + root.taskID);
            mergedTasks.add(tasks.get(root.taskID));
        }
        for (int i = 0; i < root.inputs.size(); i++) {
            dfsTaskTree(root.inputs.get(i));
        }
    }

    /**
     * 构建Task调用关系树
     * @return
     */
    public TaskNode buildTaskTree() {
        TaskNode root = new TaskNode(-1);
        List<TaskNode> nodes = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            TaskNode node = new TaskNode(i);
            List<TaskInputData> list = t.getInput().getData();
            // 通过input构建父子节点之间的联系
            for (TaskInputData inputData : list) {
                if (!inputData.getTaskSrc().equals("")) {
                    int idx = Integer.parseInt(inputData.getTaskSrc());
                    node.inputs.add(nodes.get(idx));
                    nodes.get(idx).fathers.add(node);
                    node.partyIds.addAll(nodes.get(idx).partyIds);
                }
            }
            // merge parties
            for (Party p : t.getParties()) {
                node.partyIds.add(p.getPartyID());
            }
            nodes.add(node);
            if (t.getOutput().getData().get(0).getFinalResult().equals("Y")) {
                root.inputs.add(node);
            }
        }
        return root;
    }

    /**
     * 生成FL和TEE相关的Task
     * @param node
     */
    public void generateFLTasks(LogicalPlan node) {
        if (node instanceof FederatedLearning) {
            tasks.add(parseFederatedLearning((FederatedLearning) node));
            /*
                    fl=[A.B.C.IS_TRAIN=TRUE, IS_TEST=FALSE],
                    labels=[
                        [SOURCE_DATA=ADATA, WITH_LABEL=TRUE, LABEL_TYPE=INT,
                            OUTPUT_FORMAT=DENSE, NAMESPACE=EXPERIMENT],
                        [SOURCE_DATA=BDATA, WITH_LABEL=FALSE, OUTPUT_FORMAT=DENSE,
                            NAMESPACE=EXPERIMENT]],

                    psi=[INTERSECT_METHOD=RSA],
                    feat=[],
                    model=[model_name=HELR, PENALTY=L2,
                        TOL=0.0001, ALPHA=0.01, OPTIMIZER=RMSPROP, BATCH_SIZE=-1, LEARNING_RATE=0.15,
                        INIT_PARAM.INIT_METHOD=ZEROS, INIT_PARAM.FIT_INTERCEPT=TRUE, MAX_ITER=1,
                        EARLY_STOP=DIFF, ENCRYPT_PARAM.KEY_LENGTH=1024, REVEAL_STRATEGY=RESPECTIVELY,
                        REVEAL_EVERY_ITER=TRUE],
                    eval=[EVAL_TYPE=BINARY]}
             */
        } else if (node instanceof LogicalProject){
            /*
                "select adata.a1, testt(adata.a1, bdata.b1) from adata, bdata";
             */
            List<NamedExpression> namedExpressionList = ((LogicalProject) node).getProjectList().getValues();
            for (NamedExpression ne : namedExpressionList) {
                Expression expr = ne.getExpression();
                if (modelType == 2 && expr instanceof FunctionCallExpression) {
                    // TESTT[ADATA.A1, BDATA.B1]
                    tasks.add(parseTEE((FunctionCallExpression) expr));
                }
            }
        } else {
            ;
        }
    }

    public Task parseTEE(FunctionCallExpression expr) {
        // basic info
        Task task = basicTask(String.valueOf(cnt));

        // module
        Module module = new Module();
        module.setModuleName(TaskType.TEE.name());
        JSONObject moduleParams = new JSONObject(true);
        moduleParams.put("methodName", expr.getFunction().toString());
        moduleParams.put("domainID", "");
        module.setParams(moduleParams);
        task.setModule(module);

        // input
        Input input = new Input();
        List<TaskInputData> inputDataList = new ArrayList<>();

        for (Expression e : expr.getExpressions()) {
            TaskInputData data = new TaskInputData();
            String table = e.toString().split("\\.")[0];
            String field = e.toString().split("\\.")[1];
            data.setDataName(table);
            data.setDataID(data.getDataName());
            data.setDomainID(metadata.getTableOrgId(table));
            data.setRole("client");
            data.setTaskSrc("");
            JSONObject dataParams = new JSONObject(true);
            dataParams.put("table", table);
            dataParams.put("field", field);
            data.setParams(dataParams);
            inputDataList.add(data);
        }
        input.setData(inputDataList);
        task.setInput(input);

        // output
        Output output = new Output();
        List<TaskOutputData> outputDataList = new ArrayList<>();
        TaskOutputData outputData = new TaskOutputData();
        outputData.setDataID("");
        outputData.setDataName(expr.getFunction() + "-" + cnt);
        outputData.setDomainID("");
        outputData.setFinalResult("Y");
        outputDataList.add(outputData);
        output.setData(outputDataList);
        task.setOutput(output);

        // party
        List<Party> parties = new ArrayList<>();
        HashSet<String> partySet = new HashSet<>();
        for (TaskInputData inputData : inputDataList) {
            partySet.add(inputData.getDomainID());
        }
        for (String value : partySet) {
            Party party = new Party();
            party.setServerInfo(null);
            party.setStatus(null);
            party.setTimestamp(null);
            party.setPartyID(value);
            parties.add(party);
        }
        task.setParties(parties);
        for (Party p : parties) {
            jobParties.add(p.getPartyID());
        }

        return task;
    }

    public Task parseFederatedLearning(FederatedLearning node) {
        // basic info
        FederatedLearningExpression expression = node.getParamsList();
        Task task = basicTask(String.valueOf(cnt));

        // module
        Module module = new Module();
        module.setModuleName(TaskType.FL.name());
        JSONObject moduleParams = new JSONObject(true);
        moduleParams.put("intersection", parseFLParams(expression.getPsi()));
        moduleParams.put("fl", parseFLParams(expression.getFl()));
        moduleParams.put("model", parseFLParams(expression.getModel()));
        moduleParams.put("eval", parseFLParams(expression.getEval()));
        module.setParams(moduleParams);
        task.setModule(module);

        // input
        Input input = new Input();
        List<TaskInputData> inputDataList = new ArrayList<>();
        List<List<FlExpression>> labels = expression.getLabels();
        for (int i = 0; i < labels.size(); i++) {
            inputDataList.add(parseFLLabel(labels.get(i)));
        }
        if (inputDataList.get(0).getRole().equals(inputDataList.get(1).getRole())) {
            if (inputDataList.get(0).getRole().equals("guest")) {
                inputDataList.get(0).setRole("host");
            } else {
                inputDataList.get(0).setRole("guest");
            }
        }
        input.setData(inputDataList);
        task.setInput(input);

        // output
        Output output = new Output();
        List<TaskOutputData> outputDataList = new ArrayList<>();
        TaskOutputData outputData = new TaskOutputData();
        outputData.setDataID("");
        outputData.setDataName("fl-" + cnt);
        outputData.setDomainID(inputDataList.get(0).getDomainID());
        outputData.setFinalResult("Y");
        outputDataList.add(outputData);
        output.setData(outputDataList);
        task.setOutput(output);

        // party
        List<Party> parties = new ArrayList<>();
        HashSet<String> partySet = new HashSet<>();
        for (TaskInputData inputData : inputDataList) {
            partySet.add(inputData.getDomainID());
        }
        for (String value : partySet) {
            Party party = new Party();
            party.setServerInfo(null);
            party.setStatus(null);
            party.setTimestamp(null);
            party.setPartyID(value);
            parties.add(party);
        }
        task.setParties(parties);

        for (Party p : parties) {
            jobParties.add(p.getPartyID());
        }

        return task;
    }

    public TaskInputData parseFLLabel(List<FlExpression> label) {
        String[] constLabels = {"output_format", "namespace", "label_type", "with_label", "table"};
        TaskInputData inputData = new TaskInputData();
        String dataName = "";
        for (int i = 0; i < label.size(); i++) {
            FlExpression expr = label.get(i);
            if (expr.getLeft().toString().equals("SOURCE_DATA")) {
                dataName = expr.getRight().toString();
                label.remove(i);
                break;
            }
        }
        inputData.setDataName(dataName);
        inputData.setDataID(dataName);
        inputData.setDomainID(metadata.getTableOrgId(dataName));
        JSONObject params = parseFLParams(label);
        for (String l : constLabels) {
            if (!params.containsKey(l)) {
                params.put(l, null);
            }
        }
        params.put("table", dataName);
        inputData.setParams(params);
        inputData.setTaskSrc("");
        if (inputData.getParams().get("with_label").toString().equals("true")) {
            inputData.setRole("guest");
        } else {
            inputData.setRole("host");
        }
        return inputData;
    }

    public JSONObject parseFLParams(List<FlExpression> params) {
        JSONObject object = new JSONObject();
        for (int i = 0; i < params.size(); i++) {
            FlExpression expression = params.get(i);
            String key = expression.getLeft().toString().toLowerCase();
            String value = expression.getRight().toString().toLowerCase();
            if (!key.contains(".")) {
                object.put(key, value);
            } else {
//                object.put(key, value);
                parseSubParams(key, value, object);
            }
        }
        return object;
    }

    /**
     * 处理如 a.b.c.is_tran=true 此类带点param，需要生成一系列JsonObject
     * @param key
     * @param value
     * @param object
     */
    private void parseSubParams(String key, String value, JSONObject object) {
        if (key.contains(".")) {
            int pos = key.indexOf(".");
            String subName = key.substring(0, pos);
            // 如果已经存在该JsonObject
            if (object.containsKey(subName)) {
                JSONObject subJson = object.getObject(subName, JSONObject.class);
                parseSubParams(key.substring(pos+1), value, subJson);
            } else {
                // 如果没有，则新加入一个
                JSONObject subJson = new JSONObject(true);
                parseSubParams(key.substring(pos+1), value, subJson);
                object.put(subName, subJson);
            }
        } else {
            object.put(key, value);
        }
    }

    /**
     * 将物理计划转化成Task列表
     */
    public List<Task> PhyPlan2Task() {
//        System.out.println(
//                RelOptUtil.dumpPlan("[Physical plan] TEXT", phyPlan, SqlExplainFormat.TEXT,
//                        SqlExplainLevel.ALL_ATTRIBUTES));
        String str = tidyPhyPlan(phyPlan, 0);
        System.out.println(str);
        Stack<String> stk = new Stack<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                stk.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generateTasks(stk);
    }

    /**
     * 具体分布转化的实现
     * @param stk
     * @return
     */
    public List<Task> generateTasks(Stack<String> stk) {
        List<Task> tasks = new ArrayList<>();

        Stack<List<String>> operands = new Stack<>();
        while (!stk.isEmpty()) {
            String s = stk.pop().trim();
//            System.out.println("current str = " + s);
            List<String> params = List.of(s.split("\\[|]"));
            if (s.startsWith("MPCTableScan")) {
                // MPCTableScan [TC], 直接加入操作栈
                List<String> tslist = new ArrayList<>();
                tslist.add(params.get(1));
                operands.add(tslist);
            } else if (s.startsWith("MPCJoin")) {
                // MPCJoin [JoinType[INNER], JoinCond[=($0, $2)], $ from [TA.ID, TA.AGE, TC.ID]]
                // 默认都是两两PSI，所以取两个操作数
                List<String> lefts = operands.pop();
                List<String> rights = operands.pop();

                // 完成string到Task的转换
                Task PSITask = str2Task(s, cnt, "PSI", lefts, rights, false);
                tasks.add(PSITask);

                // 该task的输出作为下一个的输入加入操作数栈
                Output PSIOutput = PSITask.getOutput();
                List<String> outputDataNames = new ArrayList<>();
                for (TaskOutputData outputData : PSIOutput.getData()) {
                    outputDataNames.add(outputData.getDataName());
                }
                operands.add(outputDataNames);
                cnt++;
            } else if (s.startsWith("MPCFilter")) {
                // MPCFilter [FilterCond[AND(>($0, 10), <($1, 50))] $ from [TA.ID, TA.AGE]]
                // 需求只有AND或者单一Cond两种情况，如后续添加OR，可再增加
                if (params.get(2).startsWith("AND")) {
                    // 如果是多个Cond的AND情况，则把它们分为多个单独的Cond加入Task栈
                    String[] conds = params.get(2).substring(4, params.get(2).length() - 1).split("\\),");
                    conds[0] += ")";
                    for (int i = 0; i < conds.length; i++) {
                        conds[i] = conds[i].trim();
                        String newFilter = s.substring(0, s.indexOf("AND"));
                        newFilter += conds[i] + "] ";
                        newFilter += s.substring(s.lastIndexOf("$"));
//                        System.out.println(newFilter);
                        stk.add(newFilter);
                    }
                } else {
                    // 如果是单独Cond的情况，则直接生成Task
                    // MPCFilter [FilterCond[>($0, 10)] $ from [TA.ID, TA.AGE]]
                    List<String> childTaskNames = operands.pop();
                    Task FilterTask;
                    String cond = params.get(2);
                    if (cond.substring(0, cond.indexOf(",")).contains("$") && cond.substring(cond.indexOf(",")).contains("$")) {
                        FilterTask = str2Task(s, cnt, "VariableFilter", childTaskNames, null, false);
                    } else {
                        FilterTask = str2Task(s, cnt, "ConstantFilter", childTaskNames, null, false);
                    }
                    tasks.add(FilterTask);
                    Output FilterOutput = FilterTask.getOutput();
                    List<String> outputDataNames = new ArrayList<>();
                    outputDataNames.add(FilterOutput.getData().get(0).getDataName());
                    operands.add(outputDataNames);
                    cnt++;
                }
            } else if (s.startsWith("MPCProject")) {
                // MPCProject [TA.ID, SUM[TA.ID-TA.AGE]]
                // Project分为普通QUERY和MPC两种
                String tmp = s.substring(s.indexOf("[")+1, s.length()-1);
                if (tmp.equals("")) {
                    // 如果project是空的，即无法处理的FL和TEE已经被提出，无需进行proj处理
                    continue;
                }
                if (tmp.contains(",")) {
                    // 如果是复合Project，则需要转化成多次单独Project
                    String[] projs = tmp.split(",");
                    for (String proj : projs) {
                        proj = proj.trim();
                        String newProject = s.substring(0, s.indexOf("[")+1);
                        newProject += proj + "] ";
                        stk.add(newProject);
                    }
                } else {
                    // 已经是单独的Project
                    List<String> childNames = operands.pop();
                    Task ProjectTask;

                    if (s.contains("MAX") || s.contains("MIN") || s.contains("COUNT") ||
                            s.contains("AVG") || s.contains("SUM") || s.contains("+") || s.contains("-") || s.contains("*") || s.contains("/")) {
                        ProjectTask = str2Task(s, cnt, "MPC", childNames, null, true);
                    } else {
                        ProjectTask = str2Task(s, cnt, "QUERY", childNames, null, true);
                    }
                    tasks.add(ProjectTask);
//                    Output ProjectOutput = ProjectTask.getOutput();
//                    String outputDataName = ProjectOutput.getData().get(0).getDataName();
                    operands.add(childNames);
                    cnt++;
                }
            } else {
                ;
            }
        }
        return tasks;
    }

    /**
     * 将字符串转换成Task
     * @param str
     * @return
     */
    public Task str2Task(String str, int curTaskName, String moduleName, List<String> leftDataName, List<String> rightDataName, boolean isFinalResult) {
        // 生成具有基本信息的Task对象
        Task task = basicTask(String.valueOf(curTaskName));
        List<String> params = List.of(str.split("\\[|]"));

        // module信息（即进行什么操作）
        Module module = new Module();
        module.setModuleName(moduleName);
        JSONObject moduleparams = new JSONObject(true);
        switch (moduleName) {
            case "PSI": {
                String joinType = params.get(2);
                moduleparams.put("joinType", joinType);
                String joinOp = params.get(4).substring(0, 1);
                moduleparams.put("operator", joinOp);
                break;
            }
            case "ConstantFilter": {
                String cond = params.get(2);
                moduleparams.put("operator", cond.substring(0, cond.indexOf("(")));
                String[] sp = cond.split("\\(|,|\\)");
                for (String s : sp) {
                    s = s.trim();
                    if (s.charAt(0) >= '0' && s.charAt(0) <= '9') {
                        moduleparams.put("constant", s);
                        break;
                    }
                }
                break;
            }
            case "VariableFilter":
            case "QUERY": {

                break;
            }
            case "MPC": {
                String MPCProj = str.substring(str.indexOf("[") + 1, str.length() - 1);
                String MPCExpr = MPCProj;
                if (MPCProj.contains("[")) {
                    // 说明是 COUNT(ADATA.A1)这种，包含聚合函数
                    String func = MPCProj.substring(0, MPCProj.indexOf("["));
                    moduleparams.put("function", func);
                    MPCExpr = MPCProj.substring(MPCProj.indexOf("["), MPCProj.length()-1);
                } else {
                    // 说明是 ADATA.A1+BDATA.B1这种，不包含聚合函数，function置为base
                    moduleparams.put("function", "base");
                }
                String expression = "";
                int chNum = 0;
                for (int i = 0; i < MPCExpr.length(); i++) {
                    char ch = MPCExpr.charAt(i);
                    if (ch == '+' || ch == '-' || ch == '*' || ch == '/' ||
                            ch == '(' || ch == ')') {
                        if (chNum != 0) {
                            expression += "x";
                            chNum = 0;
                        }
                        expression += ch;
                    } else {
                        chNum++;
                    }
                }
                if (chNum != 0) {
                    expression += "x";
                }
                moduleparams.put("expression", expression);
                break;
            }// 不需要额外的params
            default:
                break;
        }
        module.setParams(moduleparams);
        task.setModule(module);

        // 输入信息
        Input input = new Input();
        List<TaskInputData> inputDatas = new ArrayList<>();

        switch (moduleName) {
            case "PSI": {
                TaskInputData inputdata1 = new TaskInputData(), inputdata2 = new TaskInputData();
                JSONObject inputData1Params = new JSONObject(true);
                JSONObject inputData2Params = new JSONObject(true);

                List<String> fields = List.of(params.get(6).split(","));

                // 如果是无条件求交
                if (params.get(4).equals("true")) {
//                    if (leftDataName != null) {
//                        inputdata1.setDataName(leftDataName);
//                        inputdata1.setTaskSrc(leftDataName.split("-")[1]);
//                    } else {
//                        inputdata1.setDataName(fields.get(0).split("\\.")[0].trim());
//                        inputdata1.setTaskSrc("0");
//                    }
//                    if (rightDataName != null) {
//                        inputdata2.setDataName(rightDataName);
//                        inputdata2.setTaskSrc(rightDataName.split("-")[1]);
//                    } else {
//                        inputdata2.setDataName(fields.get(fields.size()-1).split("\\.")[0].trim());
//                        inputdata2.setTaskSrc("0");
//                    }
//
//                    if (!inputdata1.getDataName().contains("-")) {
//                        inputdata1.setDataID(inputdata1.getDataName());
//                    }
//                    if (!inputdata2.getDataName().contains("-")) {
//                        inputdata2.setDataID(inputdata2.getDataName());
//                    }

                } else {
                    // 正常求交 on TA.id=TB.id
                    // Join左侧
                    String index = params.get(4).substring(params.get(4).indexOf("$") + 1, params.get(4).indexOf(","));
                    String tableField = fields.get(Integer.parseInt(index)).trim();
                    String leftTable = tableField.split("\\.")[0];
                    String leftField = tableField.split("\\.")[1];

                    for (String dataName : leftDataName) {
                        if (dataName.startsWith(leftTable)) {
                            inputdata1.setDataName(dataName);
                            if (dataName.contains("-")) {
                                inputdata1.setTaskSrc(dataName.split("-")[1]);
                                inputdata1.setDataID("");
                            } else {
                                inputdata1.setTaskSrc("");
                                inputdata1.setDataID(inputdata1.getDataName());
                            }
                            break;
                        }
                    }
                    if (inputdata1.getDataName() == null) {
                        inputdata1.setDataName(leftTable);
                        inputdata1.setTaskSrc("");
                        inputdata1.setDataID(inputdata1.getDataName());
                    }

                    inputData1Params.put("table", leftTable);
                    inputData1Params.put("field", leftField);

                    // Join右侧
                    index = params.get(4).substring(params.get(4).lastIndexOf("$") + 1, params.get(4).indexOf(")"));
                    tableField = fields.get(Integer.parseInt(index)).trim();
                    String rightTable = tableField.split("\\.")[0];
                    String rightField = tableField.split("\\.")[1];

                    for (String dataName : rightDataName) {
                        if (dataName.startsWith(rightTable)) {
                            inputdata2.setDataName(dataName);
                            if (dataName.contains("-")) {
                                inputdata2.setTaskSrc(dataName.split("-")[1]);
                                inputdata2.setDataID("");
                            } else {
                                inputdata2.setTaskSrc("");
                                inputdata2.setDataID(inputdata2.getDataName());
                            }
                            break;
                        }
                    }
                    if (inputdata1.getDataName() == null) {
                        inputdata2.setDataName(rightTable);
                        inputdata2.setTaskSrc("");
                        inputdata2.setDataID(inputdata2.getDataName());
                    }


                    inputData2Params.put("table", rightTable);
                    inputData2Params.put("field", rightField);
                }

                inputdata1.setDomainID(getFieldDomainID(inputData1Params.get("table") + "." + inputData1Params.get("field")));
                inputdata2.setDomainID(getFieldDomainID(inputData2Params.get("table") + "." + inputData2Params.get("field")));
                inputdata1.setRole("client");
                inputdata2.setRole("server");
                inputdata1.setParams(inputData1Params);
                inputdata2.setParams(inputData2Params);
                inputDatas.add(inputdata1);
                inputDatas.add(inputdata2);
                break;
            }
            case "ConstantFilter": {
                String cond = params.get(2);
                String[] sp = cond.split("\\(|,|\\)");
                int index = 0;
                for (String s : sp) {
                    s = s.trim();
                    if (s.startsWith("$")) {
                        index = Integer.parseInt(s.substring(1));
                        break;
                    }
                }
                TaskInputData inputdata = new TaskInputData();
                String tableField = params.get(4).split(",")[index].trim();
                String table = tableField.split("\\.")[0];
                String field = tableField.split("\\.")[1];
                for (String dataName : leftDataName) {
                    if (dataName.startsWith(table)) {
                        inputdata.setDataName(dataName);
                        if (dataName.contains("-")) {
                            inputdata.setTaskSrc(dataName.split("-")[1]);
                            inputdata.setDataID("");
                        } else {
                            inputdata.setTaskSrc("");
                            inputdata.setDataID(inputdata.getDataName());
                        }
                        break;
                    }
                }
                if (inputdata.getDataName() == null) {
                    inputdata.setDataName(table);
                    inputdata.setTaskSrc("");
                    inputdata.setDataID(inputdata.getDataName());
                }
                JSONObject jsonObjectParams = new JSONObject(true);
                jsonObjectParams.put("table", table);
                jsonObjectParams.put("field", field);


                inputdata.setDomainID(getFieldDomainID(jsonObjectParams.get("table") + "." + jsonObjectParams.get("field")));
                inputdata.setRole("server");
                inputdata.setParams(jsonObjectParams);
                inputDatas.add(inputdata);
                break;
            }
            case "VariableFilter": {

                break;
            }
            case "MPC": {
                String MPCProj = str.substring(str.indexOf("[") + 1, str.length() - 1);
                String[] fields;
                if (MPCProj.contains("[")) {
                    fields = MPCProj.substring(MPCProj.indexOf("[") + 1, MPCProj.length() - 1).split("\\+|-|\\*|/|\\(|\\)");
                } else {
                    fields = MPCProj.split("\\+|-|\\*|/|\\(|\\)");
                }
                HashMap<String, Integer> idxMap = new HashMap<>();
                int cnt = 0;
                for (int i = 0; i < fields.length; i++) {
                    String tableField = fields[i];
                    if (tableField.length() == 0) {
                        continue;
                    }
                    if (idxMap.containsKey(tableField)) {
                        int pos = idxMap.get(tableField);
                        List<Integer> list = (List<Integer>) inputDatas.get(pos).getParams().get("index");
                        list.add(i);
                        continue;
                    } else {
                        idxMap.put(tableField, cnt++);
                    }
                    String table = tableField.split("\\.")[0];
                    String field = tableField.split("\\.")[1];
                    TaskInputData inputdata = new TaskInputData();
                    for (String dataName : leftDataName) {
                        if (dataName.startsWith(table)) {
                            inputdata.setDataName(dataName);
                            if (dataName.contains("-")) {
                                inputdata.setTaskSrc(dataName.split("-")[1]);
                                inputdata.setDataID("");
                            } else {
                                inputdata.setTaskSrc("");
                                inputdata.setDataID(inputdata.getDataName());
                            }
                            break;
                        }
                    }
                    if (inputdata.getDataName() == null) {
                        inputdata.setDataName(table);
                        inputdata.setTaskSrc("");
                        inputdata.setDataID(inputdata.getDataName());
                    }
                    JSONObject jsonObjectParams = new JSONObject(true);
                    jsonObjectParams.put("table", table);
                    jsonObjectParams.put("field", field);
                    List<Integer> list = new ArrayList<>();
                    list.add(i);
                    jsonObjectParams.put("index", Arrays.toString(list.toArray()));


                    inputdata.setDomainID(getFieldDomainID(jsonObjectParams.get("table") + "." + jsonObjectParams.get("field")));
                    inputdata.setRole("server");
                    inputdata.setParams(jsonObjectParams);
                    inputDatas.add(inputdata);
                }
                break;
            }
            case "QUERY": {
                String tableField = str.substring(str.indexOf("[") + 1, str.length() - 1);
                String table = tableField.split("\\.")[0];
                String field = tableField.split("\\.")[1];
                TaskInputData inputdata = new TaskInputData();
                for (String dataName : leftDataName) {
                    if (dataName.startsWith(table)) {
                        inputdata.setDataName(dataName);
                        if (dataName.contains("-")) {
                            inputdata.setTaskSrc(dataName.split("-")[1]);
                            inputdata.setDataID("");
                        } else {
                            inputdata.setTaskSrc("");
                            inputdata.setDataID(inputdata.getDataName());
                        }
                        break;
                    }
                }
                if (inputdata.getDataName() == null) {
                    inputdata.setDataName(table);
                    inputdata.setTaskSrc("");
                    inputdata.setDataID(inputdata.getDataName());
                }

                JSONObject jsonObjectParams = new JSONObject(true);
                if (isNumeric(tableField)) {
                    jsonObjectParams.put("const", tableField);
                    inputdata.setDomainID("");
                } else {
                    jsonObjectParams.put("table", table);
                    jsonObjectParams.put("field", field);
                    inputdata.setDomainID(getFieldDomainID(tableField));
                }


                inputdata.setRole("server");
                inputdata.setParams(jsonObjectParams);
                inputDatas.add(inputdata);
                break;
            }
            default:
                break;
        }

        input.setData(inputDatas);
        task.setInput(input);

        // 输出信息
        Output output = new Output();
        List<TaskOutputData> outputDatas = new ArrayList<>();
        TaskOutputData outputdata1 = new TaskOutputData();
        TaskOutputData outputdata2 = new TaskOutputData();
        String inputDataName1 = inputDatas.get(0).getDataName();
        String outputPrefix = "";
        if (inputDataName1.contains("-")) {
            outputPrefix = inputDataName1.substring(0, inputDataName1.indexOf('-'));
        } else {
            outputPrefix = inputDataName1;
        }


        switch (moduleName) {
            case "PSI":
                outputdata1.setDataName(outputPrefix+"-"+curTaskName);
                outputdata1.setFinalResult("N");
                outputdata1.setDomainID(inputDatas.get(0).getDomainID());
                outputdata1.setDataID("");

                // PSI需要两个output的原因主要是保存到不同的提供者domainID中
                String inputDataName2 = inputDatas.get(1).getDataName();
                if (inputDataName2.contains("-")) {
                    outputdata2.setDataName(inputDataName2.substring(0, inputDataName2.indexOf('-')) + "-" + curTaskName);
                } else {
                    outputdata2.setDataName(inputDataName2 + "-" + curTaskName);
                }
                outputdata2.setFinalResult("N");
                outputdata2.setDomainID(inputDatas.get(1).getDomainID());
                outputdata2.setDataID("");

                if (isFinalResult) {
                    outputdata1.setFinalResult("Y");
                    outputdata2.setFinalResult("Y");
                }

                outputDatas.add(outputdata1);
                outputDatas.add(outputdata2);
                break;
            case "ConstantFilter":
            case "VariableFilter":
            case "MPC":
            case "QUERY":
                outputdata1.setDataName(outputPrefix+"-"+curTaskName);
                outputdata1.setFinalResult("N");
                outputdata1.setDomainID(inputDatas.get(0).getDomainID());
                outputdata1.setDataID("");
                if (isFinalResult) {
                    outputdata1.setFinalResult("Y");
                }
                outputDatas.add(outputdata1);
                break;
            default:
                break;
        }

        output.setData(outputDatas);
        task.setOutput(output);

        // parties信息
        List<Party> parties = new ArrayList<>();
        HashSet<String> partySet = new HashSet<>();
        for (TaskInputData inputData : inputDatas) {
            partySet.add(inputData.getDomainID());
        }
        for (String value : partySet) {
            Party party = new Party();
            party.setServerInfo(null);
            party.setStatus(null);
            party.setTimestamp(null);
            party.setPartyID(value);
            parties.add(party);
        }
        task.setParties(parties);

        for (Party party : parties) {
            jobParties.add(party.getPartyID());
        }


        moduleNameCorrect(task);

        return task;
    }

    /**
     *
     * @param task
     */
    public void moduleNameCorrect(Task task) {
        switch (task.getModule().getModuleName()) {
            case "PSI":
                if (hint != null) {
                    logicalHintFix(task);
                } else {
                    if (task.getParties().size() > 1) {
                        task.getModule().setModuleName(TaskType.OTPSI.name());
                    } else {
                        task.getModule().setModuleName(TaskType.LOCALJOIN.name());
                    }
                }
                break;
            case "ConstantFilter":
                task.getModule().setModuleName(TaskType.LOCALFILTER.name());
                break;
            case "QUERY":
                task.getModule().setModuleName(TaskType.QUERY.name());
                break;
            case "MPC":
                String funcName = task.getModule().getParams().get("function").toString();
                if (task.getParties().size() > 1) {
                    if (funcName.equals("base")) {
                        // 算术表达式
                        task.getModule().setModuleName(TaskType.MPCEXP.name());
                    } else {
                        // 聚合表达式
                        task.getModule().setModuleName(TaskType.AGG.name() + funcName.toUpperCase());
                    }
                } else {
                    if (funcName.equals("base")) {
                        task.getModule().setModuleName(TaskType.LOCALEXP.name());
                    } else {
                        task.getModule().setModuleName(TaskType.LOCALAGG.name());
                    }
                }
                break;
            default:
                break;
        }

    }

    public void logicalHintFix(Task task) {
        List<HintExpression> list = hint.getValues();
        for (HintExpression kv : list) {
            if (kv.getKey().equals("TEEJOIN")) {
                List<String> values = kv.getValues();
                task.getModule().setModuleName(TaskType.TEEPSI.name());
                JSONObject params = task.getModule().getParams();
                params.put("teeHost", values.get(0).replaceAll("'", ""));
                params.put("teePort", values.get(1));
                for (TaskOutputData output : task.getOutput().getData()) {
                    output.setDomainID("");
                }
                break;
            }
        }
    }

    /**
     * 简化PhyPlan的表示，便于后续转化成Task
     * @param rel
     * @return
     */
    public String tidyPhyPlan(RelNode rel, int cnt) {
        String s = "";
        for (int i = 0; i < cnt; i++) {
            s += " ";
        }
        if (rel instanceof RelSubset) {
            s += tidyPhyPlan(((RelSubset) rel).getBest(), cnt);
        }
        else if (rel instanceof MPCTableScan) {
            s += rel.getRelTypeName() + " [";
            s += rel.getRowType().getFieldNames().get(0).split("\\.")[0];
            s += "]\n";
        } else if (rel instanceof MPCFilter) {
            cnt++;
            s += rel.getRelTypeName() + " [";
            s += "FilterCond[" + ((MPCFilter) rel).getCondition().toString() + "] $ from ";
            s += rel.getRowType().getFieldNames().toString();
            s += "]\n";
            for (int i = 0; i < rel.getInputs().size(); i++) {
                s += tidyPhyPlan(rel.getInput(i), cnt);
            }
        } else if (rel instanceof MPCJoin) {
            cnt++;
            s += rel.getRelTypeName() + " [";
            s += "JoinType[" + ((MPCJoin) rel).getJoinType().toString() + "], ";
            s += "JoinCond[" + ((MPCJoin) rel).getCondition().toString() + "], ";
            s += "$ from " + rel.getRowType().getFieldNames();
            s += "]\n";
            s += tidyPhyPlan(((MPCJoin) rel).getLeft(), cnt);
            s += tidyPhyPlan(((MPCJoin) rel).getRight(), cnt);
        } else if (rel instanceof MPCProject){
            cnt++;
            s += rel.getRelTypeName() + " [";
            int n = ((MPCProject) rel).getNamedProjects().size();
            for (int i = 0; i < n; i++) {
                String proj = ((MPCProject) rel).getNamedProjects().get(i).right;
                s += proj;
                if (i < n-1) {
                    s += ", ";
                }
            }
            s += "]\n";
            for (int i = 0; i < rel.getInputs().size(); i++) {
                s += tidyPhyPlan(rel.getInput(i), cnt);
            }
        } else {
            ;
        }
        return s;
    }

    /**
     * 生成原始Task对象，已完成固定信息的填充
     * @return
     */
    public Task basicTask(String taskName) {
        Task task = new Task();
        String taskVersion = "1.0.0";
        String taskStatus = "WAITING";
        task.setVersion(taskVersion);
        task.setCreateTime(createTime);
        task.setUpdateTime(createTime);
        task.setStatus(taskStatus);
        task.setJobID(jobID);
        task.setTaskName(taskName);
        return task;
    }

    public String getFieldDomainID(String fieldName) {
        return metadata.getFieldInfo(fieldName).getDomainID();
    }


}
