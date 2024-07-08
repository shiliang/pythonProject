package com.chainmaker.jobservice.api.builder;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.Constant;
import com.chainmaker.jobservice.api.enums.JobType;
import com.chainmaker.jobservice.api.model.OrgInfo;
import com.chainmaker.jobservice.api.model.job.Job;
import com.chainmaker.jobservice.api.model.job.service.Service;
import com.chainmaker.jobservice.api.model.job.task.*;
import com.chainmaker.jobservice.api.model.job.task.Module;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.FieldInfo;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.MPCMetadata;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.TableInfo;
import com.chainmaker.jobservice.core.calcite.relnode.*;
import com.chainmaker.jobservice.core.calcite.utils.ParserWithOptimizerReturnValue;
import com.chainmaker.jobservice.core.optimizer.plans.*;
import com.chainmaker.jobservice.core.parser.plans.*;
import com.chainmaker.jobservice.core.parser.tree.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.volcano.RelSubset;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.AggregateCall;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.rex.*;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.ImmutableBitSet;
import org.apache.calcite.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.chainmaker.jobservice.api.builder.CalciteUtil.*;


@Slf4j
public class JobBuilderWithOptimizer extends PhysicalPlanVisitor{
//    private enum JobType {
//        FQ(), FQS(), FL(), FLS(), CC(), CCS(), TEE(), MPC
//    }


    private enum TaskType {
        QUERY, LOCALFILTER, LOCALJOIN, OTPSI, PSIRSA, TEEPSI, TEEAVG, MPC, MPCEXP, FL, TEE, LOCALMERGE, LOCALEXP, LOCALAGG, NOTIFY
    }

    private class TaskNode {
        int taskID;
        boolean isMerged;
        LinkedHashSet<String> partyIds;
        List<TaskNode> inputs;
        List<TaskNode> fathers;

