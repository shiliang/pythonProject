package com.chainmaker.jobservice.api.builder;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.Constant;
import com.chainmaker.jobservice.api.enums.JobType;
import com.chainmaker.jobservice.api.model.*;
import com.chainmaker.jobservice.api.model.job.Job;
import com.chainmaker.jobservice.api.model.job.service.*;
import com.chainmaker.jobservice.api.model.job.task.*;
import com.chainmaker.jobservice.api.model.job.task.Module;
import com.chainmaker.jobservice.api.model.vo.*;
import com.chainmaker.jobservice.core.optimizer.model.FL.FlInputData;
import com.chainmaker.jobservice.core.optimizer.model.InputData;
import com.chainmaker.jobservice.core.optimizer.model.OutputData;
import com.chainmaker.jobservice.core.optimizer.model.SpdzInputData;
import com.chainmaker.jobservice.core.optimizer.model.TeeModel;
import com.chainmaker.jobservice.core.optimizer.nodes.DAG;
import com.chainmaker.jobservice.core.optimizer.nodes.Node;
import com.chainmaker.jobservice.core.optimizer.plans.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author gaokang
 * @date 2022-09-22 14:29
 * @description:生成Job和任务图
 * @version:1.0.0
 */

public class JobBuilder extends PhysicalPlanVisitor {

    private enum TaskType {
        QUERY, PIR, PSI, MPC, TEE, FL, MPCEXP
    }
    private final String orgID;
    private final String orgName;
    private final Integer isStream;
    private final DAG<PhysicalPlan> dag;
    private final String createTime, jobID;
    private Job job = new Job();
    private HashMap<String, String> kvMap = new HashMap<>();
    private List<Service> services = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();

    private List<Party> jobPartyList = new ArrayList<>();

    private Integer templateId = 1;
    private String sql;

    private SqlVo sqlVo;

    private List<Pair> setClausePair;


    public JobBuilder(SqlVo sqlVo, DAG<PhysicalPlan> dag) {
        this.sqlVo = sqlVo;
        this.setClausePair = SetClauseParser.clauses2Pairs(sqlVo.getSetClauses());
        this.isStream = sqlVo.getIsStream();
        this.dag = dag;
        this.createTime = String.valueOf(System.currentTimeMillis());
        this.jobID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        this.orgID = sqlVo.getOrgInfo().getOrgId();
        this.orgName = sqlVo.getOrgInfo().getOrgName();
        this.sql = sqlVo.getExecuteSql();
    }

    public Job getJob() {
        job.setServiceList(services);
        job.setTaskList(tasks);
        return job;
    }



    public void build() {
        Integer jobStatus = 10;
        String taskDAG = "taskDAG";
        Integer jobType;
        if (this.sql.contains("FL")) {
            jobType = JobType.FL.getValue();
        } else if (this.sql.contains("TEE")) {
            jobType = JobType.TEE.getValue();
        } else {
            jobType = JobType.MPC.getValue();
        }
        for (Node<PhysicalPlan> next : dag.getNodes()) {
            next.getObject().accept(this);
        }
        buildService();

        //支持工行项目的专用版
        if (sql.contains("TEE")) {
            List<Task> serviceTasks = Lists.newArrayList(tasks);
            Value value = new Value("taskLists", serviceTasks);
            services.forEach(x -> x.getExposeEndpointList().get(0).setValueList(Lists.newArrayList(value)));
            tasks.clear();
        } else if (!tasks.isEmpty()) {
            supportGonghang();
        }

        job.setJobId(jobID);
        job.setModelType(jobType);
        job.setStatus(Constant.JOB_STATUS);
        job.setCreateTime(createTime);
        job.setUpdateTime(createTime);
        job.setTasksDAG(taskDAG);
        job.setSubmitter(orgID);
        job.setCreateUserId(orgID);
        job.setCreatePartyId(orgID);
        job.setCreatePartyName(orgName);
        job.setRequestData(sql);
        job.setPartyList(jobPartyList);
    }

