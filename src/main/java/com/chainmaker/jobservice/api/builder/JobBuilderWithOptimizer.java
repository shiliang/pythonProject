package com.chainmaker.jobservice.api.builder;

import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.model.bo.job.Job;
import com.chainmaker.jobservice.api.model.bo.job.JobTemplate;
import com.chainmaker.jobservice.api.model.bo.job.task.*;
import com.chainmaker.jobservice.api.model.bo.job.task.Module;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;
import com.chainmaker.jobservice.api.response.ParserException;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.FieldInfo;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.MPCMetadata;
import com.chainmaker.jobservice.core.calcite.relnode.MPCFilter;
import com.chainmaker.jobservice.core.calcite.relnode.MPCJoin;
import com.chainmaker.jobservice.core.calcite.relnode.MPCProject;
import com.chainmaker.jobservice.core.calcite.utils.parserWithOptimizerReturnValue;
import com.chainmaker.jobservice.core.optimizer.plans.*;
import com.chainmaker.jobservice.core.parser.plans.FederatedLearning;
import com.chainmaker.jobservice.core.parser.plans.LogicalHint;
import com.chainmaker.jobservice.core.parser.plans.LogicalPlan;
import com.chainmaker.jobservice.core.parser.plans.LogicalProject;
import com.chainmaker.jobservice.core.parser.tree.*;
import com.google.gson.Gson;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.volcano.RelSubset;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rex.*;
import org.apache.calcite.sql.SqlExplainFormat;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;

import java.util.*;

import static com.chainmaker.jobservice.core.calcite.utils.ConstExprJudgement.isNumeric;

public class JobBuilderWithOptimizer extends PhysicalPlanVisitor{
    private enum JobType {
        FQ, FQS, FL, FLS, CC, CCS
    }
    private enum TaskType {
        QUERY, LOCALFILTER, LOCALJOIN, OTPSI, PSIRSA, TEEPSI, TEEAVG, MPC, MPCEXP, FL, TEE, LOCALMERGE, LOCALEXP, LOCALAGG, NOTIFY
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
    private List<Task> taskcp = new ArrayList<>();
    private HashSet<String> jobParties = new HashSet<>();
    private LogicalPlan OriginPlan;
    private LogicalHint hint;
    private HashMap<String, String> columnInfoMap;

    public JobBuilderWithOptimizer(Integer modelType, Integer isStream, parserWithOptimizerReturnValue value, HashMap<String, String> columnInfoMap) {
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
        this.columnInfoMap = columnInfoMap;
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
            jobType = JobType.CCS.name();
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

        System.out.println(
                RelOptUtil.dumpPlan("[Physical plan] TEXT", phyPlan, SqlExplainFormat.TEXT,
                        SqlExplainLevel.ALL_ATTRIBUTES));

        HashMap<RelNode, Task> phyTaskMap = new HashMap<>();
        // 生成tasks
        tasks.addAll(dfsPlan(phyPlan, phyTaskMap));
        updateTeePsi();
        // 特殊处理联邦学习相关的tasks
        generateFLTasks(OriginPlan);
        // 合并OTPSI，暂时放弃
//        mergeOTPSI();
        // PSI后通知所有参与表
//        notifyPSIOthers();
        // 合并本地tasks
//        mergeLocalTasks();

        job.setJobID(jobID);
        job.setJobType(jobType);
        job.setStatus(jobStatus);
        job.setCreateTime(createTime);
        job.setUpdateTime(createTime);
        job.setTasksDAG(taskDAG);
        job.setParties(new ArrayList<>(jobParties));
    }

