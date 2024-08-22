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
import com.chainmaker.jobservice.core.optimizer.nodes.DAG;
import com.chainmaker.jobservice.core.optimizer.nodes.Node;
import com.chainmaker.jobservice.core.optimizer.plans.*;
import com.google.common.collect.Lists;
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
        QUERY, PSI, MPC, TEE, FL, MPCEXP
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
        }else if (this.sql.contains("TEE")) {
            jobType = JobType.TEE.getValue();
        }else {
            jobType = JobType.MPC.getValue();
        }
        for (Node<PhysicalPlan> next : dag.getNodes()) {
            next.getObject().accept(this);
        }


        if(!tasks.isEmpty()){
            Value value1 = new Value("inputParams", StringEscapeUtils.unescapeJson(JSON.toJSONString(tasks.get(0).getInput())));
            List<String> idParams = services.stream()
                    .map(x ->{
                        Object val = x.getExposeEndpointList().get(0).getValueList().get(0).getValue();
                        JSONObject obj = JSONObject.parseObject(val.toString());
                        obj.put("domain_id", x.getPartyId());
                        obj.put("domain_name", x.getPartyName());
                        obj.remove("asset_en_name");
                        obj.put("field", obj.get("key"));
                        obj.remove("key");
                        return obj.toJSONString();
                    })
                    .collect(Collectors.toList());
            Value value2 = new Value("idParams", StringEscapeUtils.unescapeJson(JSON.toJSONString(idParams)));
            Value value3 = new Value("mpcParams", StringEscapeUtils.unescapeJson(JSON.toJSONString(tasks.get(0).getModule())));
            List<Value> valueLists = Lists.newArrayList(value1, value2, value3);
            String serviceStr = JSON.toJSONString(services);
            String newServiceStr = serviceStr.replace("PirClient", "MpcClient").replace("PirServer", "MpcServer");
            services = JSONObject.parseArray(newServiceStr, Service.class);
            for(Service service: services){
                service.setServiceName(service.getServiceClass());
                service.setServiceLabel(service.getServiceClass());
                service.getExposeEndpointList().get(0).setValueList(valueLists);
            }
            tasks.clear();
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
        if (sqlVo.getModelType() == 0) {
            templateId = 2;
            String defaultOdgDID = plan.getInputDataList().get(0).getDomainID();
            String defaultOrgName = plan.getInputDataList().get(0).getDomainName();
            HashMap<String, String> map = new HashMap<>();
            List<ServiceVo> serviceVos = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                String templateType = "";
                switch (i) {
                    case 0:
                        templateType = "PirClient4Query";
                        break;
                    case 1:
                        templateType = "PirServer4Query";
                        break;
                }
                ServiceVo serviceVo = TemplateUtils.buildServiceVo(templateId, templateType, i, createTime);
                JSONArray pirParams = new JSONArray();
                for (InputData inputData : plan.getInputDataList()) {
                    JSONObject dataSourse = new JSONObject();
                    dataSourse.put("table", inputData.getTableName());
                    dataSourse.put("asset_en_name", inputData.getAssetName());
                    dataSourse.put("key", inputData.getColumn());
                    if (plan.getProject() != null ) {
                        dataSourse.put("column", plan.getProject().get(inputData.getAssetName()).toString());
                    }
                    String condition = plan.getCondition().toString();
                    for(Pair pair: setClausePair){
                        String key = pair.getKey();
                        String value = pair.getValue();
                        if(value.equalsIgnoreCase("?") && condition.contains(key)){
                            dataSourse.put("variable", key);
                        }
                    }
                    pirParams.add(dataSourse);
                }
                ValueVo valueTable = new ValueVo();
                valueTable.setKey("pirParams");
                valueTable.setValue(pirParams.toString().replace("[", "").replace("]", "").replace(" ", ""));
                serviceVo.getValues().set(0, valueTable);
                map.put(serviceVo.getExposeEndpoints().get(0).getName(), serviceVo.getServiceId());
                serviceVos.add(serviceVo);
            }
            for (ServiceVo serviceVo : serviceVos) {
                Service service = new Service();
                BeanUtils.copyProperties(serviceVo, service);
                if (serviceVo.getServiceClass().equals("PirClient4Query")) {
                    service.setPartyId(orgID);
                    service.setPartyName(orgName);
                } else {
                    service.setPartyId(defaultOdgDID);
                    service.setPartyName(defaultOrgName);
                }
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
                List<ExposeEndpoint> exposeEndpointList = new ArrayList<>();
                for (ExposeEndpointVo exposeEndpointVo : serviceVo.getExposeEndpoints()) {
                    ExposeEndpoint exposeEndpoint = new ExposeEndpoint();
                    HashMap<String, String> exposeEndpointFormMap = new HashMap<>();
                    for (ExposeFormVo exposeFormVo : exposeEndpointVo.getForm()) {
                        exposeEndpointFormMap.put(exposeFormVo.getKey(), exposeFormVo.getValues());
                    }
                    exposeEndpoint.setPartyId(service.getPartyId());
                    exposeEndpoint.setPartyName(service.getPartyName());
                    exposeEndpoint.setDescription(exposeEndpointFormMap.get("description"));
                    exposeEndpoint.setTlsEnabled(Boolean.valueOf(exposeEndpointFormMap.get("tlsEnabled")));
                    exposeEndpoint.setServiceCa(exposeEndpointFormMap.get("serviceCa"));
                    exposeEndpoint.setServiceCert(exposeEndpointFormMap.get("serviceCert"));
                    exposeEndpoint.setServiceKey(exposeEndpointFormMap.get("serviceKey"));
                    exposeEndpoint.setProtocol(exposeEndpointFormMap.get("protocol"));
                    exposeEndpoint.setMethod(exposeEndpointFormMap.get("method"));
                    exposeEndpoint.setAddress(exposeEndpointFormMap.get("address"));
                    exposeEndpoint.setPath(exposeEndpointFormMap.get("path"));
//                    exposeEndpoint.setOrgId(exposeEndpointFormMap.get("groupID"));
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

                service.setServiceLabel(service.getServiceName() + "(" + service.getPartyName() + ")");
                service.setReferExposeEndpointList(referExposeEndpointList);
                service.setExposeEndpointList(exposeEndpointList);
                services.add(service);
            }
            Map<String, String> model_method = new HashMap<>();
            model_method.put("method_name", "pir");
            job.setCommon(model_method);
        } else {
            templateId = 3;
            String defaultOdgDID = plan.getInputDataList().get(0).getDomainID();
            String defaultOrgName = plan.getInputDataList().get(0).getDomainName();
            HashMap<String, String> map = new HashMap<>();
            List<ServiceVo> serviceVos = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                String templateType = "";
                switch (i) {
                    case 0:
                        templateType = "TeePirClient4Query";
                        break;
                    case 1:
                        templateType = "TeePirServer4Query";
                        break;
                }
                ServiceVo serviceVo = TemplateUtils.buildServiceVo(templateId, templateType, i, createTime);
                JSONArray pirParams = new JSONArray();
                for (InputData inputData : plan.getInputDataList()) {
                    JSONObject dataSourse = new JSONObject();
                    dataSourse.put("table", inputData.getTableName());
                    dataSourse.put("asset_en_name", inputData.getAssetName());
                    dataSourse.put("key", inputData.getColumn());
                    if (plan.getProject() != null ) {
                        dataSourse.put("column", plan.getProject().get(inputData.getAssetName()).toString());
                    }
                    pirParams.add(dataSourse);
                }
                ValueVo valueTable = new ValueVo();
                valueTable.setKey("pirParams");
                valueTable.setValue(pirParams.toString().replace("[", "").replace("]", "").replace(" ", ""));
                serviceVo.getValues().set(0, valueTable);
                map.put(serviceVo.getExposeEndpoints().get(0).getName(), serviceVo.getServiceId());
                serviceVos.add(serviceVo);
            }
            for (ServiceVo serviceVo : serviceVos) {
                Service service = new Service();
                BeanUtils.copyProperties(serviceVo, service);
                if (serviceVo.getServiceClass().equals("TeePirClient4Query")) {
                    service.setPartyId(orgID);
                    service.setPartyName(orgName);
                } else {
                    service.setPartyId(defaultOdgDID);
                    service.setPartyName(defaultOrgName);
                }
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
                List<ExposeEndpoint> exposeEndpointList = new ArrayList<>();
                for (ExposeEndpointVo exposeEndpointVo : serviceVo.getExposeEndpoints()) {
                    ExposeEndpoint exposeEndpoint = new ExposeEndpoint();
//                    exposeEndpoint.setServiceClass(exposeEndpointVo);
                    HashMap<String, String> exposeEndpointFormMap = new HashMap<>();
                    for (ExposeFormVo exposeFormVo : exposeEndpointVo.getForm()) {
                        exposeEndpointFormMap.put(exposeFormVo.getKey(), exposeFormVo.getValues());
                    }
                    exposeEndpoint.setPartyName(service.getPartyName());
                    exposeEndpoint.setPartyId(service.getPartyId());
                    exposeEndpoint.setDescription(exposeEndpointFormMap.get("description"));
                    exposeEndpoint.setTlsEnabled(Boolean.valueOf(exposeEndpointFormMap.get("tlsEnabled")));
                    exposeEndpoint.setServiceCa(exposeEndpointFormMap.get("serviceCa"));
                    exposeEndpoint.setServiceCert(exposeEndpointFormMap.get("serviceCert"));
                    exposeEndpoint.setServiceKey(exposeEndpointFormMap.get("serviceKey"));
                    exposeEndpoint.setProtocol(exposeEndpointFormMap.get("protocol"));
                    exposeEndpoint.setMethod(exposeEndpointFormMap.get("method"));
                    exposeEndpoint.setAddress(exposeEndpointFormMap.get("address"));
                    exposeEndpoint.setPath(exposeEndpointFormMap.get("path"));
//                    exposeEndpoint.setOrgId(exposeEndpointFormMap.get("groupID"));
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

                service.setReferExposeEndpointList(referExposeEndpointList);
                service.setExposeEndpointList(exposeEndpointList);
                service.setServiceLabel(service.getServiceName() + "(" + service.getPartyName() + ")");
                services.add(service);
            }
            Map<String, String> model_method = new HashMap<>();
            model_method.put("method_name", "teepir");
            job.setCommon(model_method);
        }
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
        moduleParams.add(new ModuleParam("aggregate", plan.getAggregateType()));

        module.setParamList(moduleParams);
        task.setModule(module);

        tasks.add(task);
    }
    @Override
    public void visit(TeeMpc plan) {
        if (sqlVo.getIsStream() == 0) {
            String moduleName = TaskType.TEE.name();
            Task task = basePlanToTask(plan);

            Module module = new Module();
            module.setModuleName(moduleName);
            JSONObject param = new JSONObject();
            List<ModuleParam> moduleParams = new ArrayList<>();
            moduleParams.add(new ModuleParam("methodName", plan.getTeeModel().getMethodName()));
            moduleParams.add(new ModuleParam("domainID", plan.getTeeModel().getDomainID()));
//            param.put("methodName", plan.getTeeModel().getMethodName());
//            param.put("domainID", plan.getTeeModel().getDomainID());
            module.setParamList(moduleParams);
            task.setModule(module);

            tasks.add(task);
        } else {
            templateId = 1;
            String defaultOdgDID = plan.getInputDataList().get(0).getDomainID();
            String defaultOrgName = plan.getInputDataList().get(0).getDomainName();
            HashMap<String, String> map = new HashMap<>();
            List<ServiceVo> serviceVos = new ArrayList<>();
            for (int i=0; i<4; i++) {
                String templateType = "";
                switch (i) {
                    case 0:
                        templateType = "DataSourceAllInOne4RISC";
                        break;
                    case 1:
                        templateType = "PrivacyCompute4RISC";
                        break;
                    case 2:
                        templateType = "SuccessDownstream4RISC";
                        break;
                    case 3:
                        templateType = "FailureDownstream4RISC";
                        break;
                }
                ServiceVo serviceVo = TemplateUtils.buildServiceVo(templateId, templateType, i, createTime);
                map.put(serviceVo.getExposeEndpoints().get(0).getName(), serviceVo.getServiceId());
                serviceVos.add(serviceVo);
            }
            for (ServiceVo serviceVo : serviceVos) {

                Service service = new Service();
                BeanUtils.copyProperties(serviceVo, service);
                service.setPartyName(defaultOrgName);
                service.setPartyId(defaultOdgDID);

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
                List<ExposeEndpoint> exposeEndpointList = new ArrayList<>();
                for (ExposeEndpointVo exposeEndpointVo : serviceVo.getExposeEndpoints()) {
                    ExposeEndpoint exposeEndpoint = new ExposeEndpoint();
                    HashMap<String, String> exposeEndpointFormMap = new HashMap<>();
                    for (ExposeFormVo exposeFormVo : exposeEndpointVo.getForm()) {
                        exposeEndpointFormMap.put(exposeFormVo.getKey(), exposeFormVo.getValues());
                    }
                    exposeEndpoint.setPartyId(service.getPartyId());
                    exposeEndpoint.setPartyName(service.getPartyName());
                    exposeEndpoint.setDescription(exposeEndpointFormMap.get("description"));
                    exposeEndpoint.setTlsEnabled(Boolean.valueOf(exposeEndpointFormMap.get("tlsEnabled")));
                    exposeEndpoint.setServiceCa(exposeEndpointFormMap.get("serviceCa"));
                    exposeEndpoint.setServiceCert(exposeEndpointFormMap.get("serviceCert"));
                    exposeEndpoint.setServiceKey(exposeEndpointFormMap.get("serviceKey"));
                    exposeEndpoint.setProtocol(exposeEndpointFormMap.get("protocol"));
                    exposeEndpoint.setMethod(exposeEndpointFormMap.get("method"));
                    exposeEndpoint.setAddress(exposeEndpointFormMap.get("address"));
                    exposeEndpoint.setPath(exposeEndpointFormMap.get("path"));
//                    exposeEndpoint.setOrgId(exposeEndpointFormMap.get("groupID"));
                    exposeEndpoint.setName(exposeEndpointVo.getName());
                    exposeEndpoint.setServiceClass(serviceVo.getServiceClass());
                    exposeEndpointList.add(exposeEndpoint);
                }


                service.setExposeEndpointList(exposeEndpointList);
                service.setReferExposeEndpointList(referExposeEndpointList);
                services.add(service);
            }
            Map<String, String> model_method = new HashMap<>();
            model_method.put("method_name", plan.getTeeModel().getMethodName());
            job.setCommon(model_method);

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
//        param.put("FL", plan.getFateModel().getFl());
//        param.put("EVAL", plan.getFateModel().getEval());
//        param.put("MODEL", plan.getFateModel().getModel());
//        param.put("INTERSECTION", plan.getFateModel().getIntersection());
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
//        String taskStatus = "WAITING";
        Integer taskStatus = 0;
        task.setVersion(taskVersion);
        task.setCreateTime(createTime);
        task.setUpdateTime(createTime);
//        task.setStatus(taskStatus);
        task.setStatus(Constant.TASK_STATUS);
        task.setJobId(jobID);
        task.setTaskName(taskName);
        task.setTaskId(taskName);

        // module部分不处理

        Input input = new Input();
        List<InputDetail> taskInputDataList = new ArrayList<>();
        String srcTaskName = "";
        for (int i=0; i<plan.getInputDataList().size(); i++) {
            InputData inputData = plan.getInputDataList().get(i);
            InputDetail taskInputData = new InputDetail();
            taskInputData.setDataName(inputData.getTableName().toLowerCase());
            if (inputData.getNodeSrc() != null) {
                srcTaskName = String.valueOf(inputData.getNodeSrc());
                taskInputData.setTaskSrc(String.valueOf(inputData.getNodeSrc()));
                taskInputData.setDataName(inputData.getTableName().toLowerCase() + "-" + inputData.getNodeSrc());
            } else {
                taskInputData.setDataId(inputData.getTableName().toLowerCase());
            }
            taskInputData.setAssetName(inputData.getAssetName());


            JSONObject inputParam = new JSONObject();
            inputParam.put("table", inputData.getTableName().toLowerCase());
//            inputParam.put("asset_en_name", inputData.getAssetName());
            if (inputData.getColumn() != null) {
                inputParam.put("field", inputData.getColumn().toLowerCase());
            }
            if(inputData instanceof SpdzInputData){
                SpdzInputData spdzInputData = (SpdzInputData) inputData;
                inputParam.put("index", spdzInputData.getIndex());
            }
            for(Pair pair: setClausePair){
                String key = pair.getKey();
                if(key.contains("noise")){
                    String table = key.split("\\.")[0];
                    String field = key.split("\\.")[1];
                    if(table.equals(inputData.getTableName().toLowerCase()) && field.equals(inputData.getColumn().toLowerCase())){
                        inputParam.put("noise", JSON.parseObject(pair.getValue()));
                    }

                }
            }

            taskInputData.setParams(inputParam);
            taskInputData.setDomainId(inputData.getDomainID());
            taskInputDataList.add(taskInputData);
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

        List<Party> parties = new ArrayList<>();
        for (String partyID : plan.getParties()) {
            Party party = new Party();
            party.setPartyId(partyID);
            parties.add(party);
            jobPartyList.add(party);
        }
        parties = parties.stream().filter(StreamUtils.distinctByKey(Party::getPartyId)).collect(Collectors.toList());
        task.setPartyList(parties);
        return task;
    }
}