    public void supportGonghang(){
        Task mpcTask = tasks.stream().filter(x -> x.getModule().getModuleName().equals(TaskType.MPCEXP.name())).findAny().get();
        Task pirTask = tasks.stream().filter(x -> x.getModule().getModuleName().equals(TaskType.PIR.name())).findAny().get();
        Value value1 = new Value("inputParams", StringEscapeUtils.unescapeJson(JSON.toJSONString(mpcTask.getInput())));
        List<JSONObject> idParams = pirTask.getInput().getInputDataDetailList()
                .stream()
                .map(x -> {
                    JSONObject obj = new JSONObject();
                    obj.put("table", x.getAssetName());
                    obj.put("field", x.getColumnName());
                    obj.put("variable", x.getParams().get("variable"));
                    obj.put("domainId", x.getDomainId());
                    obj.put("domainName", x.getDomainName());
                    return obj;
                }).collect(Collectors.toList());
        Value value2 = new Value("idParams", StringEscapeUtils.unescapeJson(JSON.toJSONString(idParams)));
        Value value3 = new Value("mpcParams", StringEscapeUtils.unescapeJson(JSON.toJSONString(mpcTask.getModule())));
        List<Value> valueLists = Lists.newArrayList(value1, value2, value3);
        String serviceStr = JSON.toJSONString(services);
        String newServiceStr = serviceStr.replace("PirClient", "MpcClient").replace("PirServer", "MpcServer");
        services = JSONObject.parseArray(newServiceStr, Service.class);
        for (Service service : services) {
            service.setServiceName(service.getServiceClass());
            service.setServiceLabel(service.getServiceClass());
            service.getExposeEndpointList().get(0).setValueList(valueLists);
        }
        tasks.clear();
    }

    public List<Party> partiesFromDag(){
        Set<Party> parties = Sets.newHashSet();
        for(Node<PhysicalPlan> node: dag.getRoots()){
            PhysicalPlan plan = node.getObject();
            if(plan instanceof  TableScan){
                TableScan tableScan = (TableScan)plan;
                for(OutputData output : tableScan.getOutputDataList()){
                    if(output.getDomainID() != null){
                        Party party = new Party();
                        party.setPartyId(output.getDomainID());
                        party.setPartyName(output.getDomainName());
                        parties.add(party);
                    }
                }
            }
        }
        return Lists.newArrayList(parties);
    }


    @Override
    public void visit(Project plan) {
        if (sqlVo.getIsStream() != 1) {
            String moduleName = TaskType.QUERY.name();
            Task task = basePlanToTask(plan);
            Module module = new Module();
            module.setModuleName(moduleName);
            List<ModuleParam> moduleParams = new ArrayList<>();
            moduleParams.add(new ModuleParam("alias", plan.getOutputDataList().get(0).getOutputSymbol()));
//            JSONObject param = new JSONObject();
//            param.put("alias", plan.getOutputDataList().get(0).getOutputSymbol());
            module.setParamList(moduleParams);
            task.setModule(module);
            tasks.add(task);
        }
    }
    @Override
    public void visit(PirFilter plan) {
        if(sql.contains("TEE")) {
            templateId = 3;
        }else{
            templateId = 2;
        }
        String moduleName = TaskType.PIR.name();
        Task task = basePlanToTask(plan);
        Module module = new Module();
        module.setModuleName(moduleName);
        task.setModule(module);
        tasks.add(task);
    }

