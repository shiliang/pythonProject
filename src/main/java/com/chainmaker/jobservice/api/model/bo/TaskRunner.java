package com.chainmaker.jobservice.api.model.bo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.model.bo.job.task.*;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskInputDataPo;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskPo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-13 10:10
 * @description:联邦SQL生成的执行任务结构
 * @version: 1.0.0
 */
@Data
public class TaskRunner {
    private String version;
    private String jobID;
    private String taskName;
    private String status;
    private String updateTime;
    private String createTime;

    private TaskRunnerModule module;
    private Input input;
    private Output output;
    private List<Party> parties;

    public static TaskRunner converterToTaskRunner(TaskPo taskPo) {
        TaskRunner task = new TaskRunner();
        task.version = taskPo.getVersion();
        task.jobID = taskPo.getJobID();
        task.taskName = taskPo.getTaskName();
        task.status = taskPo.getStatus();
        task.updateTime = taskPo.getUpdateTime();
        task.createTime = taskPo.getCreateTime();
        task.output = taskPo.getOutput();
        task.parties = taskPo.getParties();

        TaskRunnerModule module = new TaskRunnerModule();
        module.setModuleName(taskPo.getModule().getModuleName());
        module.setParamList(taskPo.getModule().getParamList());
        task.setModule(module);

        Input input = new Input();
        List<TaskInputData> data = new ArrayList<>();
        for (TaskInputDataPo taskInputDataPo : taskPo.getInput().getData()) {
            TaskInputData taskInputData = new TaskInputData();
            taskInputData.setDataID(taskInputDataPo.getDataID());
            taskInputData.setDataName(taskInputDataPo.getDataName());
            taskInputData.setTaskSrc(taskInputDataPo.getTaskSrc());
            taskInputData.setDomainID(taskInputDataPo.getDomainID());
            taskInputData.setRole(taskInputDataPo.getRole());
            taskInputData.setParams(JSON.parseObject(taskInputDataPo.getParams(), Feature.OrderedField));
            taskInputData.setAssetName(taskInputDataPo.getAssetName());
            taskInputData.setType(taskInputDataPo.getType());
            taskInputData.setLength(taskInputDataPo.getLength());
            taskInputData.setComments(taskInputDataPo.getComments());
            taskInputData.setDatabaseName(taskInputDataPo.getDatabaseName());
            taskInputData.setTableName(taskInputDataPo.getTableName());
            taskInputData.setColumnName(taskInputDataPo.getColumnName());
            data.add(taskInputData);
        }
        input.setData(data);
        task.setInput(input);
        return task;
    }
}