    public void updateTeePsi() {
        Boolean updateFlag = true;
        String psiColumn = "";
        HashMap<Integer, String> indexPartyMap = new HashMap<>();
        HashMap<String, TaskOutputData> outputMap = new HashMap<>();
         for (Task task : tasks) {
             if (task.getModule().getModuleName().equals(TaskType.TEEPSI.name())) {
                 psiColumn = task.getInput().getData().get(0).getParams().getString("field");
                 for (TaskOutputData taskOutputData : task.getOutput().getData()) {
                     outputMap.put(taskOutputData.getDomainID(), taskOutputData);
                 }
             }
         }
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getModule().getModuleName().equals(TaskType.QUERY.name())) {
                indexPartyMap.put(i, tasks.get(i).getParties().get(0).getPartyID());
                if (!tasks.get(i).getInput().getData().get(0).getParams().getString("field").equals(psiColumn)) {
                    updateFlag = false;
                }
            }
        }
        if (updateFlag) {
            for (Integer index : indexPartyMap.keySet()) {
                System.out.println("remove query");
                Task task = tasks.get(index);
                tasks.remove(task);
            }
            System.out.println(tasks.size());
            for (Task task : tasks) {
                if (task.getModule().getModuleName().equals(TaskType.TEEPSI.name())) {
                    Output output = new Output();
                    List<TaskOutputData> taskOutputDataList = new ArrayList<>();
                    for (TaskOutputData taskOutputData : task.getOutput().getData()) {
                        if (indexPartyMap.containsValue(taskOutputData.getDomainID())) {
                            taskOutputData.setFinalResult("Y");
                            taskOutputDataList.add(taskOutputData);
                        }
                    }
                    output.setData(taskOutputDataList);
                    task.setOutput(output);
                }
            }
        }

    }

    /**
     * 将物理计划转化成Task列表
     * @param phyPlan
     * @param phyTaskMap
     * @return
     */
    public List<Task> dfsPlan(RelNode phyPlan, HashMap<RelNode, Task> phyTaskMap) {
        List<Task> result = new ArrayList<>();
        if (phyPlan instanceof RelSubset) {
            phyPlan = ((RelSubset) phyPlan).getBest();
        }
        int n = phyPlan.getInputs().size();
        for (int i = 0; i < n; i++) {
            result.addAll(dfsPlan(phyPlan.getInput(i), phyTaskMap));
        }
        result.addAll(generateTasks(phyPlan, phyTaskMap));
        return result;
    }

    /**
     * 最后一次PSI中的任意一方通知其他所有表最后的求交结果
     */
    public void notifyPSIOthers() {
        int n = tasks.size();
        HashSet<String> notifyList = new HashSet<>();
        HashMap<String, String> affectedOutputNames = new HashMap<>();
        int maxPSIid = 0;
        String leader1 = null;  // 多方PSI后最后的通知方，选择一个即可，留两个是为了之后可能会选择更优的那个来进行通知
        String leader2 = null;
        for (int i = 0; i < n; i++) {
            if (tasks.get(i).getModule().getModuleName().equals(TaskType.OTPSI.name())) {
                maxPSIid = i;
                leader1 = tasks.get(i).getInput().getData().get(0).getParams().getString("table");
                leader2 = tasks.get(i).getInput().getData().get(1).getParams().getString("table");
                notifyList.add(leader1);
                notifyList.add(leader2);
                for (TaskOutputData outputData : tasks.get(i).getOutput().getData()) {
                    String[] tmp = outputData.getDataName().split("-");
                    affectedOutputNames.put(tmp[0], tmp[1]);
                }
            }
        }
        // 如果PSI次数小于等于1次，则不需要进行额外通知
        if (affectedOutputNames.size() <= 2) {
            return;
        }

        // 基本信息
        Task task = basicTask(String.valueOf(cnt++));

        // module信息（即进行什么操作）
        Module module = new Module();
        module.setModuleName(TaskType.NOTIFY.name());
        JSONObject moduleparams = new JSONObject(true);
        moduleparams.put("leader", leader1);
        module.setParams(moduleparams);
        task.setModule(module);

        // 输入信息
        Input input = new Input();
        List<TaskInputData> inputDatas = new ArrayList<>();
        for (String table : notifyList) {
            if (table.equals(leader1) || table.equals(leader2)) {
                continue;
            }
            TaskInputData inputData = new TaskInputData();
            inputData.setRole("follower");
            inputData.setDataName(table + "-" + affectedOutputNames.get(table));
            inputData.setTaskSrc(affectedOutputNames.get(table));
            inputData.setDomainID(metadata.getTableOrgId(table));
            JSONObject inputDataParams = new JSONObject(true);
            inputDataParams.put("table", table);
            inputData.setParams(inputDataParams);
            inputDatas.add(inputData);
        }
        input.setData(inputDatas);
        task.setInput(input);


        // 输出信息
        Output output = new Output();
        List<TaskOutputData> outputDatas = new ArrayList<>();
        for (TaskInputData inputData : inputDatas) {
            TaskOutputData outputData = new TaskOutputData();
            outputData.setDataName(inputData.getParams().get("table") + "-" + task.getTaskName());
            outputData.setDomainID(inputData.getDomainID());
            outputData.setFinalResult("N");
            outputDatas.add(outputData);
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
        tasks.add(task);

        // 通知上位依赖，outputDataName的修改
        for (int i = maxPSIid+1; i < n; i++) {
            for (TaskInputData inputData : tasks.get(i).getInput().getData()) {
                String oldDataName = inputData.getDataName();
                String[] tmp = oldDataName.split("-");
                if (tmp[0].equals(leader1) || tmp[0].equals(leader2)) {
                    continue;
                }
                String oldID = affectedOutputNames.get(tmp[0]);
                if (tmp[1].equals(oldID)) {
                    inputData.setDataName(tmp[0] + "-" + task.getTaskName());
                }
            }
        }
    }

    /**
     * 合并tasks中的所有OTPSI，可以适配更多的底层实现方法
     * 暂时弃用，改用添加额外的通知task
     */
    @Deprecated
    public void mergeOTPSI() {
        List<Task> psiTaskList = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getModule().getModuleName().equals(TaskType.OTPSI.name())) {
                psiTaskList.add(tasks.get(i));
            } else {
                continue;
            }
        }