        TaskNode (int id) {
            taskID = id;
            isMerged = false;
            partyIds = new LinkedHashSet<>();
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
    private List<String> modelList = new ArrayList<>();
    private Job job = new Job();
    private List<Service> services = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private List<Task> mergedTasks = new ArrayList<>();
    private List<Task> taskcp = new ArrayList<>();
    private Set<Party> jobPartySet = Sets.newHashSet();
    private LinkedHashSet<String> jobParties = new LinkedHashSet<>();
    private XPCPlan originPlan;
    private XPCHint hint;
    private HashMap<String, String> columnInfoMap;
    private String orgID;
    private String orgName;
    private String sql;

    private Map<String, RelNode> tableOriginTableMap = new HashMap<>();


    public JobBuilderWithOptimizer(Integer modelType, Integer isStream, ParserWithOptimizerReturnValue value, HashMap<String, String> columnInfoMap, OrgInfo orgInfo, String sql) {
        this.modelType = modelType;
        this.isStream = isStream;
        this.phyPlan = value.getPhyPlan();
        this.originPlan = value.getOriginPlan();
        this.createTime = String.valueOf(System.currentTimeMillis());
        this.jobID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        this.metadata = MPCMetadata.getInstance();
        if (originPlan instanceof XPCHint) {
            hint = (XPCHint) originPlan;
            originPlan = (XPCPlan) originPlan.getChildren().get(0);
        } else {
            hint = null;
        }
        this.columnInfoMap = columnInfoMap;
        this.orgID = orgInfo.getOrgId();
        this.orgName = orgInfo.getOrgName();
        this.sql = sql;
    }

    public Job getJob() {
//        jobTemplate.setJob(job);
        job.setServiceList(services);
        job.setTaskList(tasks);
//        jobTemplate.setTasks(mergedTasks);
        return job;
    }

    public void build() {
        String jobStatus = "WAITING";
        String taskDAG = "taskDAG";
        Integer jobType = null;
//        if (modelType == 0 && isStream == 0) {
//            jobType = JobType.FQ.name();
//        } else if (modelType == 0 && isStream == 1) {
//            jobType = JobType.CCS.name();
//        } else if (modelType == 1 && isStream == 0) {
//            jobType = JobType.FL.name();
//        } else if (modelType == 1 && isStream == 1) {
//            jobType = JobType.FLS.name();
//        } else if (modelType == 2 && isStream == 0) {
//            jobType = JobType.CC.name();
//        } else if (modelType == 2 && isStream == 1) {
//            jobType = JobType.CCS.name();
//        } else {
//            throw new ParserException("暂不支持的任务类型");
//        }

        if (this.sql.contains("FL")) {
            jobType = JobType.FL.getValue();
        }else if (this.sql.contains("TEE")) {
            jobType = JobType.TEE.getValue();
        }else {
            jobType = JobType.MPC.getValue();
        }

        System.out.println(
                RelOptUtil.dumpPlan("[Physical plan] TEXT", phyPlan, SqlExplainFormat.TEXT,
                        SqlExplainLevel.ALL_ATTRIBUTES));

        HashMap<RelNode, Task> phyTaskMap = new HashMap<>();
        // 生成tasks
        generateFLTasks(originPlan);
        tasks.addAll(dfsPlan(phyPlan, phyTaskMap));
//        updateTeePsi();
        // 特殊处理联邦学习相关的tasks

        // 合并OTPSI，暂时放弃
//        mergeOTPSI();
        // PSI后通知所有参与表
        notifyPSIOthers();
        // 合并本地tasks
//        mergeLocalTasks();
        List<String> finalTasks = getFinalResultTasks();
        for (Task task : tasks) {
            String taskId = task.getTaskId();
            List<InputDetail> inputs = task.getInput().getInputDataDetailList();
            List<Output> outputs = task.getOutputList();
            Optional<InputDetail> srcTaskOp = inputs.stream().filter(x -> StrUtil.isNotEmpty(x.getTaskSrc())).findAny();
            if(srcTaskOp.isEmpty()){
                task.setTaskLabel("起始任务" + task.getTaskName());
            }else if(!finalTasks.contains(taskId)){
                //project, fl, tee 任务，默认都是最终任务，但是在子查询中，project有可能都是中间任务，需要更新finalResult。
                task.setTaskLabel("中间任务" + task.getTaskName());
                outputs.forEach(output -> {
                    output.setFinalResult("N");
                    output.setIsFinalResult(false);
                });
            } else{
                task.setTaskLabel("最终任务" + task.getTaskName());
            }
        }

        job.setJobId(jobID);
        job.setJobName(jobID);
        job.setModelType(jobType);
//        job.setStatus(jobStatus);
        job.setStatus(Constant.JOB_STATUS);
        job.setCreateTime(createTime);
        job.setUpdateTime(createTime);
        job.setSubmitter(orgID);
        job.setCreateUserId(orgID);
        job.setCreatePartyId(orgID);
        job.setCreatePartyName(orgName);
        job.setRequestData(sql);
        job.setTasksDAG(taskDAG);
        job.setPartyList(new ArrayList<>(jobPartySet));
    }

    public boolean isSingleOrg(RelNode phyPlan){
        List<String> fieldNames = phyPlan.getRowType().getFieldNames();
        Set<String> tables = fieldNames.stream()
                .map(x -> getTableName(fromNumericName2FieldName(phyPlan, x))).collect(Collectors.toSet());
        return tables.size() == 1;
    }






    //任务ID的生成是从叶子节点开始递增的，root节点的id都是最大的几个。
    public List<String> getFinalResultTasks(){
        List<Integer> queryIDs = new ArrayList<>();
        for (Task task : tasks) {
            if(task.getModule().getModuleName().equals(TaskType.QUERY.name())){
                queryIDs.add(Integer.parseInt(task.getTaskId()));
            }
        }
        queryIDs.sort(Comparator.reverseOrder());
        int index = 0;
        while (index + 1 < queryIDs.size() && queryIDs.get(index) - 1 == queryIDs.get(index + 1)) {
            index++;
        }
        // 截取连续部分
        return queryIDs.subList(0, index + 1).stream().map(String::valueOf).collect(Collectors.toList());

    }
    public void updateTeePsi() {
        Boolean updateFlag = true;
        String psiColumn = "";
        HashMap<Integer, String> indexPartyMap = new HashMap<>();
        HashMap<String, Output> outputMap = new HashMap<>();
         for (Task task : tasks) {
             if (task.getModule().getModuleName().equals(TaskType.TEEPSI.name())) {
                 psiColumn = task.getInput().getInputDataDetailList().get(0).getParams().getString("field");
                 for (Output taskOutputData : task.getOutputList()) {
                     outputMap.put(taskOutputData.getDomainId(), taskOutputData);
                 }
             }
         }
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getModule().getModuleName().equals(TaskType.QUERY.name())) {
                indexPartyMap.put(i, tasks.get(i).getPartyList().get(0).getPartyId());
                if (!tasks.get(i).getInput().getInputDataDetailList().get(0).getParams().getString("field").equals(psiColumn)) {
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
//                    Output output = new Output();
                    List<Output> taskOutputDataList = new ArrayList<>();
                    for (Output taskOutputData : task.getOutputList()) {
                        if (indexPartyMap.containsValue(taskOutputData.getDomainId())) {
                            taskOutputData.setFinalResult("Y");
                            taskOutputData.setIsFinalResult(true);
                            taskOutputDataList.add(taskOutputData);
                        }
                    }
//                    output.setData(taskOutputDataList);
                    task.setOutputList(taskOutputDataList);
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
        result.addAll(generateMpcTasks(phyPlan, phyTaskMap));
        return result;
    }

    /**
     * 最后一次PSI中的任意一方通知其他所有表最后的求交结果
     * （由于底层实现问题，现阶段实现针对三方做了特殊处理，导致不可扩展，后续需要修改）
     */
    public void notifyPSIOthers() {
        int n = tasks.size();
        LinkedHashSet<String> notifyList = new LinkedHashSet<>();
        HashMap<String, String> affectedOutputNames = new HashMap<>();
        int maxPSIid = 0;
        String leader1 = null;  // 多方PSI后最后的通知方，选择一个即可，留两个是为了之后可能会选择更优的那个来进行通知
        String leader2 = null;
        for (int i = 0; i < n; i++) {
            if (tasks.get(i).getModule().getModuleName().equals(TaskType.OTPSI.name())) {
                maxPSIid = i;
                leader1 = tasks.get(i).getInput().getInputDataDetailList().get(0).getParams().getString("table");
                leader2 = tasks.get(i).getInput().getInputDataDetailList().get(1).getParams().getString("table");
                notifyList.add(leader1);
                notifyList.add(leader2);
                for (Output outputData : tasks.get(i).getOutputList()) {
                    String outputName = outputData.getDataName();
                    int lastPos = outputName.lastIndexOf("-");
                    affectedOutputNames.put(outputName.substring(0, lastPos), outputName.substring(lastPos+1));
                }
            }
        }
        // 如果PSI次数小于等于1次，则不需要进行额外通知
        if (affectedOutputNames.size() <= 2) {
            return;
        }
        // 如果后续查询用不到中间求交的其他表，则也不用通知
        boolean needNotify = false;
        for (int i = maxPSIid+1; i < n; i++) {
            for (InputDetail inputData : tasks.get(i).getInput().getInputDataDetailList()) {
                String oldOrgID = inputData.getDomainId();
                if (oldOrgID.equals(metadata.getTableOrgId(leader1)) || oldOrgID.equals(metadata.getTableOrgId(leader2))) {
                    continue;
                }
                needNotify = true;
            }
        }
        if (!needNotify) {
            return;
        }

        // 基本信息
        Task task = basicTask(String.valueOf(maxPSIid+1));
        cnt++;

        // module信息（即进行什么操作）
        Module module = new Module();
        module.setModuleName(TaskType.NOTIFY.name());
//        JSONObject moduleparams = new JSONObject(true);

        List<ModuleParam> moduleparams = new ArrayList<ModuleParam>();
        module.setParamList(moduleparams);
        task.setModule(module);

        // 输入信息
        Input input = new Input();
        List<InputDetail> inputDatas = new ArrayList<>();

        // 此处为临时处理，逻辑基本写死，不可扩展
        // 由于只针对三方PSI，所以需要通知的就是第一次PSI，没有其他情况，所以直接照抄第一次PSI的Task
        module.setModuleName(TaskType.OTPSI.name());
        moduleparams.add(new ModuleParam("joinType", "INNER"));
        moduleparams.add(new ModuleParam("operator", "="));
        int firstIdx = 0;
        for (int i = 0; i < n; i++) {
            if (tasks.get(i).getModule().getModuleName().equals(TaskType.OTPSI.name())) {
                firstIdx = i;
                break;
            }
        }
        InputDetail inputData1 = new InputDetail();
        inputData1.setRole("server");
        inputData1.setDomainId(tasks.get(firstIdx).getInput().getInputDataDetailList().get(0).getDomainId());
        inputData1.setDomainName(tasks.get(firstIdx).getInput().getInputDataDetailList().get(0).getDomainName());
        inputData1.setDataName(inputData1.getDomainId() + "-" + affectedOutputNames.get(inputData1.getDomainId()));
        inputData1.setTaskSrc(String.valueOf(Integer.parseInt(affectedOutputNames.get(inputData1.getDomainId()))-1));
//        inputData1.setComments();
        JSONObject inputData1Params = new JSONObject(true);
        inputData1Params.put("table", tasks.get(firstIdx).getInput().getInputDataDetailList().get(0).getParams().get("table"));
        inputData1Params.put("field", tasks.get(firstIdx).getInput().getInputDataDetailList().get(0).getParams().get("field"));
        inputData1.setParams(inputData1Params);
        inputDatas.add(inputData1);

        InputDetail inputData2 = new InputDetail();
        inputData2.setRole("client");
        inputData2.setDomainId(tasks.get(firstIdx).getInput().getInputDataDetailList().get(1).getDomainId());
        inputData2.setDomainName(tasks.get(firstIdx).getInput().getInputDataDetailList().get(1).getDomainName());
        inputData2.setDataName(inputData2.getDomainId() + "-" + affectedOutputNames.get(inputData2.getDomainId()));
        inputData2.setTaskSrc(String.valueOf(Integer.parseInt(affectedOutputNames.get(inputData2.getDomainId()))-1));
        JSONObject inputData2Params = new JSONObject(true);
        inputData2Params.put("table", tasks.get(firstIdx).getInput().getInputDataDetailList().get(1).getParams().get("table"));
        inputData2Params.put("field", tasks.get(firstIdx).getInput().getInputDataDetailList().get(1).getParams().get("field"));
        inputData2.setParams(inputData2Params);
        inputDatas.add(inputData2);

        // 确保server是完整的那一方，对output有影响，因为此处只有client方会有输出
        if (Integer.parseInt(affectedOutputNames.get(inputData1.getDomainId())) < Integer.parseInt(affectedOutputNames.get(inputData2.getDomainId()))) {
            inputData1.setRole("client");
            inputData2.setRole("server");
        }

        // 以下是添加uid的通知方式
//        for (String table : notifyList) {
//            if (table.equals(leader1)) {
//                continue;
//            }
//            TaskInputData inputData = new TaskInputData();
//            if (table.equals(leader2)) {
//                inputData.setRole("server");
//            } else {
//                inputData.setRole("client");
//            }
//            inputData.setDomainID(metadata.getTableOrgId(table));
//            inputData.setDataName(inputData.getDomainID() + "-" + affectedOutputNames.get(inputData.getDomainID()));
//            inputData.setTaskSrc(String.valueOf(Integer.parseInt(affectedOutputNames.get(inputData.getDomainID()))-1));
//
//            JSONObject inputDataParams = new JSONObject(true);
//            inputDataParams.put("table", table);
////            inputDataParams.put("field", );
//            inputData.setParams(inputDataParams);
//            inputDatas.add(inputData);
//        }
        input.setInputDataDetailList(inputDatas);
        input.setTaskId(task.getTaskName());
        input.setSrcTaskId(inputData1.getTaskSrc());
        input.setSrcTaskName(inputData1.getTaskSrc());
        task.setInput(input);


        // 输出信息
//        Output output = new Output();
        List<Output> outputDatas = new ArrayList<>();
        for (InputDetail inputData : inputDatas) {
            if (inputData.getRole().equals("server")) {
                continue;
            }
            Output outputData = new Output();
            outputData.setDataName(metadata.getTableOrgId((String) inputData.getParams().get("table")) + "-" + task.getTaskName());
            outputData.setDomainId(inputData.getDomainId());
            outputData.setDomainName(inputData.getDomainName());
            outputData.setColumnName(inputData.getColumnName());
            outputData.setLength(inputData.getLength());
            outputData.setType(inputData.getType());
            outputData.setFinalResult("N");
            outputDatas.add(outputData);
        }
//        output.setData(outputDatas);
        task.setOutputList(outputDatas);

        // parties信息
        List<Party> parties = new ArrayList<>();
        for (InputDetail inputData : inputDatas) {
            Party party = new Party();
            party.setServerInfo(null);
            party.setStatus(null);
            party.setTimestamp(null);
            party.setPartyId(inputData.getDomainId());
            party.setPartyName(inputData.getDomainName());
            parties.add(party);
        }
        parties = parties.stream().filter(StreamUtils.distinctByKey(Party::getPartyId)).collect(Collectors.toList());
        task.setPartyList(parties);
        tasks.add(maxPSIid+1, task);

        // 通知上位依赖，outputDataName的修改
        for (int i = maxPSIid+2; i < n+1; i++) {
            tasks.get(i).setTaskName(String.valueOf(Integer.parseInt(tasks.get(i).getTaskName())+1));
            tasks.get(i).setTaskId(String.valueOf(Integer.parseInt(tasks.get(i).getTaskName())+1));
            for (InputDetail inputData : tasks.get(i).getInput().getInputDataDetailList()) {
                String oldOrgID = inputData.getDomainId();
                if (oldOrgID.equals(metadata.getTableOrgId(leader1)) || oldOrgID.equals(metadata.getTableOrgId(leader2))) {
                    continue;
                }
                inputData.setDataName(oldOrgID + "-" + task.getTaskName());
                inputData.setTaskSrc(task.getTaskName());
            }
            for (Output outputData : tasks.get(i).getOutputList()) {
                int pos = outputData.getDataName().lastIndexOf("-");
                String prefix = outputData.getDataName().substring(0, pos+1);
                outputData.setDataName(prefix+tasks.get(i).getTaskName());
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
//        LinkedHashSet<Party> parties = new LinkedHashSet<>();
//        LinkedHashSet<String> outputNames = new LinkedHashSet<>();
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
            LinkedHashSet<String> outputNames = new LinkedHashSet<>();
            int n = t.getOutputList().size();
            for (int i = 0; i < n; i++) {
                outputNames.add(t.getOutputList().get(i).getDataName());
            }
            // 删除多余的output，修改outputName
            for (int i = 1; i < n; i++) {
                t.getOutputList().remove(i);
            }
            String outName = "LOCALJOIN-" + t.getTaskName();
            t.getOutputList().get(0).setDataName(outName);

            // 修改上位依赖项
            for (int i = taskID + 1; i < tasks.size(); i++) {
                Task ft = tasks.get(i);
                for (InputDetail inputData : ft.getInput().getInputDataDetailList()) {
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
//        Gson gson = new Gson();
        for (int i = 0; i < tasks.size(); i++) {
            taskcp.add(tasks.get(i));
//            taskcp.add(gson.fromJson(gson.toJson(tasks.get(i)), Task.class));
            String moduleName = taskcp.get(i).getModule().getModuleName();
            if (moduleName.equals(TaskType.LOCALEXP.name()) || moduleName.equals(TaskType.LOCALAGG.name()) || moduleName.startsWith(TaskType.MPC.name())) {
                for (InputDetail inputData : taskcp.get(i).getInput().getInputDataDetailList()) {
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
        LinkedHashSet<String> inputTables = new LinkedHashSet<>();
        LinkedHashSet<String> outputTables = new LinkedHashSet<>();
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
                    String op = String.valueOf(t.getModule().getValueByKey("operator"));
                    String constant = String.valueOf(t.getModule().getValueByKey("constant"));
                    String table = t.getInput().getInputDataDetailList().get(0).getParams().getString("table");
                    String field = t.getInput().getInputDataDetailList().get(0).getParams().getString("field");
                    inputTables.add(table);
                    outputTables.add(t.getOutputList().get(0).getDataName());
                    String predicate = table + "." + field + op + constant;
                    if (PredicateString.equals("")) {
                        PredicateString += predicate;
                    } else {
                        PredicateString += " and " + predicate;
                    }
                    break;
                }
                case "LOCALJOIN": {
                    String joinType = String.valueOf(t.getModule().getValueByKey("joinType"));
                    String joinOp = String.valueOf(t.getModule().getValueByKey("operator"));
                    String leftTable = t.getInput().getInputDataDetailList().get(0).getParams().getString("table");
                    String leftField = t.getInput().getInputDataDetailList().get(0).getParams().getString("field");
                    String rightTable = t.getInput().getInputDataDetailList().get(1).getParams().getString("table");
                    String rightField = t.getInput().getInputDataDetailList().get(1).getParams().getString("field");
                    String joinCond = leftTable + "." + leftField + joinOp + rightTable + "." + rightField;
                    inputTables.add(leftTable);
                    inputTables.add(rightTable);
                    outputTables.add(t.getOutputList().get(0).getDataName());
//                    outputTables.add(t.getOutput().getData().get(1).getDataName());
                    if (TableJoinString.equals("")) {
                        TableJoinString += "(" + leftTable + " join " + rightTable + " on " + joinCond + ")";
                    } else {
                        // 还需要考虑四个Table，先两两Join，再Join的情况，暂时没有支持
                        TableJoinString = "(" + TableJoinString;
                        boolean isLeftLocalJoin = taskcp.get(Integer.parseInt(t.getInput().getInputDataDetailList().get(0).getTaskSrc())).getModule().getModuleName().equals(TaskType.LOCALJOIN.name());
                        if (isLeftLocalJoin) {
                            TableJoinString += " join " + rightTable + " on " + joinCond + ")";
                        } else {
                            TableJoinString += " join " + leftTable + " on " + joinCond + ")";
                        }
                    }
                    break;
                }
                case "QUERY": {
                    String table = t.getInput().getInputDataDetailList().get(0).getParams().getString("table");
                    String field = t.getInput().getInputDataDetailList().get(0).getParams().getString("field");
                    String proj = table + "." + field;
                    inputTables.add(table);
                    outputTables.add(t.getOutputList().get(0).getDataName());
                    if (ProjectString.equals("")) {
                        ProjectString += proj;
                    } else {
                        ProjectString += ", " + proj;
                    }
                    break;
                }
                case "LOCALEXP": {
                    String exp = String.valueOf(t.getModule().getValueByKey("expression"));
                    String proj = "";
                    int pos = 0;
                    int xnum = 0;
                    for (int i = 0; i < exp.length(); i++) {
                        if (exp.charAt(i) == 'x') {
                            xnum++;
                        }
                    }
                    int xpos = exp.indexOf('x', 0);
                    List<InputDetail> list = t.getInput().getInputDataDetailList();
                    for (int i = 0; i < xnum; i++) {
                        for (InputDetail inputData : list) {
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
                            outputTables.add(t.getOutputList().get(0).getDataName());
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
                    String exp = String.valueOf(t.getModule().getValueByKey("expression"));
                    String proj = "";
                    int pos = 0;
                    int xnum = 0;
                    for (int i = 0; i < exp.length(); i++) {
                        if (exp.charAt(i) == 'x') {
                            xnum++;
                        }
                    }
                    int xpos = exp.indexOf('x', 0);
                    List<InputDetail> list = t.getInput().getInputDataDetailList();
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
                        outputTables.add(t.getOutputList().get(0).getDataName());
                        proj += table + "." + field;
                        xpos = exp.indexOf('x', xpos+1);
                    }
                    proj = String.valueOf(t.getModule().getValueByKey("function")) + "(" + proj + ")";
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
        List<InputDetail> inputDataList = new ArrayList<>();
        for (String tbName : inputTables) {
            InputDetail inputData = new InputDetail();
            inputData.setDataName(tbName);
            inputDataList.add(inputData);
        }
        ans.getInput().setTaskId(ans.getTaskId());
        ans.getInput().setSrcTaskName(inputDataList.get(0).getTaskSrc());
        ans.getInput().setSrcTaskId(inputDataList.get(0).getTaskSrc());
        ans.getInput().setInputDataDetailList(inputDataList);

        // 删除多余的output，修改outputName
        for (int i = 1; i < ans.getOutputList().size(); i++) {
            ans.getOutputList().remove(i);
        }
        String outName = "LOCAL-" + ans.getTaskName();
        ans.getOutputList().get(0).setDataName(outName);

        // 修改上位依赖
        for (int i = root.taskID+1; i < tasks.size(); i++) {
            Task ft = taskcp.get(i);
            for (InputDetail inputData : ft.getInput().getInputDataDetailList()) {
                String inputName = inputData.getDataName();
                if (outputTables.contains(inputName)) {
                    inputData.setDataName(outName);
                }
            }
        }

        // 反向生成sql并填写相关module信息
        ans.getModule().setModuleName(TaskType.LOCALMERGE.name());
        ans.getModule().setParamList(new ArrayList<>());
        if (ProjectString.equals("")) {
            ProjectString = "*";
        }
        if (PredicateString.equals("")) {
            sql = "select " + ProjectString + " from " + TableJoinString;
        } else {
            sql = "select " + ProjectString + " from " + TableJoinString + " where " + PredicateString;
        }
        ans.getModule().getParamList().add(new ModuleParam("sql", sql));
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
        LinkedHashSet<String> PSIPartyIds = new LinkedHashSet<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            TaskNode node = new TaskNode(i);
            List<InputDetail> list = t.getInput().getInputDataDetailList();
            // 通过input构建父子节点之间的联系
            for (InputDetail inputData : list) {
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
            for (Party p : t.getPartyList()) {
                node.partyIds.add(p.getPartyId());
            }
            node.partyIds.addAll(PSIPartyIds);

            nodes.add(node);
            if (t.getOutputList().get(0).getFinalResult().equals("Y")) {
                root.inputs.add(node);
            }
        }
        return root;
    }

    /**
     * 生成FL和TEE相关的Task
     * @param node
     */
    public void generateFLTasks(XPCPlan node) {
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
        } else if (node instanceof XPCProject) {
            /*
                "select adata.a1, testt(adata.a1, bdata.b1) from adata, bdata";
             */
            List<NamedExpression> namedExpressionList = ((XPCProject) node).getProjectList().getValues();
            for (NamedExpression ne : namedExpressionList) {
                Expression expr = ne.getExpression();
                boolean funcTee = false;
                if (hint != null) {
                    for (HintExpression kv : hint.getValues()) {
                        if (kv.getKey().equals("FUNC") && kv.getValues().get(0).equals("TEE")) {
                            funcTee = true;
                        }
                    }
                }
                if (funcTee && expr instanceof FunctionCallExpression) {
                    // TESTT[ADATA.A1, BDATA.B1]
                    modelList.add(((FunctionCallExpression) expr).getFunction().toString());
                }
            }
        } else {
            ;
        }
    }

    public Task parseFederatedLearning(FederatedLearning node) {
        // basic info
        FederatedLearningExpression expression = node.getParamsList();
        Task task = basicTask(String.valueOf(cnt++));

        // module
        Module module = new Module();
        module.setModuleName(TaskType.FL.name());
//        JSONObject moduleParams = new JSONObject(true);
        List<ModuleParam> moduleParams = new ArrayList<ModuleParam>();
        if (expression.getPsi().size() != 0) {
            moduleParams.add(new ModuleParam("intersection", parseFLParams(expression.getPsi())));
        }
        if (expression.getFl().size() != 0) {
            moduleParams.add(new ModuleParam("fl", parseFLParams(expression.getFl())));
        }

        if (expression.getFeat().size() != 0) {
            moduleParams.add(new ModuleParam("feat", parseFLParams(expression.getFeat())));
        }
        if (expression.getModel().size() != 0) {
            moduleParams.add(new ModuleParam("model", parseFLParams(expression.getModel())));
        }
        if (expression.getEval().size() != 0) {
            moduleParams.add(new ModuleParam("eval", parseFLParams(expression.getEval())));
        }

        module.setParamList(moduleParams);
        task.setModule(module);

        // input
        Input input = new Input();
        List<InputDetail> inputDataList = new ArrayList<>();
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
        input.setInputDataDetailList(inputDataList);
        input.setTaskId(task.getTaskId());
        input.setSrcTaskId(inputDataList.get(0).getTaskSrc());
        input.setSrcTaskName(inputDataList.get(0).getTaskSrc());
        task.setInput(input);

        // output
//        Output output = new Output();
        List<Output> outputDataList = new ArrayList<>();
        Output outputData = new Output();
        outputData.setDataId("");
        outputData.setDataName("fl-" + cnt);
        outputData.setDomainId(inputDataList.get(0).getDomainId());
        outputData.setDomainName(inputDataList.get(0).getDomainName());
        outputData.setType(inputDataList.get(0).getType());
        outputData.setColumnName(inputDataList.get(0).getColumnName());
        outputData.setLength(inputDataList.get(0).getLength());
        outputData.setFinalResult("Y");
        outputData.setIsFinalResult(true);
        outputDataList.add(outputData);
//        output.setData(outputDataList);
        task.setOutputList(outputDataList);

        // party
        genParties(input, task);

        return task;
    }

    public InputDetail parseFLLabel(List<FlExpression> label) {
        String[] constLabels = {"output_format", "namespace", "label_type", "with_label", "table"};
        InputDetail inputData = new InputDetail();
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
        inputData.setDataId(dataName);
        inputData.setDomainId(metadata.getTableOrgId(dataName));
        inputData.setDomainName(metadata.getTable(dataName).getOrgName());
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

    public void buildTableCache(RelNode phyPlan){
        List<String> fieldNames = phyPlan.getRowType().getFieldNames();
        if(phyPlan instanceof MPCTableScan) {
            String tableName = getTableName(fieldNames.get(0));
            tableOriginTableMap.put(tableName, phyPlan);
        }else if(phyPlan instanceof MPCAggregate){
            Set<String> set = fieldNames.stream().map(CalciteUtil::getTableName).collect(Collectors.toSet());
            RelNode node = getOneInputOriginTable(phyPlan);
            for(String tableName : set){
                if(!tableOriginTableMap.containsKey(tableName) && node != null){
                    tableOriginTableMap.put(tableName, node);
                }
            }
        }else if(phyPlan instanceof MPCProject){
            RelNode node = getOneInputOriginTable(phyPlan);
            if(node != null){
                String tableName = getTableName(fieldNames.get(0));
                tableOriginTableMap.put(tableName, node);
            }
        }
    }

    public String getOriginalTableName(String table){
        return tableOriginTableMap.get(table).getTable().getQualifiedName().get(0);
    }


    //这个是准确的。
    //无法用表名去映射relNode，因为project可能涉及多个表名。
    public RelNode getOneInputOriginTable(RelNode phyPlan){
        if(phyPlan.getInputs().size() > 1){
            return null;
        }
        if(phyPlan instanceof MPCTableScan){
            return phyPlan;
        }else if(phyPlan instanceof RelSubset) {
            phyPlan = ((RelSubset) phyPlan).getBest();
        }else {
            phyPlan = phyPlan.getInputs().get(0);
        }
        return getOneInputOriginTable(phyPlan);
    }


    /**
     * 具体Task转化的实现
     * @param phyPlan
     * @param phyTaskMap
     * @return
     */
    public List<Task> generateMpcTasks(RelNode phyPlan, HashMap<RelNode, Task> phyTaskMap) {
        List<Task> tasks = new ArrayList<>();
        buildTableCache(phyPlan);
//        if (phyPlan instanceof RelSubset) {
//            phyPlan = ((RelSubset) phyPlan).getBest();
//        }
        switch (phyPlan.getRelTypeName()) {
            case "MPCProject":
//                notifyPSIOthers(tasks);
                for (int i = 0; i < phyPlan.getRowType().getFieldNames().size(); i++) {
                    RexNode node = ((MPCProject) phyPlan).getProjects().get(i);
                    List<String> inputList = getInputList(phyPlan, i);
                    tasks.add(generateProjectTask((MPCProject) phyPlan, phyTaskMap, node, inputList));
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
            case "MPCAggregate":
                tasks.add(generateAggregateTask((MPCAggregate) phyPlan, phyTaskMap));
                break;
            case "MPCTableScan":
                Task task = basicTask("");
//                Output output = new Output();
                Output outputData = new Output();
                String tableName = phyPlan.getTable().getQualifiedName().get(0);
                outputData.setDataName(tableName);
                outputData.setDataId(tableName);
//                output.setData(List.of(outputData));
                task.setOutputList(List.of(outputData));
                Party party = new Party();
                party.setPartyId(metadata.getTableOrgId(tableName));
                party.setPartyName(metadata.getTable(tableName).getOrgName());
                task.setPartyList(List.of(party));
                phyTaskMap.put(phyPlan, task);
                break;
            default:
                break;
        }

        return tasks;
    }

    private List<String> getInputList(RelNode phyPlan, int i) {
        List<String> fieldNames = phyPlan.getRowType().getFieldNames();
        List<String> fullQualifiedNames = fieldNames.stream()
                .map(x -> fromNumericName2FieldName(phyPlan, x)).collect(Collectors.toList());

        List<String> inputList = new ArrayList<String>(List.of(fullQualifiedNames.get(i).split("\\+|-|\\*|/|%|\\[|]|\\(|\\)|,")));
        // 删除如 SUM[ADATA.A1] 这种split出来剩余 SUM 的情况
        for (int j = 0; j < inputList.size(); j++) {
            inputList.set(j,inputList.get(j).strip());
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
        return inputList;
    }

    public Task generateAggregateTask(MPCAggregate phyPlan, HashMap<RelNode, Task> phyTaskMap){
        Task task = basicTask(String.valueOf(cnt++));
        MPCMetadata mpcMetadata = MPCMetadata.getInstance();

        Module module = new Module();
        module.setModuleName(TaskType.LOCALAGG.name());
        List<ModuleParam> moduleparams = new ArrayList<ModuleParam>();

        Set<String> usedFields = new HashSet<String>();
        ImmutableList<ImmutableBitSet> sets = phyPlan.getGroupSets();
        for(ImmutableBitSet set : sets){
            String fullQualifiedfield = fromNumericName2FieldName(phyPlan, set.toString());
            Pair<String,String> pair = getTableNameAndColumnName(fullQualifiedfield);
            moduleparams.add(new ModuleParam("groupBy", pair.right));
            usedFields.add(fullQualifiedfield);
        }
        List<AggregateCall> calls = phyPlan.getAggCallList();
        List<String> funcList = Lists.newArrayList();
        for(AggregateCall call : calls){
            String callStr = call.toString();
            String funcLiteral = callStr + " as " + getColumnName(call.name);
            if(!call.getArgList().isEmpty()){
                String ref = call.getArgList().get(0).toString();
                String qualifiedName = fromNumericName2FieldName(phyPlan, ref);
                Pair<String,String> pair = getTableNameAndColumnName(qualifiedName);
                String refLiteral = "$" + ref;
                assert pair.right != null;
                funcList.add(funcLiteral.replace(refLiteral, pair.right));
                usedFields.add(qualifiedName);
            }else if("COUNT()".equalsIgnoreCase(callStr)){
                funcLiteral = funcLiteral.replace("()", "(1)");
                funcList.add(funcLiteral);
            }else{
                throw new RuntimeException("illegal agg func: " + callStr);
            }
        }
        String funcStr = String.join(",", funcList);
        moduleparams.add(new ModuleParam("aggFunc", funcStr));
        module.setParamList(moduleparams);
        Input input = new Input();
        List<InputDetail> inputDataList = new ArrayList<>();
        List<Output> outputDataList = new ArrayList<>();

        RelSubset relSubset = (RelSubset)(phyPlan.getInputs().get(0));
        List<RelDataTypeField> inFields =relSubset.getBest().getRowType().getFieldList();
        Task srcTask = phyTaskMap.get(relSubset.getBest());
        for(RelDataTypeField field: inFields){
            InputDetail inputData = new InputDetail();
            String fullQualifiedfield = fromNumericName2FieldName(relSubset.getBest(),field.getName());
            Pair<String, String> pair = getTableNameAndColumnName(fullQualifiedfield);
            if(usedFields.contains(fullQualifiedfield)) {
                inputData.setColumnName(pair.right);
                inputData.setType(field.getType().getFullTypeString());
                TableInfo inputTable = mpcMetadata.getTable(pair.left);

                inputData.setDataName(srcTask.getOutputList().get(0).getDataName());
                inputData.setDataId(inputTable.getAssetName());

                inputData.setAssetName(inputTable.getAssetName());
                inputData.setTableName(inputTable.getName());

                inputData.setDomainId(inputTable.getOrgDId());
                inputData.setDomainName(inputTable.getOrgName());
                inputData.setTaskSrc(srcTask.getTaskName());
                inputDataList.add(inputData);
            }
        }

        List<RelDataTypeField> outFields = phyPlan.getRowType().getFieldList();
        Output outputData = new Output();
        List<String> cols = Lists.newArrayList();
        String tableName = null;
        for(RelDataTypeField field : outFields){
            String fullQualifiedfield = fromNumericName2FieldName(phyPlan,field.getName());
            Pair<String, String> pair = getTableNameAndColumnName(fullQualifiedfield);
            tableName = pair.left;
            cols.add(pair.right);
        }
        outputData.setColumnName(String.join(",", cols));
        outputData.setType("");
        TableInfo inputTable = mpcMetadata.getTable(tableName);
        outputData.setDataName(inputDataList.get(0).getDomainId() + "_" + cnt);  //不能去除，后面有代码依赖这个做判断
        outputData.setDataId("");
        outputData.setDomainId(inputTable.getOrgDId());
        outputData.setDomainName(inputTable.getOrgName());
        outputDataList.add(outputData);

        task.setModule(module);
        input.setInputDataDetailList(inputDataList);
        task.setInput(input);
        task.setOutputList(outputDataList);

        genParties(input,task);
        phyTaskMap.put(phyPlan, task);
        return task;
    }

    public RexCall rmAsOfRexCall(RexCall call){
        String callStr = call.toString();
        int cnt = ReUtil.count("AS\\(", callStr);
        for (int i = 0; i < cnt - 1 ; i++){
            RexNode rexNode = call.getOperands().get(0);
            if (rexNode instanceof  RexCall){
                call = (RexCall) rexNode;
            }
        }
        return call;
    }



    public Task generateProjectTask(MPCProject phyPlan, HashMap<RelNode, Task> phyTaskMap, RexNode rexNode, List<String> inputList) {
        log.info("inputList:" + inputList);
        Task task = basicTask(String.valueOf(cnt++));

        // [AS(+($8, $7), ''), AS(SUM($4), ''), AS($0, '')]
        // 所有 project 默认最上层都是 AS 的 RexCall, 所以去掉一层之后才是真的 proj 的内容
        if(rexNode instanceof RexInputRef){
            RexBuilder rexBuilder = phyPlan.getCluster().getRexBuilder();
            RexNode stringLiteral = rexBuilder.makeLiteral("",
                    rexBuilder.getTypeFactory().createSqlType(SqlTypeName.VARCHAR),
                    false);
            rexNode = rexBuilder.makeCall(new SqlAsOperator(), rexNode, stringLiteral);
        }else if(rexNode instanceof RexLiteral){
            RexLiteral rexLiteral = (RexLiteral)rexNode;
            RexBuilder rexBuilder = phyPlan.getCluster().getRexBuilder();
            RexNode stringLiteral = rexBuilder.makeLiteral(rexLiteral.getValue(),
                    rexBuilder.getTypeFactory().createSqlType(SqlTypeName.VARCHAR),
                    false);
            rexNode = rexBuilder.makeCall(new SqlAsOperator(), rexNode, stringLiteral);
        }else if(rexNode instanceof RexCall){
            rexNode = rmAsOfRexCall((RexCall)rexNode);
        }

        RexCall node = (RexCall)rexNode;
        RexNode proj = node.getOperands().get(0);
        // module信息（即进行什么操作）
        Module module = new Module();

        List<ModuleParam> moduleparams = new ArrayList<ModuleParam>();
        List<String> constantList = new ArrayList<>();
        if (proj instanceof RexCall) {
            SqlOperator op = ((RexCall) proj).getOperator();
            if (op.equals(SqlStdOperatorTable.PLUS) || op.equals(SqlStdOperatorTable.MINUS) ||
                    op.equals(SqlStdOperatorTable.MULTIPLY) || op.equals(SqlStdOperatorTable.DIVIDE) ||
                    op.equals(SqlStdOperatorTable.MOD)) {
                module.setModuleName("EXP");
                moduleparams.add(new ModuleParam("function", "base"));
                String expr = dfsRexNode(proj, constantList);
                moduleparams.add(new ModuleParam("expression", expr));
            } else {
                module.setModuleName("AGG");
                moduleparams.add(new ModuleParam("function", op.toString()));
                String expr = dfsRexNode(((RexCall) proj).getOperands().get(0), constantList);
                moduleparams.add(new ModuleParam("expression", expr));
            }
        } else if (proj instanceof RexInputRef){
            module.setModuleName(TaskType.QUERY.name());
        } else if(proj instanceof RexLiteral){
            module.setModuleName(TaskType.QUERY.name());
        }
        if (!constantList.isEmpty()) {
            String constants = "";
            for (String constant : constantList) {
                constants += constant + ",";
            }
            constants = constants.substring(0, constants.length() - 1);
            moduleparams.add(new ModuleParam("constant", constants));
        }
        module.setParamList(moduleparams);
        task.setModule(checkMpcModule(module));

        // 输入信息
        Input input = new Input();
        List<InputDetail> inputDatas = new ArrayList<>();
        Task srcTask = phyTaskMap.get(((RelSubset) phyPlan.getInput()).getBest());
        for (int i = 0; i < inputList.size(); i++) {
            InputDetail inputdata = new InputDetail();
            String tableField = inputList.get(i);
            inputdata.setTaskSrc(srcTask.getTaskName());
            inputdata.setDomainId(getFieldDomainID(tableField));
            if (srcTask.getTaskName().equals("") ||
                    srcTask.getOutputList().get(0).getDataName().startsWith(inputdata.getDomainId())) {
                inputdata.setDataName(srcTask.getOutputList().get(0).getDataName());
                inputdata.setDataId(srcTask.getOutputList().get(0).getDataId());
            } else {
                inputdata.setDataName(srcTask.getOutputList().get(1).getDataName());
                inputdata.setDataId(srcTask.getOutputList().get(1).getDataId());
            }
            if (i == 0) {
                inputdata.setRole("server");
            } else {
                inputdata.setRole("client");
            }
            JSONObject inputParam = new JSONObject(true);
            String table = tableField.split("\\.")[0];
            String field = tableField.split("\\.")[1];
            inputParam.put("table", table);
            inputParam.put("field", field);
            if (module.getModuleName().equals("EXP") || module.getModuleName().equals("AGG")) {
                inputParam.put("type", columnInfoMap.get(tableField));
                List<Integer> list = new ArrayList<>();
                list.add(i);
                inputParam.put("index", Arrays.toString(list.toArray()));
            }
            inputdata.setParams(inputParam);
            inputdata.setType(columnInfoMap.get(tableField.toUpperCase()));
            TableInfo tableInfo = metadata.getTableInfoMap().get(table);
            inputdata.setTableName(tableInfo.getName());
            FieldInfo fieldInfo = tableInfo.getFields().get(tableField);
            inputdata.setColumnName(fieldInfo.getFieldName());

            //对于中间表，需要找到源头的资产名称。因为中间表不会注册在ida中
            String originTable = getOriginalTableName(table);
            TableInfo originTableInfo = metadata.getTableInfoMap().get(originTable);
            inputdata.setAssetName(originTableInfo.getAssetName());

            inputdata.setDatabaseName(fieldInfo.getDatabaseName());
            inputdata.setComments(fieldInfo.getComments());
            inputdata.setLength(fieldInfo.getDataLength());
            inputdata.setDomainName(fieldInfo.getDomainName());
            inputDatas.add(inputdata);
        }

        input.setInputDataDetailList(inputDatas);
        input.setTaskId(task.getTaskId());
        input.setSrcTaskId(inputDatas.get(0).getTaskSrc());
        input.setSrcTaskName(inputDatas.get(0).getTaskSrc());
        task.setInput(input);

        // 输出信息
//        Output output = new Output();
        Output outputdata = new Output();
        outputdata.setDataName(inputDatas.get(0).getDomainId() + "-" + cnt);
        outputdata.setColumnName(inputDatas.get(0).getColumnName());
        outputdata.setLength(inputDatas.get(0).getLength());
        outputdata.setType(inputDatas.get(0).getType());
        outputdata.setFinalResult("Y");
        outputdata.setIsFinalResult(true);
        outputdata.setDomainId(inputDatas.get(0).getDomainId());
        outputdata.setDomainName(inputDatas.get(0).getDomainName());
        outputdata.setDataId("");

//        output.setData(List.of(outputdata));
        task.setOutputList(List.of(outputdata));

        // parties信息
        List<Party> parties = genParties(input, task);

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

        //以为project是一个字段一个任务，所以input比较简单。对于LocalAgg来说。做个转换就可以了。
        if(parties.size() == 1 && module.getModuleName().equals(TaskType.LOCALAGG.name())) {
            InputDetail inputData = input.getInputDataDetailList().get(0);
            JSONObject json = inputData.getParams();
            String field = json.getString("field");
            String func = String.valueOf(moduleparams.stream()
                    .filter(x -> x.getKey().equals("function"))
                    .findAny().get().getValue()
            );
            moduleparams.add(new ModuleParam("aggFunc", StrUtil.format("{}({})",func, field)));
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
    public String dfsRexNode(RexNode proj, List<String> constants) {
        String expr = "";
        if (proj instanceof RexCall) {
            String tmpl = dfsRexNode(((RexCall) proj).getOperands().get(0), constants);
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
            String tmpr = dfsRexNode(((RexCall) proj).getOperands().get(1), constants);
            if (tmpr.length() > 1 && tmpr.contains("x")) {
                expr += "(" + tmpr + ")";
            } else {
                expr += tmpr;
            }

        } else if (proj instanceof RexInputRef) {
            expr += "x";
        } else if (proj instanceof RexLiteral) {
            expr += "c";
            constants.add(((RexLiteral) proj).getValue().toString());
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
        InputDetail inputdata1 = new InputDetail(), inputdata2 = new InputDetail();
        JSONObject inputData1Params = new JSONObject(true);
        JSONObject inputData2Params = new JSONObject(true);

        String leftField = phyPlan.getRowType().getFieldNames().get(((RexInputRef) cond.getOperands().get(0)).getIndex());
        String rightField = phyPlan.getRowType().getFieldNames().get(((RexInputRef) cond.getOperands().get(1)).getIndex());
        Task leftChild = phyTaskMap.get(((RelSubset) phyPlan.getLeft()).getBest());
//        System.out.println(((RelSubset) phyPlan.getRight()).getBest() instanceof MPCFilter);
        Task rightChild = phyTaskMap.get(((RelSubset) phyPlan.getRight()).getBest());

        inputdata1.setTaskSrc(leftChild.getTaskName());
        inputdata1.setRole("client");
        inputdata1.setDomainId(getFieldDomainID(leftField));
        inputdata1.setDomainName(getFieldInfo(leftField).getDomainName());
        if (leftChild.getTaskName().equals("")) {
            inputdata1.setDataName(leftField.split("\\.")[0]);
            inputdata1.setDataId(leftField.split("\\.")[0]);
        } else {
            if (leftChild.getOutputList().get(0).getDomainId().equals(inputdata1.getDomainId())) {
                inputdata1.setDataName(leftChild.getOutputList().get(0).getDataName());
                inputdata1.setDataId(leftChild.getOutputList().get(0).getDataId());
            } else {
                inputdata1.setDataName(leftChild.getOutputList().get(1).getDataName());
                inputdata1.setDataId(leftChild.getOutputList().get(1).getDataId());
            }
        }

        String leftTable = leftField.split("\\.")[0];
        String leftTableField = leftField.split("\\.")[1];
        inputData1Params.put("table", leftTable);
        inputData1Params.put("field", leftTableField);
        inputdata1.setType(columnInfoMap.get(leftField.toUpperCase()));
        TableInfo leftTableInfo = metadata.getTableInfoMap().get(leftTable);
        inputdata1.setTableName(leftTableInfo.getName());

        String leftOriginTable = getOriginalTableName(leftTableInfo.getAssetName());
        TableInfo leftOriginTableInfo = metadata.getTable(leftOriginTable);
        inputdata1.setAssetName(leftOriginTableInfo.getAssetName());

        FieldInfo leftFieldInfo = leftTableInfo.getFields().get(leftField);
        inputdata1.setColumnName(leftFieldInfo.getFieldName());
        inputdata1.setDatabaseName(leftFieldInfo.getDatabaseName());
        inputdata1.setComments(leftFieldInfo.getComments());
        inputdata1.setLength(leftFieldInfo.getDataLength());
        inputdata1.setParams(inputData1Params);

        inputdata2.setTaskSrc(rightChild.getTaskName());
        inputdata2.setRole("server");
        inputdata2.setDomainId(getFieldDomainID(rightField));
        inputdata2.setDomainName(getFieldInfo(rightField).getDomainName());
        if (rightChild.getTaskName().equals("")) {
            inputdata2.setDataName(rightField.split("\\.")[0]);
            inputdata2.setDataId(rightField.split("\\.")[0]);
        } else {
            if (rightChild.getOutputList().get(0).getDomainId().equals(inputdata2.getDomainId())) {
                inputdata2.setDataName(rightChild.getOutputList().get(0).getDataName());
            } else {
                inputdata2.setDataName(rightChild.getOutputList().get(1).getDataName());
                inputdata2.setDataId(rightChild.getOutputList().get(1).getDataId());
            }
        }

        String rightTable = rightField.split("\\.")[0];
        String rightTableField = rightField.split("\\.")[1];
        inputData2Params.put("table", rightTable);
        inputData2Params.put("field", rightTableField);
        inputdata2.setType(columnInfoMap.get(rightField.toUpperCase()));
        TableInfo rightTableInfo = metadata.getTableInfoMap().get(rightTable);
        inputdata2.setTableName(rightTableInfo.getName());

        String rightOriginTable = getOriginalTableName(rightTableInfo.getAssetName());
        TableInfo rightOriginTableInfo = metadata.getTable(rightOriginTable);
        inputdata2.setAssetName(rightOriginTableInfo.getAssetName());

        FieldInfo rightFieldInfo = rightTableInfo.getFields().get(rightField);
        inputdata2.setColumnName(rightFieldInfo.getFieldName());
        inputdata2.setDatabaseName(rightFieldInfo.getDatabaseName());
        inputdata2.setComments(rightFieldInfo.getComments());
        inputdata2.setLength(rightFieldInfo.getDataLength());
        inputdata2.setParams(inputData2Params);

        input.setInputDataDetailList(List.of(inputdata1, inputdata2));
        input.setTaskId(task.getTaskId());
        input.setSrcTaskName(inputdata1.getTaskSrc());
        input.setSrcTaskId(inputdata1.getTaskSrc());
        task.setInput(input);

        // parties信息
        genParties(input, task);

        // 输出信息
//        Output output = new Output();

        Output outputdata1 = new Output();
        Output outputdata2 = new Output();

        outputdata1.setDataName(inputdata1.getDomainId() + "-" + cnt);
        outputdata1.setType(inputdata1.getType());
        outputdata1.setLength(inputdata1.getLength());
        outputdata1.setColumnName(inputdata1.getColumnName());
        outputdata1.setFinalResult("N");
        outputdata1.setDomainId(inputdata1.getDomainId());
        outputdata1.setDomainName(inputdata1.getDomainName());
        outputdata1.setDataId("");

        outputdata2.setDataName(inputdata2.getDomainId() + "-" + cnt);
        outputdata2.setType(inputdata2.getType());
        outputdata2.setLength(inputdata2.getLength());
        outputdata2.setColumnName(inputdata2.getColumnName());
        outputdata2.setFinalResult("N");
        outputdata2.setDomainId(inputdata2.getDomainId());
        outputdata2.setDomainName(inputdata2.getDomainName());
        outputdata2.setDataId("");

        List<Party> parties = genParties(input,task);

        if (parties.size() == 1) {
//            output.setData();
            task.setOutputList(List.of(outputdata1));
        } else {
//            output.setData();
            task.setOutputList(List.of(outputdata1, outputdata2));
        }



        phyTaskMap.put(phyPlan, task);

        return task;
    }

    public Task generateFilterTask(MPCFilter phyPlan, HashMap<RelNode, Task> phyTaskMap, RexCall cond) {
        Task task = basicTask(String.valueOf(cnt++));

        // module信息（即进行什么操作）
        Module module = new Module();
        module.setModuleName(TaskType.LOCALFILTER.name());
//        JSONObject moduleparams = new JSONObject(true);
        List<ModuleParam> moduleParams = new ArrayList<ModuleParam>();

        moduleParams.add(new ModuleParam("operator", cond.getOperator().toString()));
        RexLiteral constant;
        RexInputRef field;
        if (cond.getOperands().get(0) instanceof RexLiteral) {
            constant = (RexLiteral) cond.getOperands().get(0);
            field = (RexInputRef) cond.getOperands().get(1);
        } else {
            constant = (RexLiteral) cond.getOperands().get(1);
            field = (RexInputRef) cond.getOperands().get(0);
        }
        if (constant.getType().getSqlTypeName().equals(SqlTypeName.CHAR)) {
            moduleParams.add(new ModuleParam("constant", RexLiteral.stringValue(constant)));
        }else {
            moduleParams.add(new ModuleParam("constant", String.valueOf(constant.getValue())));
        }

        String tableField = phyPlan.getRowType().getFieldNames().get(field.getIndex());

        module.setParamList(moduleParams);
        task.setModule(module);


        // 输入信息
        Input input = new Input();
        InputDetail inputdata = new InputDetail();
        Task childTask;
        if (phyTaskMap.containsKey(phyPlan)) {
            childTask = phyTaskMap.get(phyPlan);
        } else {
            childTask = phyTaskMap.get(((RelSubset) phyPlan.getInput()).getBest());
        }
        String table = tableField.split("\\.")[0];
        inputdata.setTaskSrc(childTask.getTaskName());
        inputdata.setDataName(childTask.getOutputList().get(0).getDataName());
        inputdata.setDomainId(getFieldDomainID(tableField));
        inputdata.setType(columnInfoMap.get(tableField.toUpperCase()));

        TableInfo tableInfo = metadata.getTableInfoMap().get(table);
        inputdata.setAssetName(tableInfo.getAssetName());
        FieldInfo fieldInfo = tableInfo.getFields().get(tableField);
        inputdata.setTableName(tableInfo.getName());
        inputdata.setColumnName(fieldInfo.getFieldName());
        inputdata.setDatabaseName(fieldInfo.getDatabaseName());
        inputdata.setComments(fieldInfo.getComments());
        inputdata.setLength(fieldInfo.getDataLength());
        inputdata.setDataId(childTask.getOutputList().get(0).getDataId());
        inputdata.setDomainName(tableInfo.getOrgName());
        inputdata.setRole("server");
        JSONObject inputParam = new JSONObject(true);
        inputParam.put("table", tableField.split("\\.")[0]);
        inputParam.put("field", tableField.split("\\.")[1]);
        inputdata.setParams(inputParam);
        input.setInputDataDetailList(List.of(inputdata));
        input.setTaskId(task.getTaskId());
        input.setSrcTaskName(inputdata.getTaskSrc());
        input.setSrcTaskId(inputdata.getTaskSrc());
        task.setInput(input);

        // 输出信息
//        Output output = new Output();
        Output outputdata = new Output();
        outputdata.setDataName(inputdata.getDomainId() + "-" + cnt);
        outputdata.setColumnName(inputdata.getColumnName());
        outputdata.setLength(inputdata.getLength());
        outputdata.setType(inputdata.getType());
        outputdata.setFinalResult("N");
        outputdata.setDomainId(inputdata.getDomainId());
        outputdata.setDomainName(inputdata.getDomainName());
        outputdata.setDataId("");

//        output.setData(List.of(outputdata));
        task.setOutputList(List.of(outputdata));

        // parties信息
        genParties(input, task);
        phyTaskMap.put(phyPlan, task);
        return task;
    }

    public List<Party> genParties(Input input, Task task){
        List<Party> parties = new ArrayList<>();
        for (InputDetail inputData : input.getInputDataDetailList()) {
            Party party = new Party();
            party.setServerInfo(null);
            party.setStatus(null);
            party.setTimestamp(null);
            party.setPartyId(inputData.getDomainId());
            party.setPartyName(inputData.getDomainName());
            parties.add(party);
        }
        parties = parties.stream().filter(StreamUtils.distinctByKey(Party::getPartyId)).collect(Collectors.toList());
        task.setPartyList(parties);
        jobPartySet.addAll(parties);
        return parties;
    }
    public Module checkPSIModule(MPCJoin phyPlan) {
        Module module = new Module();
        RexCall cond = (RexCall) phyPlan.getCondition();
        String moduleName = "";
//        JSONObject moduleparams = new JSONObject(true);
        List<ModuleParam> moduleParams = new ArrayList<ModuleParam>();
        moduleParams.add(new ModuleParam("joinType", phyPlan.getJoinType().toString()));
        moduleParams.add(new ModuleParam("operator", cond.getOperator().toString()));
        module.setParamList(moduleParams);

        if (hint != null) {
            for (HintExpression kv : hint.getValues()) {
                if (kv.getKey().equals("JOIN") && kv.getValues().get(0).equals("TEE")) {
                    moduleName = TaskType.TEEPSI.name();
                    module.setModuleName(moduleName);
                    module.getParamList().add(new ModuleParam("teeHost", "192.168.40.21"));
                    module.getParamList().add(new ModuleParam("teePort", "30091"));
                    module.getParamList().add(new ModuleParam("domainID", "wx-org3.chainmaker.orgDID"));
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
                if (kv.getKey().equals("FUNC") && kv.getValues().get(0).equals("TEE")) {
                    temp.setModuleName(TaskType.TEE.name());
                    temp.getParamList().add(new ModuleParam("methodName", modelList.get(0)));
                    temp.getParamList().add(new ModuleParam("teeHost", "192.168.40.21"));
                    temp.getParamList().add(new ModuleParam("teePort", "30091"));
                    temp.getParamList().add(new ModuleParam("domainID", "wx-org3.chainmaker.orgDID"));
                    temp.deleteByKey("function");
                    temp.deleteByKey("expression");
                    System.out.println("module:" + temp);
                    return temp;
                }
            }
        }
        return temp;
    }

    public void logicalHintFix(Task task) {
        List<HintExpression> list = hint.getValues();
        for (HintExpression kv : list) {
            if (kv.getKey().equals("JOIN")) {
                List<String> values = kv.getValues();
                task.getModule().setModuleName(TaskType.TEEPSI.name());
                List<ModuleParam> params = task.getModule().getParamList();
                params.add(new ModuleParam("teeHost", "192.168.40.230"));
                params.add(new ModuleParam("teePort", "30091"));
                params.add(new ModuleParam("domainID", ""));
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
//        task.setStatus(taskStatus);
        task.setStatus(Constant.TASK_STATUS);
        task.setJobId(jobID);
        task.setTaskName(taskName);
        task.setTaskId(taskName);
        return task;
    }

    public FieldInfo getFieldInfo(String fieldName) {
        return metadata.getFieldInfo(fieldName);
    }

    public String getFieldDomainID(String fieldName) {return getFieldInfo(fieldName).getDomainID();}


//    public void searchAggInfo(XPCPlan xpcPlan, AggregateCall call){
//        if(xpcPlan instanceof XPCAggregate){
//            XPCAggregate xpcAggregate = (XPCAggregate) xpcPlan;
//            List<NamedExpression> exprs = xpcAggregate.getAggCallList();
//
//            for(NamedExpression expr : exprs){
//                System.out.println(expr);
//            }
//        }else{
//        List<XPCPlan> children = xpcPlan.getChildren();
//        for(XPCPlan child : children){
//            searchAggInfo(child,call);
//        }
//    }
//    }
    private void multipartyPsi() {
        HashMap<String, InputDetail> inputMap = new HashMap<>();
        HashMap<String, Output> outputMap = new HashMap<>();
        int flag = 0, count = 0;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.getModule().getModuleName().equals("TEEPSI")) {
                for (int j = 0; j < task.getInput().getInputDataDetailList().size(); j++) {
                    InputDetail taskInputData = task.getInput().getInputDataDetailList().get(j);
                    Output taskOutputData = task.getOutputList().get(j);
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
        List<InputDetail> inputData = new ArrayList<>(inputMap.values());
        input.setSrcTaskName(inputData.get(0).getTaskSrc());
        input.setSrcTaskId(inputData.get(0).getTaskSrc());
        input.setInputDataDetailList(inputData);

//        Output output = new Output();
        List<Output> outputData = new ArrayList<>(outputMap.values());
//        output.setData(outputData);
        if (tasks.get(flag).getModule().getModuleName().equals("TEEPSI")) {
            input.setTaskId(tasks.get(flag).getTaskId());
            tasks.get(flag).setInput(input);
            tasks.get(flag).setOutputList(outputData);
            List<Party> parties = new ArrayList<>();
            for (InputDetail taskInputData : input.getInputDataDetailList()) {
                Party party = new Party();
                party.setServerInfo(null);
                party.setStatus(null);
                party.setTimestamp(null);
                party.setPartyId(taskInputData.getDomainId());
                party.setPartyName(taskInputData.getDomainName());
                parties.add(party);
            }
            parties = parties.stream().filter(StreamUtils.distinctByKey(Party::getPartyId)).collect(Collectors.toList());
            tasks.get(flag).setPartyList(parties);
        }
    }
}