    private static List<ExposeEndpoint> getExposeEndpoints(ServiceVo serviceVo, String partyId, String partyName) {
        List<ExposeEndpoint> exposeEndpointList = new ArrayList<>();
        for (ExposeEndpointVo exposeEndpointVo : serviceVo.getExposeEndpoints()) {
            ExposeEndpoint exposeEndpoint = new ExposeEndpoint();
//                    exposeEndpoint.setServiceClass(exposeEndpointVo);
            HashMap<String, String> exposeEndpointFormMap = new HashMap<>();
            for (ExposeFormVo exposeFormVo : exposeEndpointVo.getForm()) {
                exposeEndpointFormMap.put(exposeFormVo.getKey(), exposeFormVo.getValues());
            }
            exposeEndpoint.setPartyName(partyName);
            exposeEndpoint.setPartyId(partyId);
            exposeEndpoint.setDescription(exposeEndpointFormMap.get("description"));
            exposeEndpoint.setTlsEnabled(Boolean.valueOf(exposeEndpointFormMap.get("tlsEnabled")));
            exposeEndpoint.setServiceCa(exposeEndpointFormMap.get("serviceCa"));
            exposeEndpoint.setServiceCert(exposeEndpointFormMap.get("serviceCert"));
            exposeEndpoint.setServiceKey(exposeEndpointFormMap.get("serviceKey"));
            exposeEndpoint.setProtocol(exposeEndpointFormMap.get("protocol"));
            exposeEndpoint.setMethod(exposeEndpointFormMap.get("method"));
            exposeEndpoint.setAddress(exposeEndpointFormMap.get("address"));
            exposeEndpoint.setPath(exposeEndpointFormMap.get("path"));
            exposeEndpoint.setName(exposeEndpointVo.getName());
            exposeEndpoint.setServiceClass(serviceVo.getServiceClass());
            ValueVo valueVo = serviceVo.getValues().get(0);
            List<Value> valueList = new ArrayList<>();
            if (!StringUtils.isEmpty(valueVo.getKey())){
                Value value = new Value();
                value.setKey(valueVo.getKey());
                value.setValue(valueVo.getValue());
                valueList.add(value);
            }
            exposeEndpoint.setValueList(valueList);
            exposeEndpointList.add(exposeEndpoint);
        }
        return exposeEndpointList;
    }

    private static List<ReferExposeEndpoint> getReferExposeEndpoints(ServiceVo serviceVo, HashMap<String, String> map) {
        List<ReferExposeEndpoint> referExposeEndpointList = new ArrayList<>();
        for (ReferEndpoint referEndpoint : serviceVo.getReferEndpoints()) {
            referEndpoint.setReferServiceID(map.get(referEndpoint.getName()));
            ReferExposeEndpoint referExposeEndpoint = new ReferExposeEndpoint();
            referExposeEndpoint.setReferServiceId(map.get(referEndpoint.getName()));
            referExposeEndpoint.setReferEndpointName(referEndpoint.getReferEndpointName());
            referExposeEndpoint.setProtocol(referEndpoint.getProtocol());
            referExposeEndpoint.setId(referEndpoint.getName());
            referExposeEndpoint.setName(referEndpoint.getName());
            referExposeEndpointList.add(referExposeEndpoint);
        }
        return referExposeEndpointList;
    }

    @Override
    public void visit(TableScan plan) {
//        System.out.println("TableScan");
    }
    @Override
    public void visit(SpdzMpc plan) {
        String moduleName = TaskType.MPCEXP.name();
        Task task =  basePlanToTask(plan);

        Module module = new Module();
        module.setModuleName(moduleName);
        List<ModuleParam> moduleParams = new ArrayList<>();
        moduleParams.add(new ModuleParam("expression", plan.getExpression()));
        if(StrUtil.isNotEmpty(plan.getConstants())) {
            moduleParams.add(new ModuleParam("constants", plan.getConstants()));
        }
        if(StrUtil.isNotEmpty(plan.getVariables())){
            moduleParams.add(new ModuleParam("variables", plan.getVariables()));
        }
        if(StrUtil.isNotEmpty(plan.getAggregateType())) {
            moduleParams.add(new ModuleParam("function", plan.getAggregateType()));
        }else{
            moduleParams.add(new ModuleParam("function", "base"));
        }

        module.setParamList(moduleParams);
        task.setModule(module);

        tasks.add(task);
    }
    @Override
    public void visit(TeeMpc plan) {
//        templateId = 1;
        String moduleName = TaskType.TEE.name();
        Task task = basePlanToTask(plan);
        Module module = new Module();
        module.setModuleName(moduleName);
        List<ModuleParam> moduleParams = new ArrayList<>();
        TeeModel teeModel = plan.getTeeModel();
        if(teeModel != null) {
            moduleParams.add(new ModuleParam("methodName", plan.getTeeModel().getMethodName()));
            moduleParams.add(new ModuleParam("domainID", plan.getTeeModel().getDomainID()));
            Map<String, String> model_method = new HashMap<>();
            model_method.put("method_name", plan.getTeeModel().getMethodName());
            job.setCommon(model_method);
        }else {
            moduleParams.add(new ModuleParam("expression", plan.getExpression()));
            if(StrUtil.isNotEmpty(plan.getConstants())) {
                moduleParams.add(new ModuleParam("constants", plan.getConstants()));
            }
            if(StrUtil.isNotEmpty(plan.getVariables())){
                moduleParams.add(new ModuleParam("variables", plan.getVariables()));
            }
            moduleParams.add(new ModuleParam("function", "base"));
        }
        module.setParamList(moduleParams);
        task.setModule(module);
        tasks.add(task);
    }