//        // 找出所有的OTPSI 的 input、output、parties信息
//        List<TaskInputData> inputDataList = new ArrayList<>();
//        HashSet<Party> parties = new HashSet<>();
//        HashSet<String> outputNames = new HashSet<>();
//        int lastOTPSItaskId = -1;
//        for (int i = 0; i < tasks.size(); i++) {
//            Task t = tasks.get(i);
//            if (!t.getModule().getModuleName().equals(TaskType.OTPSI.name())) {
//                continue;
//            }
//            inputDataList.addAll(t.getInput().getData());
//            parties.addAll(t.getParties());
//            for (int j = 0; i < t.getOutput().getData().size(); i++) {
//                outputNames.add(t.getOutput().getData().get(i).getDataName().split("-")[0]);
//            }
//            lastOTPSItaskId = i;
//        }
//
//        // 修改最后一个OTPSI的task信息
//        if (inputDataList.size() <= 2) {
//            return;
//        }
//        Task t = tasks.get(lastOTPSItaskId);
//        t.setParties(List.copyOf(parties));
//        List<TaskInputData> newInputDataList = new ArrayList<>();
//        for (int i = 0; i < inputDataList.size(); i += 2) {
//
//        }
//
//        // 改变上位依赖项

    }


    /**
     * 修改LocalJoin中的output数量 超过一个的都改成一个，并修改上位依赖
     * @param
     */
    public void localJoinCorrect() {
        for (int taskID = 0; taskID < tasks.size(); taskID++) {
            Task t = tasks.get(taskID);
            if (!t.getModule().getModuleName().equals(TaskType.LOCALJOIN.name())) {
                continue;
            }
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
            String outName = "LOCALJOIN-" + t.getTaskName();
            t.getOutput().getData().get(0).setDataName(outName);

            // 修改上位依赖项
            for (int i = taskID + 1; i < tasks.size(); i++) {
                Task ft = tasks.get(i);
                for (TaskInputData inputData : ft.getInput().getData()) {
                    String inputName = inputData.getDataName();
                    if (outputNames.contains(inputName)) {
                        inputData.setDataName(outName);
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
        // 2、先复制一份tasks
        makeTackCp();
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
     * 深拷贝tasks
     */
    private void makeTackCp() {
        Gson gson = new Gson();
        for (int i = 0; i < tasks.size(); i++) {
            taskcp.add(gson.fromJson(gson.toJson(tasks.get(i)), Task.class));
            String moduleName = taskcp.get(i).getModule().getModuleName();
            if (moduleName.equals(TaskType.LOCALEXP.name()) || moduleName.equals(TaskType.LOCALAGG.name()) || moduleName.startsWith(TaskType.MPC.name())) {
                for (TaskInputData inputData : taskcp.get(i).getInput().getData()) {
                    List<Double> doubleList = (List<Double>) inputData.getParams().get("index");
                    List<Integer> integerList = new ArrayList<>();
                    for (double j : doubleList) {
                        integerList.add((int) j);
                    }
                    inputData.getParams().put("index", integerList);
                }
            }
        }
    }

    /**
     * 具体merge操作的实现
     * @param root
     * @return
     */
    public Task mergeTaskTree(TaskNode root) {
        Task ans = taskcp.get(root.taskID);
        String sql = "select ProjectString from TableJoinString where PredicateString";
        String ProjectString = "", TableJoinString = "", PredicateString = "";
        HashSet<String> inputTables = new HashSet<>();
        HashSet<String> outputTables = new HashSet<>();
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
            Task t = taskcp.get(tn.taskID);
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
                    outputTables.add(t.getOutput().getData().get(0).getDataName());
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
                    outputTables.add(t.getOutput().getData().get(0).getDataName());
//                    outputTables.add(t.getOutput().getData().get(1).getDataName());
                    if (TableJoinString.equals("")) {
                        TableJoinString += "(" + leftTable + " join " + rightTable + " on " + joinCond + ")";
                    } else {
                        // 还需要考虑四个Table，先两两Join，再Join的情况，暂时没有支持
                        TableJoinString = "(" + TableJoinString;
                        boolean isLeftLocalJoin = taskcp.get(Integer.parseInt(t.getInput().getData().get(0).getTaskSrc())).getModule().getModuleName().equals(TaskType.LOCALJOIN.name());
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
                    outputTables.add(t.getOutput().getData().get(0).getDataName());
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
                        for (TaskInputData inputData : list) {
                            List<Integer> index = (List<Integer>) inputData.getParams().get("index");
                            if (!index.contains(i)) {
                                continue;
                            }
                            while (pos < xpos) {
                                proj += exp.charAt(pos);
                                pos++;
                            }
                            pos++;
                            String table = inputData.getParams().getString("table");
                            String field = inputData.getParams().getString("field");
                            inputTables.add(table);
                            outputTables.add(t.getOutput().getData().get(0).getDataName());
                            proj += table + "." + field;
                            xpos = exp.indexOf('x', xpos + 1);
                            break;
                        }
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
                        inputTables.add(table);
                        outputTables.add(t.getOutput().getData().get(0).getDataName());
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

        // 删除多余的output，修改outputName
        for (int i = 1; i < ans.getOutput().getData().size(); i++) {
            ans.getOutput().getData().remove(i);
        }
        String outName = "LOCAL-" + ans.getTaskName();
        ans.getOutput().getData().get(0).setDataName(outName);

        // 修改上位依赖
        for (int i = root.taskID+1; i < tasks.size(); i++) {
            Task ft = taskcp.get(i);
            for (TaskInputData inputData : ft.getInput().getData()) {
                String inputName = inputData.getDataName();
                if (outputTables.contains(inputName)) {
                    inputData.setDataName(outName);
                }
            }
        }

        // 反向生成sql并填写相关module信息
        ans.getModule().setModuleName(TaskType.LOCALMERGE.name());
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
     * 遍历Task调用关系树，对Local节点进行merge
     * @param root
     */
    public void dfsTaskTree(TaskNode root) {
        if (root.isMerged) {
            return;
        }
        root.isMerged = true;
        if (root.taskID != -1 && root.isLocal()) {
            if (root.inputs.size() > 0) {
                System.out.println("mergedTask ID = " + root.taskID);
                mergedTasks.add(mergeTaskTree(root));
                return;
            }
        }
        if (root.taskID != -1) {
            System.out.println("notMergedTask ID = " + root.taskID);
            mergedTasks.add(taskcp.get(root.taskID));
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
        HashSet<String> PSIPartyIds = new HashSet<>();
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
            // 如果发生PSI，则后续partyIds将至少包含该node的partyIds（这里的partyIds专用于判断是否可以merge，和task本身的有区别）
            if (t.getModule().getModuleName().equals(TaskType.OTPSI.name())) {
                PSIPartyIds.addAll(node.partyIds);
            }
            // merge parties
            for (Party p : t.getParties()) {
                node.partyIds.add(p.getPartyID());
            }
            node.partyIds.addAll(PSIPartyIds);

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
        } else if (node instanceof LogicalProject) {
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
        Task task = basicTask(String.valueOf(cnt++));

        // module
        Module module = new Module();
        module.setModuleName(TaskType.TEE.name());
        JSONObject moduleParams = new JSONObject(true);
        moduleParams.put("methodName", expr.getFunction().toString());
        moduleParams.put("domainID", "wx-org3.chainmaker.orgDID");
        moduleParams.put("teeHost", "172.16.12.230");
        moduleParams.put("teePort", "30091");
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
            dataParams.put("type", columnInfoMap.get(table+field));
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
        partySet.add(module.getParams().get("domainID").toString());
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
        Task task = basicTask(String.valueOf(cnt++));

        // module
        Module module = new Module();
        module.setModuleName(TaskType.FL.name());
        JSONObject moduleParams = new JSONObject(true);
        if (expression.getPsi().size() != 0) {
            moduleParams.put("intersection", parseFLParams(expression.getPsi()));
        }
        if (expression.getFl().size() != 0) {
            moduleParams.put("fl", parseFLParams(expression.getFl()));
        }

        if (expression.getFeat().size() != 0) {
            moduleParams.put("feat", parseFLParams(expression.getFeat()));
        }
        if (expression.getModel().size() != 0) {
            moduleParams.put("model", parseFLParams(expression.getModel()));
        }
        if (expression.getEval().size() != 0) {
            moduleParams.put("eval", parseFLParams(expression.getEval()));
        }

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
            String key = expression.getLeft().toString();
            String value = expression.getRight().toString();
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
     * 具体Task转化的实现
     * @param phyPlan
     * @param phyTaskMap
     * @return
     */
    public List<Task> generateTasks(RelNode phyPlan, HashMap<RelNode, Task> phyTaskMap) {
        List<Task> tasks = new ArrayList<>();

//        if (phyPlan instanceof RelSubset) {
//            phyPlan = ((RelSubset) phyPlan).getBest();
//        }
        switch (phyPlan.getRelTypeName()) {
            case "MPCProject":
                for (int i = 0; i < phyPlan.getRowType().getFieldNames().size(); i++) {
                    RexNode node = ((MPCProject) phyPlan).getProjects().get(i);
                    List<String> inputList = new ArrayList<String>(List.of(phyPlan.getRowType().getFieldNames().get(i).split("\\+|-|\\*|/|%|\\[|]|\\(|\\)")));
                    // 删除如 SUM[ADATA.A1] 这种split出来剩余 SUM 的情况
                    for (int j = 0; j < inputList.size(); j++) {
                        if (!(inputList.get(j).contains("."))) {
                            inputList.remove(j);
                            j--;
                            continue;
                        }
                        if (inputList.get(j).matches("[-+]?\\d*\\.?\\d+")) {
                            inputList.remove(j);
                            j--;
                            continue;
                        }
                    }
                    System.out.println();
                    tasks.add(generateProjectTask((MPCProject) phyPlan, phyTaskMap, (RexCall) node, inputList));
                }
                break;
            case "MPCJoin":
                tasks.add(generateJoinTask((MPCJoin) phyPlan, phyTaskMap));
                break;
            case "MPCFilter":
                RexNode conds = ((MPCFilter) phyPlan).getCondition();
                // 多个filter条件 每个形成单独task
                if (conds.getKind() == SqlKind.AND) {
                    for (RexNode cond : ((RexCall) conds).getOperands()) {
                        tasks.add(generateFilterTask((MPCFilter) phyPlan, phyTaskMap, (RexCall) cond));
                    }
                } else {
                    tasks.add(generateFilterTask((MPCFilter) phyPlan, phyTaskMap, (RexCall) conds));
                }
                break;
            case "MPCTableScan":
                Task task = basicTask("");
                Output output = new Output();
                TaskOutputData outputData = new TaskOutputData();
                String tableName = phyPlan.getTable().getQualifiedName().get(0);
                outputData.setDataName(tableName);
                outputData.setDataID(tableName);
                output.setData(List.of(outputData));
                task.setOutput(output);
                Party party = new Party();
                party.setPartyID(metadata.getTableOrgId(tableName));
                task.setParties(List.of(party));
                phyTaskMap.put(phyPlan, task);
                break;
            default:

                break;
        }

        return tasks;
    }

    public Task generateProjectTask(MPCProject phyPlan, HashMap<RelNode, Task> phyTaskMap, RexCall node, List<String> inputList) {
        System.out.println("inputList:" + inputList);
        Task task = basicTask(String.valueOf(cnt++));

        // [AS(+($8, $7), ''), AS(SUM($4), ''), AS($0, '')]
        // 所有 project 默认最上层都是 AS 的 RexCall, 所以去掉一层之后才是真的 proj 的内容
        RexNode proj = node.getOperands().get(0);
        // module信息（即进行什么操作）
        Module module = new Module();

        JSONObject moduleparams = new JSONObject(true);
        if (proj instanceof RexCall) {
            SqlOperator op = ((RexCall) proj).getOperator();
            if (op.equals(SqlStdOperatorTable.PLUS) || op.equals(SqlStdOperatorTable.MINUS) ||
                    op.equals(SqlStdOperatorTable.MULTIPLY) || op.equals(SqlStdOperatorTable.DIVIDE) ||
                    op.equals(SqlStdOperatorTable.MOD)) {
                module.setModuleName("EXP");
                moduleparams.put("function", "base");
                String expr = dfsRexNode(proj);
                moduleparams.put("expression", expr);
            } else {
                module.setModuleName("AGG");
                moduleparams.put("function", op.toString());
                String expr = dfsRexNode(((RexCall) proj).getOperands().get(0));
                moduleparams.put("expression", expr);
            }
        } else if (proj instanceof RexInputRef){
            module.setModuleName(TaskType.QUERY.name());
        } else {
            // System.out.println("RexElse" + proj);
        }
        module.setParams(moduleparams);
        task.setModule(checkMpcModule(module));

        // 输入信息
        Input input = new Input();
        List<TaskInputData> inputDatas = new ArrayList<>();
        Task childTask = phyTaskMap.get(((RelSubset) phyPlan.getInput()).getBest());
        for (int i = 0; i < inputList.size(); i++) {
            TaskInputData inputdata = new TaskInputData();
            String tableField = inputList.get(i);
            inputdata.setTaskSrc(childTask.getTaskName());
            inputdata.setDomainID(getFieldDomainID(tableField));
            if (childTask.getTaskName().equals("") || childTask.getOutput().getData().get(0).getDataName().startsWith(inputdata.getDomainID())) {
                inputdata.setDataName(childTask.getOutput().getData().get(0).getDataName());
                inputdata.setDataID(childTask.getOutput().getData().get(0).getDataID());
            } else {
                inputdata.setDataName(childTask.getOutput().getData().get(1).getDataName());
                inputdata.setDataID(childTask.getOutput().getData().get(1).getDataID());
            }
            if (i == 0) {
                inputdata.setRole("server");
            } else {
                inputdata.setRole("client");
            }
            JSONObject inputParam = new JSONObject(true);
            inputParam.put("table", tableField.split("\\.")[0]);
            inputParam.put("field", tableField.split("\\.")[1]);
            if (module.getModuleName().equals("EXP") || module.getModuleName().equals("AGG")) {
                inputParam.put("type", columnInfoMap.get(tableField.split("\\.")[0]+tableField.split("\\.")[1]));
                List<Integer> list = new ArrayList<>();
                list.add(i);
                inputParam.put("index", Arrays.toString(list.toArray()));
            }
            inputdata.setParams(inputParam);
            inputDatas.add(inputdata);
        }

        input.setData(inputDatas);
        task.setInput(input);

        // 输出信息
        Output output = new Output();
        TaskOutputData outputdata = new TaskOutputData();
        outputdata.setDataName(inputDatas.get(0).getDomainID() + "-" + cnt);
        outputdata.setFinalResult("Y");
        outputdata.setDomainID(inputDatas.get(0).getDomainID());
        outputdata.setDataID("");

        output.setData(List.of(outputdata));
        task.setOutput(output);

        // parties信息
        List<Party> parties = new ArrayList<>();
        HashSet<String> partySet = new HashSet<>();
        for (TaskInputData inputData : input.getData()) {
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

        if (parties.size() == 1) {
            if (module.getModuleName().equals("EXP")) {
                module.setModuleName(TaskType.LOCALEXP.name());
            } else if (module.getModuleName().equals("AGG")) {
                module.setModuleName(TaskType.LOCALAGG.name());
            }
        } else {
            if (module.getModuleName().equals("EXP")) {
                module.setModuleName(TaskType.MPCEXP.name());
            } else if (module.getModuleName().equals("AGG")) {
                module.setModuleName(TaskType.MPC.name());
            }
        }

        phyTaskMap.put(phyPlan, task);
        return task;
    }

    /**
     * 暂时只支持 a+b+c, sum(a+b+c) 这种 proj，所以来这里遍历的都是 exp 运算类型的 RexNode
     * 如果要支持非常复杂的表达式（互相嵌套的那种），最好采用 RexNode 的 accept、visitor 机制
     * @param proj
     * @return
     */
    public String dfsRexNode(RexNode proj) {
        String expr = "";
        if (proj instanceof RexCall) {
            String tmpl = dfsRexNode(((RexCall) proj).getOperands().get(0));
            if (tmpl.length() > 1 && tmpl.contains("x")) {
                expr += "(" + tmpl + ")";
            } else {
                expr += tmpl;
            }
            String tmpOp = ((RexCall) proj).getOperator().toString();
            if (tmpOp.equals("CAST")) {
                return expr;
            } else {
                expr += tmpOp;
            }
            String tmpr = dfsRexNode(((RexCall) proj).getOperands().get(1));
            if (tmpr.length() > 1 && tmpr.contains("x")) {
                expr += "(" + tmpr + ")";
            } else {
                expr += tmpr;
            }

        } else if (proj instanceof RexInputRef) {
            expr += "x";
        } else if (proj instanceof RexLiteral) {
            expr += ((RexLiteral) proj).getValue().toString();
        } else {
            ;
        }
        return expr;
    }

    public Task generateJoinTask(MPCJoin phyPlan, HashMap<RelNode, Task> phyTaskMap) {
        Task task = basicTask(String.valueOf(cnt++));
        RexCall cond = (RexCall) phyPlan.getCondition();

        // module信息（即进行什么操作）
        task.setModule(checkPSIModule(phyPlan));

        // 输入信息
        Input input = new Input();
        TaskInputData inputdata1 = new TaskInputData(), inputdata2 = new TaskInputData();
        JSONObject inputData1Params = new JSONObject(true);
        JSONObject inputData2Params = new JSONObject(true);

        String leftField = phyPlan.getRowType().getFieldNames().get(((RexInputRef) cond.getOperands().get(0)).getIndex());
        String rightField = phyPlan.getRowType().getFieldNames().get(((RexInputRef) cond.getOperands().get(1)).getIndex());
        Task leftChild = phyTaskMap.get(((RelSubset) phyPlan.getLeft()).getBest());
        System.out.println(((RelSubset) phyPlan.getRight()).getBest() instanceof MPCFilter);
        Task rightChild = phyTaskMap.get(((RelSubset) phyPlan.getRight()).getBest());

        inputdata1.setTaskSrc(leftChild.getTaskName());
        inputdata1.setRole("client");
        inputdata1.setDomainID(getFieldDomainID(leftField));
        if (leftChild.getTaskName().equals("")) {
            inputdata1.setDataName(leftField.split("\\.")[0]);
            inputdata1.setDataID(leftField.split("\\.")[0]);
        } else {
            if (leftChild.getOutput().getData().get(0).getDomainID().equals(inputdata1.getDomainID())) {
                inputdata1.setDataName(leftChild.getOutput().getData().get(0).getDataName());
                inputdata1.setDataID(leftChild.getOutput().getData().get(0).getDataID());
            } else {
                inputdata1.setDataName(leftChild.getOutput().getData().get(1).getDataName());
                inputdata1.setDataID(leftChild.getOutput().getData().get(1).getDataID());
            }
        }

        inputData1Params.put("table", leftField.split("\\.")[0]);
        inputData1Params.put("field", leftField.split("\\.")[1]);
        inputdata1.setParams(inputData1Params);

        inputdata2.setTaskSrc(rightChild.getTaskName());
        inputdata2.setRole("server");
        inputdata2.setDomainID(getFieldDomainID(rightField));
        if (rightChild.getTaskName().equals("")) {
            inputdata2.setDataName(rightField.split("\\.")[0]);
            inputdata2.setDataID(rightField.split("\\.")[0]);
        } else {
            if (rightChild.getOutput().getData().get(0).getDomainID().equals(inputdata2.getDomainID())) {
                inputdata2.setDataName(rightChild.getOutput().getData().get(0).getDataName());
            } else {
                inputdata2.setDataName(rightChild.getOutput().getData().get(1).getDataName());
                inputdata2.setDataID(rightChild.getOutput().getData().get(1).getDataID());
            }
        }

        inputData2Params.put("table", rightField.split("\\.")[0]);
        inputData2Params.put("field", rightField.split("\\.")[1]);
        inputdata2.setParams(inputData2Params);

        input.setData(List.of(inputdata1, inputdata2));
        task.setInput(input);

        // parties信息
        List<Party> parties = new ArrayList<>();
        HashSet<String> partySet = new HashSet<>();
        for (TaskInputData inputData : input.getData()) {
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

        // 输出信息
        Output output = new Output();

        TaskOutputData outputdata1 = new TaskOutputData();
        TaskOutputData outputdata2 = new TaskOutputData();

        outputdata1.setDataName(inputdata1.getDomainID() + "-" + cnt);
        outputdata1.setFinalResult("N");
        outputdata1.setDomainID(inputdata1.getDomainID());
        outputdata1.setDataID("");

        outputdata2.setDataName(inputdata2.getDomainID() + "-" + cnt);
        outputdata2.setFinalResult("N");
        outputdata2.setDomainID(inputdata2.getDomainID());
        outputdata2.setDataID("");

        if (parties.size() == 1) {
            output.setData(List.of(outputdata1));
        } else {
            output.setData(List.of(outputdata1, outputdata2));
        }
        task.setOutput(output);


        phyTaskMap.put(phyPlan, task);

        return task;
    }

    public Task generateFilterTask(MPCFilter phyPlan, HashMap<RelNode, Task> phyTaskMap, RexCall cond) {
        Task task = basicTask(String.valueOf(cnt++));

        // module信息（即进行什么操作）
        Module module = new Module();
        module.setModuleName(TaskType.LOCALFILTER.name());
        JSONObject moduleparams = new JSONObject(true);

        moduleparams.put("operator", cond.getOperator().toString());
        RexLiteral constant;
        RexInputRef field;
        if (cond.getOperands().get(0) instanceof RexLiteral) {
            constant = (RexLiteral) cond.getOperands().get(0);
            field = (RexInputRef) cond.getOperands().get(1);
        } else {
            constant = (RexLiteral) cond.getOperands().get(1);
            field = (RexInputRef) cond.getOperands().get(0);
        }
        moduleparams.put("constant", constant.getValue());
        String tableField = phyPlan.getRowType().getFieldNames().get(field.getIndex());

        module.setParams(moduleparams);
        task.setModule(module);


        // 输入信息
        Input input = new Input();
        TaskInputData inputdata = new TaskInputData();
        Task childTask;
        if (phyTaskMap.containsKey(phyPlan)) {
            childTask = phyTaskMap.get(phyPlan);
        } else {
            childTask = phyTaskMap.get(((RelSubset) phyPlan.getInput()).getBest());
        }
        inputdata.setTaskSrc(childTask.getTaskName());
        inputdata.setDataName(childTask.getOutput().getData().get(0).getDataName());
        inputdata.setDomainID(getFieldDomainID(tableField));
        inputdata.setDataID(childTask.getOutput().getData().get(0).getDataID());
        inputdata.setRole("server");
        JSONObject inputParam = new JSONObject(true);
        inputParam.put("table", tableField.split("\\.")[0]);
        inputParam.put("field", tableField.split("\\.")[1]);
        inputdata.setParams(inputParam);
        input.setData(List.of(inputdata));
        task.setInput(input);

        // 输出信息
        Output output = new Output();
        TaskOutputData outputdata = new TaskOutputData();
        outputdata.setDataName(inputdata.getDomainID() + "-" + cnt);
        outputdata.setFinalResult("N");
        outputdata.setDomainID(inputdata.getDomainID());
        outputdata.setDataID("");

        output.setData(List.of(outputdata));
        task.setOutput(output);

        // parties信息
        List<Party> parties = new ArrayList<>();
        HashSet<String> partySet = new HashSet<>();
        for (TaskInputData inputData : input.getData()) {
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
        phyTaskMap.put(phyPlan, task);
        return task;
    }

    public Module checkPSIModule(MPCJoin phyPlan) {
        Module module = new Module();
        RexCall cond = (RexCall) phyPlan.getCondition();
        String moduleName = "";
        JSONObject moduleparams = new JSONObject(true);
        moduleparams.put("joinType", phyPlan.getJoinType().toString());
        moduleparams.put("operator", cond.getOperator().toString());
        module.setParams(moduleparams);

        if (hint != null) {
            for (HintExpression kv : hint.getValues()) {
                if (kv.getKey().equals("JOIN") && kv.getValues().get(0).equals("TEE")) {
                    moduleName = TaskType.TEEPSI.name();
                    module.setModuleName(moduleName);
                    module.getParams().put("teeHost", "127.0.0.1");
                    module.getParams().put("teePort", "30000");
                    module.getParams().put("domainID", "");
                    return module;
                }
            }
        }
        String leftTable = phyPlan.getRowType().getFieldNames().get(((RexInputRef) cond.getOperands().get(0)).getIndex()).split("\\.")[0];
        String rightTable = phyPlan.getRowType().getFieldNames().get(((RexInputRef) cond.getOperands().get(1)).getIndex()).split("\\.")[0];
        String org1 = metadata.getTableOrgId(leftTable);
        String org2 = metadata.getTableOrgId(rightTable);
        if (org1.equals(org2)) {
            moduleName = TaskType.LOCALJOIN.name();
        } else {
            moduleName = TaskType.OTPSI.name();
        }
        module.setModuleName(moduleName);

        return module;
    }
    public Module checkMpcModule(Module temp) {
        if (hint != null) {
            for (HintExpression kv : hint.getValues()) {
                if (kv.getKey().equals("TEEAVG")) {
                    temp.setModuleName(TaskType.TEEAVG.name());
                    temp.getParams().put("teeHost", "192.168.40.230");
                    temp.getParams().put("teePort", "30091");
                    temp.getParams().put("domainID", "");
                    System.out.println("module:" + temp);
                    return temp;
                }
            }
        }
        return temp;
    }

    /**
     * 将字符串转换成Task
     * @param str
     * @return
     */
    public Task str2Task(String str, int curTaskName, String moduleName, List<String> inputTables, boolean isFinalResult) {
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
                String joinOp = "";
                if (params.get(4).equals("true")) {
                    joinOp = "true";
                } else {
                    joinOp = params.get(4).substring(0, 1);
                }
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
                    String rightDataName = inputTables.get(0);
                    String leftDataName = inputTables.get(1);
                    if (leftDataName.contains("-")) {
                        String table = leftDataName.split("-")[0];
                        inputdata1.setDataName(leftDataName);
                        inputdata1.setTaskSrc(leftDataName.split("-")[1]);
                        inputdata1.setDomainID(metadata.getTableOrgId(table));
                        inputData1Params.put("table", table);
                    } else {
                        inputdata1.setDataName(leftDataName);
                        inputdata1.setTaskSrc("0");
                        inputdata1.setDomainID(metadata.getTableOrgId(leftDataName));
                        inputData1Params.put("table", leftDataName);
                    }
                    if (rightDataName.contains("-")) {
                        String table = leftDataName.split("-")[0];
                        inputdata2.setDataName(rightDataName);
                        inputdata2.setTaskSrc(rightDataName.split("-")[1]);
                        inputdata2.setDomainID(metadata.getTableOrgId(table));
                        inputData2Params.put("table", table);
                    } else {
                        inputdata2.setDataName(rightDataName);
                        inputdata2.setTaskSrc("0");
                        inputdata2.setDomainID(metadata.getTableOrgId(rightDataName));
                        inputData2Params.put("table", rightDataName);
                    }

                    if (!inputdata1.getDataName().contains("-")) {
                        inputdata1.setDataID(inputdata1.getDataName());
                    }
                    if (!inputdata2.getDataName().contains("-")) {
                        inputdata2.setDataID(inputdata2.getDataName());
                    }

                    inputdata1.setRole("client");
                    inputdata2.setRole("server");
                    inputdata1.setParams(inputData1Params);
                    inputdata2.setParams(inputData2Params);

                } else {
                    // 正常求交 on TA.id=TB.id
                    // Join左侧
                    String index = params.get(4).substring(params.get(4).indexOf("$") + 1, params.get(4).indexOf(","));
                    String tableField = fields.get(Integer.parseInt(index)).trim();
                    String leftTable = tableField.split("\\.")[0];
                    String leftField = tableField.split("\\.")[1];

                    for (String dataName : inputTables) {
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

                    for (String dataName : inputTables) {
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

                    inputdata1.setDomainID(getFieldDomainID(inputData1Params.get("table") + "." + inputData1Params.get("field")));
                    inputdata2.setDomainID(getFieldDomainID(inputData2Params.get("table") + "." + inputData2Params.get("field")));
                    inputdata1.setRole("client");
                    inputdata2.setRole("server");
                    inputdata1.setParams(inputData1Params);
                    inputdata2.setParams(inputData2Params);
                }

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
                for (String dataName : inputTables) {
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
                    for (String dataName : inputTables) {
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
                    jsonObjectParams.put("type", columnInfoMap.get(table+field));
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
                for (String dataName : inputTables) {
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

        // 输出信息
        Output output = new Output();
        List<TaskOutputData> outputDatas = new ArrayList<>();
        TaskOutputData outputdata1 = new TaskOutputData();
        TaskOutputData outputdata2 = new TaskOutputData();
        String inputDataName1 = "";
        String outputDomainID = "";
        for (TaskInputData taskInputData : inputDatas) {
            if (taskInputData.getDomainID().equals(parties.get(0).getPartyID())) {
                inputDataName1 = taskInputData.getDataName();
                outputDomainID = taskInputData.getDomainID();
            }
        }
        String outputPrefix = "";
        if (inputDataName1.contains("-")) {
            outputPrefix = inputDataName1.substring(0, inputDataName1.indexOf('-'));
        } else {
            outputPrefix = inputDataName1;
        }


        switch (moduleName) {
            case "PSI":
                String inputDataName0 = inputDatas.get(0).getDataName();
                if (inputDataName0.contains("-")) {
                    outputdata1.setDataName(inputDataName0.substring(0, inputDataName0.indexOf('-')) + "-" + curTaskName);
                } else {
                    outputdata1.setDataName(inputDataName0 + "-" + curTaskName);
                }
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
                outputdata1.setDomainID(outputDomainID);
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



        moduleNameCorrect(task);

        return task;
    }

    /**
     * 细分ModuleName
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
                        task.getModule().setModuleName(TaskType.MPC.name() + funcName.toUpperCase());
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
            if (kv.getKey().equals("JOIN")) {
                List<String> values = kv.getValues();
                task.getModule().setModuleName(TaskType.TEEPSI.name());
                JSONObject params = task.getModule().getParams();
                params.put("teeHost", "192.168.40.230");
                params.put("teePort", "30091");
                params.put("domainID", "");
//                for (TaskOutputData output : task.getOutput().getData()) {
//                    output.setDomainID("");
//                }
                break;
            }
        }
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
    private void multipartyPsi() {
        HashMap<String, TaskInputData> inputMap = new HashMap<>();
        HashMap<String, TaskOutputData> outputMap = new HashMap<>();
        int flag = 0, count = 0;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.getModule().getModuleName().equals("TEEPSI")) {
                for (int j = 0; j < task.getInput().getData().size(); j++) {
                    TaskInputData taskInputData = task.getInput().getData().get(j);
                    TaskOutputData taskOutputData = task.getOutput().getData().get(j);
                    String key = taskInputData.getParams().getString("table") + "_" + taskInputData.getParams().getString("field");
                    if (!inputMap.containsKey(key)) {
                        inputMap.put(key, taskInputData);
                    }
                    outputMap.put(key, taskOutputData);
                }
                count++;
                if (count == 1) {
                    flag = i;
                }
            }
        }
        for (int i = 1; i < count; i++) {
            tasks.remove(flag + i);
        }
        Input input = new Input();
        List<TaskInputData> inputData = new ArrayList<>(inputMap.values());
        input.setData(inputData);

        Output output = new Output();
        List<TaskOutputData> outputData = new ArrayList<>(outputMap.values());
        output.setData(outputData);
        if (tasks.get(flag).getModule().getModuleName().equals("TEEPSI")) {
            tasks.get(flag).setInput(input);
            tasks.get(flag).setOutput(output);
            List<Party> parties = new ArrayList<>();
            HashSet<String> partySet = new HashSet<>();
            for (TaskInputData taskInputData : input.getData()) {
                partySet.add(taskInputData.getDomainID());
            }
            for (String value : partySet) {
                Party party = new Party();
                party.setServerInfo(null);
                party.setStatus(null);
                party.setTimestamp(null);
                party.setPartyID(value);
                parties.add(party);
            }
            tasks.get(flag).setParties(parties);
        }
    }
}
