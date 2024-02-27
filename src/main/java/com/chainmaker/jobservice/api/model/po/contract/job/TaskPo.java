package com.chainmaker.jobservice.api.model.po.contract.job;

import com.chainmaker.jobservice.api.model.bo.job.task.*;
import com.chainmaker.jobservice.api.util.ParamsConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-19 19:08
 * @description:持久化的Task结构
 * @version: 1.0.0
 */

public class TaskPo {
    private String version;
    private String jobID;
    private String taskName;
    private String status;
    private String updateTime;
    private String createTime;

    private ModulePo module;
    private InputPo input;
    private Output output;
    private List<Party> parties;

    public static TaskPo converterToTaskPo(Task task) {
        TaskPo taskPo = new TaskPo();
        taskPo.version = task.getVersion();
        taskPo.jobID = task.getJobID();
        taskPo.taskName = task.getTaskName();
        taskPo.status = task.getStatus();
        taskPo.updateTime = task.getUpdateTime();
        taskPo.createTime = task.getCreateTime();
        taskPo.output = task.getOutput();
        taskPo.parties = task.getParties();

        ModulePo modulePo = new ModulePo();
        modulePo.setModuleName(task.getModule().getModuleName());
        modulePo.setParams(ParamsConverter.convertToString(task.getModule().getParams()));
        taskPo.setModule(modulePo);

        InputPo inputPo = new InputPo();
        List<TaskInputDataPo> taskInputDataPos = new ArrayList<>();
        for (TaskInputData taskInputData : task.getInput().getData()) {
            TaskInputDataPo taskInputDataPo = new TaskInputDataPo();
            taskInputDataPo.setDataID(taskInputData.getDataID());
            taskInputDataPo.setAssetEnName(taskInputData.getDataID());
            taskInputDataPo.setDataName(taskInputData.getDataName());
            taskInputDataPo.setTaskSrc(taskInputData.getTaskSrc());
            taskInputDataPo.setDomainID(taskInputData.getDomainID());
            taskInputDataPo.setRole(taskInputData.getRole());
            taskInputDataPo.setParams(ParamsConverter.convertToString(taskInputData.getParams()));
            taskInputDataPos.add(taskInputDataPo);
        }
        inputPo.setData(taskInputDataPos);
        taskPo.setInput(inputPo);
        return taskPo;
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

    public ModulePo getModule() {
        return module;
    }

    public void setModule(ModulePo module) {
        this.module = module;
    }

    public InputPo getInput() {
        return input;
    }

    public void setInput(InputPo input) {
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
        return "TaskPo{" +
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
