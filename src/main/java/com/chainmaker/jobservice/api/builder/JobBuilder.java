package com.chainmaker.jobservice.api.builder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.config.BlockchainConf;
import com.chainmaker.jobservice.api.model.bo.job.Job;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.bo.job.JobTemplate;
import com.chainmaker.jobservice.api.model.bo.job.service.ReferEndpoint;
import com.chainmaker.jobservice.api.model.bo.job.service.Service;
import com.chainmaker.jobservice.api.model.bo.job.task.*;
import com.chainmaker.jobservice.api.model.bo.job.task.Module;
import com.chainmaker.jobservice.api.model.vo.JobInfoVo;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;
import com.chainmaker.jobservice.api.model.vo.ValueVo;
import com.chainmaker.jobservice.api.response.ParserException;
import com.chainmaker.jobservice.api.response.ContractException;
import com.chainmaker.jobservice.core.optimizer.model.FL.FlInputData;
import com.chainmaker.jobservice.core.optimizer.model.InputData;
import com.chainmaker.jobservice.core.optimizer.model.OutputData;
import com.chainmaker.jobservice.core.optimizer.nodes.DAG;
import com.chainmaker.jobservice.core.optimizer.nodes.Node;
import com.chainmaker.jobservice.core.optimizer.plans.*;

import java.util.*;


/**
 * @author gaokang
 * @date 2022-09-22 14:29
 * @description:生成Job和任务图
 * @version:1.0.0
 */

public class JobBuilder extends PhysicalPlanVisitor {
    private enum JobType {
        FQ, FQS, FL, FLS, CC, CCS
    }
    private enum TaskType {
        QUERY, PSI, MPC, TEE, FL
    }
    private final String orgDID;

    private final Integer modelType;
    private final Integer isStream;
    private final DAG<PhysicalPlan> dag;
    private final String createTime, jobID;
    private Job job = new Job();
    private HashMap<String, String> kvMap = new HashMap<>();
    private List<ServiceVo> services = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();

    private HashSet<String> jobParties = new HashSet<>();

    private Integer templateId = 1;

    public JobBuilder(Integer modelType, Integer isStream, DAG<PhysicalPlan> dag, String orgDID) {
        this.modelType = modelType;
        this.isStream = isStream;
        this.dag = dag;
        this.createTime = String.valueOf(System.currentTimeMillis());
        this.jobID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        this.orgDID = orgDID;
    }

    public JobTemplate getJobTemplate() {
        JobTemplate jobTemplate = new JobTemplate();
        jobTemplate.setJob(job);
        jobTemplate.setServices(services);
        jobTemplate.setTasks(tasks);
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
        for (Node<PhysicalPlan> next : dag.getNodes()) {
            next.getObject().accept(this);
        }

        job.setJobID(jobID);
        job.setJobType(jobType);
        job.setStatus(jobStatus);
        job.setCreateTime(createTime);
        job.setUpdateTime(createTime);
        job.setTasksDAG(taskDAG);
        job.setParties(new ArrayList<>(jobParties));
    }


