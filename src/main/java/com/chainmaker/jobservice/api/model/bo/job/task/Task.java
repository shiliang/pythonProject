package com.chainmaker.jobservice.api.model.bo.job.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskInputDataPo;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskPo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-13 10:10
 * @description:联邦SQL生成的执行任务结构
 * @version: 1.0.0
 */

public class Task {
    private String version;
    private String jobID;
    private String taskName;
    private String status;
    private String updateTime;
    private String createTime;

    private Module module;
    private Input input;
    private Output output;
    private List<Party> parties;

    public static Task converterToTask(TaskPo taskPo) {
        Task task = new Task();
        task.version = taskPo.getVersion();
        task.jobID = taskPo.getJobID();
        task.taskName = taskPo.getTaskName();
        task.status = taskPo.getStatus();
        task.updateTime = taskPo.getUpdateTime();
        task.createTime = taskPo.getCreateTime();
        task.output = taskPo.getOutput();
        task.parties = taskPo.getParties();

        Module module = new Module();
        module.setModuleName(taskPo.getModule().getModuleName());
        module.setParams(JSON.parseObject(taskPo.getModule().getParamList(), Feature.OrderedField));
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
            data.add(taskInputData);
        }
        input.setData(data);
        task.setInput(input);
        return task;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public List<Party> getParties() {
        return parties;
    }

    public void setParties(List<Party> parties) {
        this.parties = parties;
    }

    @Override
    public String toString() {
        return "Task{" +
                "version='" + version + '\'' +
                ", jobID='" + jobID + '\'' +
                ", taskName='" + taskName + '\'' +
                ", status='" + status + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", createTime='" + createTime + '\'' +
                ", module=" + module +
                ", input=" + input +
                ", output=" + output +
                ", parties=" + parties +
                '}';
    }
}