    private void buildService() {
        HashMap<String, String> map = new HashMap<>();
        List<ServiceVo> serviceVos = new ArrayList<>();
        List<String> templateTypes = Lists.newArrayList("DataSourceAllInOne4RISC", "PrivacyCompute4RISC", "SuccessDownstream4RISC", "FailureDownstream4RISC");
        if(templateId == 2){
            templateTypes = Lists.newArrayList("PirClient4Query", "PirServer4Query");
        }else if(templateId == 3){
            templateTypes = Lists.newArrayList("TeePirClient4Query", "TeePirServer4Query");
        }
        for (int i=0; i<templateTypes.size(); i++) {
            String templateType = templateTypes.get(i);
            ServiceVo serviceVo = TemplateUtils.buildServiceVo(templateId, templateType, i, createTime);
            map.put(serviceVo.getExposeEndpoints().get(0).getName(), serviceVo.getServiceId());
            serviceVos.add(serviceVo);
        }
        vo2Service(serviceVos, map);
    }

    private void vo2Service(List<ServiceVo> serviceVos, HashMap<String, String> map) {
        List<Party> parties = partiesFromDag();
        for (ServiceVo serviceVo : serviceVos) {
            Service service = new Service();
            BeanUtils.copyProperties(serviceVo, service);
            int idx = Math.min(serviceVos.indexOf(serviceVo), parties.size() -1);
            Party party = parties.get(idx);
            service.setPartyId(party.getPartyId());
            service.setPartyName(party.getPartyName());
            List<ReferExposeEndpoint> referExposeEndpointList = getReferExposeEndpoints(serviceVo, map);
            List<ExposeEndpoint> exposeEndpointList = getExposeEndpoints(serviceVo, service.getPartyId(), service.getPartyName());
            service.setExposeEndpointList(exposeEndpointList);
            service.setReferExposeEndpointList(referExposeEndpointList);
            services.add(service);
        }
    }

    @Override
    public void visit(Fate plan) {
        String moduleName = TaskType.FL.name();
        Task task = basePlanToTask(plan);
        Module module = new Module();
        module.setModuleName(moduleName);
        List<ModuleParam> moduleParams = new ArrayList<>();
        moduleParams.add(new ModuleParam("FL", plan.getFateModel().getFl()));
        moduleParams.add(new ModuleParam("EVAL", plan.getFateModel().getEval()));
        moduleParams.add(new ModuleParam("MODEL", plan.getFateModel().getEval()));
        moduleParams.add(new ModuleParam("INTERSECTION", plan.getFateModel().getIntersection()));
        module.setParamList(moduleParams);
        task.setModule(module);
        for (int i=0; i<plan.getInputDataList().size(); i++) {
            FlInputData flInputData = (FlInputData) plan.getInputDataList().get(i);
            task.getInput().getInputDataDetailList().get(i).setRole(flInputData.getRole());
            JSONObject inputParam = new JSONObject();
            inputParam.put("label_type", flInputData.getLabel_type());
            inputParam.put("output_format", flInputData.getOutput_format());
            inputParam.put("with_label", flInputData.getWith_label());
            inputParam.put("namespace", flInputData.getNamespace());
            inputParam.put("table", flInputData.getTableName());

            task.getInput().getInputDataDetailList().get(i).setParams(inputParam);
        }
        tasks.add(task);
    }