    @Override
    public void visit(Project plan) {
        if (isStream != 1) {
            String moduleName = TaskType.QUERY.name();
            Task task = basePlanToTask(plan);
            Module module = new Module();
            module.setModuleName(moduleName);
            JSONObject param = new JSONObject();
            param.put("alias", plan.getOutputDataList().get(0).getOutputSymbol());
            module.setParams(param);
            task.setModule(module);
            tasks.add(task);
        }
    }
    @Override
    public void visit(PirFilter plan) {
        if (modelType == 0) {
            templateId = 2;
            String defaultOdgDID = plan.getInputDataList().get(0).getDomainID();
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
                ServiceVo serviceVo = teeTemplateToService(templateType, i);
                JSONArray pirParams = new JSONArray();
                for (InputData inputData : plan.getInputDataList()) {
                    JSONObject dataSourse = new JSONObject();
                    dataSourse.put("table", inputData.getTableName());
                    dataSourse.put("key", inputData.getColumn());
                    dataSourse.put("column", plan.getProject().get(inputData.getTableName()).toString());
                    pirParams.add(dataSourse);
                }
                ValueVo valueTable = new ValueVo();
                valueTable.setKey("pirParams");
                valueTable.setValue(pirParams.toString().replace("[", "").replace("]", ""));
                serviceVo.getValues().set(0, valueTable);
                map.put(serviceVo.getExposeEndpoints().get(0).getName(), serviceVo.getId());
                serviceVos.add(serviceVo);
            }
            for (ServiceVo serviceVo : serviceVos) {
                for (ReferEndpoint referEndpoint : serviceVo.getReferEndpoints()) {
                    referEndpoint.setReferServiceID(map.get(referEndpoint.getName()));
                }
                if (serviceVo.getServiceClass().equals("PirClient4Query")) {
                    serviceVo.setOrgDID(orgDID);
                } else {
                    serviceVo.setOrgDID(defaultOdgDID);
                }
                services.add(serviceVo);
            }
            Map<String, String> model_method = new HashMap<>();
            model_method.put("method_name", "pir");
            job.setCommon(model_method);
        } else {
            templateId = 3;
            String defaultOdgDID = plan.getInputDataList().get(0).getDomainID();
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
                ServiceVo serviceVo = teeTemplateToService(templateType, i);
                JSONArray pirParams = new JSONArray();
                for (InputData inputData : plan.getInputDataList()) {
                    JSONObject dataSourse = new JSONObject();
                    dataSourse.put("table", inputData.getTableName());
                    dataSourse.put("key", inputData.getColumn());
                    dataSourse.put("column", plan.getProject().get(inputData.getTableName()).toString());
                    pirParams.add(dataSourse);
                }
                ValueVo valueTable = new ValueVo();
                valueTable.setKey("pirParams");
                valueTable.setValue(pirParams.toString().replace("[", "").replace("]", ""));
                serviceVo.getValues().set(0, valueTable);
                map.put(serviceVo.getExposeEndpoints().get(0).getName(), serviceVo.getId());
                serviceVos.add(serviceVo);
            }
            for (ServiceVo serviceVo : serviceVos) {
                for (ReferEndpoint referEndpoint : serviceVo.getReferEndpoints()) {
                    referEndpoint.setReferServiceID(map.get(referEndpoint.getName()));
                }
                if (serviceVo.getServiceClass().equals("TeePirClient4Query")) {
                    serviceVo.setOrgDID(orgDID);
                } else {
                    serviceVo.setOrgDID(defaultOdgDID);
                }
                services.add(serviceVo);
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
        String moduleName = TaskType.TEE.name();
        Task task =  basePlanToTask(plan);

        Module module = new Module();
        module.setModuleName(moduleName);
        JSONObject param = new JSONObject();
        param.put("expression", plan.getExpression());
        param.put("aggregate", plan.getAggregateType());
        module.setParams(param);
        task.setModule(module);

        tasks.add(task);
    }
    @Override
    public void visit(TeeMpc plan) {
        if (isStream == 0) {
            String moduleName = TaskType.TEE.name();
            Task task = basePlanToTask(plan);

            Module module = new Module();
            module.setModuleName(moduleName);
            JSONObject param = new JSONObject();
            param.put("methodName", plan.getTeeModel().getMethodName());
            param.put("domainID", plan.getTeeModel().getDomainID());
            module.setParams(param);
            task.setModule(module);

            tasks.add(task);
        } else {
            templateId = 1;
            String defaultOdgDID = plan.getInputDataList().get(0).getDomainID();
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
                ServiceVo serviceVo = teeTemplateToService(templateType, i);
                map.put(serviceVo.getExposeEndpoints().get(0).getName(), serviceVo.getId());
                serviceVos.add(serviceVo);
            }
            for (ServiceVo serviceVo : serviceVos) {
                for (ReferEndpoint referEndpoint : serviceVo.getReferEndpoints()) {
                    referEndpoint.setReferServiceID(map.get(referEndpoint.getName()));
                }
                serviceVo.setOrgDID(defaultOdgDID);
                services.add(serviceVo);
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
        JSONObject param = new JSONObject();
        param.put("FL", plan.getFateModel().getFl());
        param.put("EVAL", plan.getFateModel().getEval());
        param.put("MODEL", plan.getFateModel().getModel());
        param.put("INTERSECTION", plan.getFateModel().getIntersection());
        module.setParams(param);
        task.setModule(module);
        for (int i=0; i<plan.getInputDataList().size(); i++) {
            FlInputData flInputData = (FlInputData) plan.getInputDataList().get(i);
            task.getInput().getData().get(i).setRole(flInputData.getRole());
            JSONObject inputParam = new JSONObject();
            inputParam.put("label_type", flInputData.getLabel_type());
            inputParam.put("output_format", flInputData.getOutput_format());
            inputParam.put("with_label", flInputData.getWith_label());
            inputParam.put("namespace", flInputData.getNamespace());
            inputParam.put("table", flInputData.getTableName());

            task.getInput().getData().get(i).setParams(inputParam);
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
        String taskStatus = "WAITING";
        task.setVersion(taskVersion);
        task.setCreateTime(createTime);
        task.setUpdateTime(createTime);
        task.setStatus(taskStatus);
        task.setJobID(jobID);
        task.setTaskName(taskName);

        // module部分不处理

        Input input = new Input();
        List<TaskInputData> taskInputDataList = new ArrayList<>();
        for (int i=0; i<plan.getInputDataList().size(); i++) {
            InputData inputData = plan.getInputDataList().get(i);
            TaskInputData taskInputData = new TaskInputData();
            taskInputData.setDataName(inputData.getTableName().toLowerCase());
            if (inputData.getNodeSrc() != null) {
                taskInputData.setTaskSrc(String.valueOf(inputData.getNodeSrc()));
                taskInputData.setDataName(inputData.getTableName().toLowerCase() + "-" + inputData.getNodeSrc());
            } else {
                taskInputData.setDataID(inputData.getTableName().toLowerCase());
            }


            JSONObject inputParam = new JSONObject();
            inputParam.put("table", inputData.getTableName().toLowerCase());
            if (inputData.getColumn() != null) {
                inputParam.put("field", inputData.getColumn().toLowerCase());
            }
            taskInputData.setParams(inputParam);
            taskInputData.setDomainID(inputData.getDomainID());
            taskInputDataList.add(taskInputData);
        }
        input.setData(taskInputDataList);
        task.setInput(input);
        Output output = new Output();
        List<TaskOutputData> taskOutputDataList = new ArrayList<>();
        for (int i=0; i<plan.getOutputDataList().size(); i++) {
            OutputData outputData = plan.getOutputDataList().get(i);
            TaskOutputData taskOutputData = new TaskOutputData();
            taskOutputData.setDataName(outputData.getTableName().toLowerCase() + "-" + plan.getId());
            taskOutputData.setDomainID(outputData.getDomainID());
            if (plan.isFinalResult()) {
                taskOutputData.setFinalResult("Y");
            }
            taskOutputDataList.add(taskOutputData);
        }
        output.setData(taskOutputDataList);
        task.setOutput(output);

        List<Party> parties = new ArrayList<>();
        for (String partyID : plan.getParties()) {
            jobParties.add(partyID);
            Party party = new Party();
            party.setPartyID(partyID);
            parties.add(party);
        }
        task.setParties(parties);
        return task;
    }
    
    private ServiceVo teeTemplateToService(String templateType, int id) {
        ServiceVo serviceTee = ServiceVo.templateToServiceVo(String.valueOf(templateId), templateType);
        String serviceVersion = "1.0.0";
        String serviceStatus = "WAITING";
        serviceTee.setId(String.valueOf(id));
        serviceTee.setVersion(serviceVersion);
        serviceTee.setStatus(serviceStatus);
        serviceTee.setCreateTime(createTime);
        serviceTee.setUpdateTime(createTime);
        return serviceTee;
    }

}