    /***
     * @description 先做通用的转换，由执行计划生成对应的任务，之后不同类型任务各自填充
     * @param plan
     * @return com.chainmaker.jobservice.api.model.domain.job.task.Task
     * @author gaokang
     * @date 2022/8/24 15:10
     */
    private Task basePlanToTask(PhysicalPlan plan) {
        Task task = new Task();
        String taskName = String.valueOf(plan.getId());
        String taskVersion = "1.0.0";
        Integer taskStatus = 0;
        task.setVersion(taskVersion);
        task.setCreateTime(createTime);
        task.setUpdateTime(createTime);
        task.setStatus(Constant.TASK_STATUS);
        task.setJobId(jobID);
        task.setTaskName(taskName);
        task.setTaskId(taskName);

        Input input = new Input();
        List<InputDetail> taskInputDataList = new ArrayList<>();
        String srcTaskName = "";
        for (int i=0; i<plan.getInputDataList().size(); i++) {
            InputData inputData = plan.getInputDataList().get(i);
            String assetName = inputData.getAssetName();
//            String assetName = inputData.getTableName().toLowerCase();

            InputDetail inputDetail = new InputDetail();
            inputDetail.setDataName(assetName);
            inputDetail.setDomainId(inputData.getDomainID());
            inputDetail.setDomainName(inputData.getDomainName());
            if (inputData.getNodeSrc() != null) {
                inputDetail.setTaskSrc(String.valueOf(inputData.getNodeSrc()));
                inputDetail.setDataName(assetName + "-" + inputData.getNodeSrc());
            } else {
                inputDetail.setDataId(assetName);
            }
            inputDetail.setAssetName(inputData.getAssetName());

            JSONObject inputParam = new JSONObject();
            inputParam.put("table", assetName);
            inputParam.put("field", inputData.getColumn().toLowerCase());
            inputParam.put("index", inputData.getIndex());
            for(Pair pair: setClausePair){
                String key = pair.getKey();
                String value = pair.getValue().toString();
                if(plan instanceof PirFilter){
                    PirFilter pirFilter = (PirFilter) plan;
                    String condition = pirFilter.getCondition().toString();
                    if(value.equalsIgnoreCase("?") && condition.contains(key)){
                        inputParam.put("variable", key);
                    }
                }
                if(key.contains("noise")){
                    String table = key.split("\\.")[0];
                    String field = key.split("\\.")[1];
                    if(table.equalsIgnoreCase(inputData.getTableName()) && field.equalsIgnoreCase(inputData.getColumn())){
                        inputParam.put("noise", JSON.parseObject(StringEscapeUtils.unescapeJson(pair.getValue().toString())));
                    }
                }
            }
            inputDetail.setParams(inputParam);
            taskInputDataList.add(inputDetail);
        }
        input.setInputDataDetailList(taskInputDataList);
        input.setTaskId(taskName);
        task.setInput(input);

        List<Output> taskOutputDataList = new ArrayList<>();
        for (int i=0; i<plan.getOutputDataList().size(); i++) {
            OutputData outputData = plan.getOutputDataList().get(i);
            Output taskOutputData = new Output();
            if(StrUtil.isNotEmpty(outputData.getTableName())) {
                taskOutputData.setDataName(outputData.getTableName().toLowerCase() + "-" + plan.getId());
            }
            taskOutputData.setDomainId(outputData.getDomainID());
            if (plan.isFinalResult()) {
                taskOutputData.setFinalResult("Y");
            }
            taskOutputDataList.add(taskOutputData);
        }
        task.setOutputList(taskOutputDataList);

        Set<Party> parties = Sets.newHashSet();
        for (InputDetail inputDetail : taskInputDataList) {
            Party party = new Party();
            party.setPartyId(inputDetail.getDomainId());
            party.setPartyName(inputDetail.getDomainName());
            parties.add(party);
        }
        task.setPartyList(Lists.newArrayList(parties));
        return task;
    }
}
