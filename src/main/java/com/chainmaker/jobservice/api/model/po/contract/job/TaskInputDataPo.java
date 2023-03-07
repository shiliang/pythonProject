package com.chainmaker.jobservice.api.model.po.contract.job;

/**
 * @author gaokang
 * @date 2022-09-19 19:27
 * @description:
 * @version:
 */

public class TaskInputDataPo {
    private String dataName;
    private String taskSrc;
    private String dataID;
    private String domainID;
    private String role;
    private String params;

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public String getTaskSrc() {
        return taskSrc;
    }

    public void setTaskSrc(String taskSrc) {
        this.taskSrc = taskSrc;
    }

    public String getDataID() {
        return dataID;
    }

    public void setDataID(String dataID) {
        this.dataID = dataID;
    }

    public String getDomainID() {
        return domainID;
    }

    public void setDomainID(String domainID) {
        this.domainID = domainID;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "TaskInputDataPo{" +
                "dataName='" + dataName + '\'' +
                ", taskSrc='" + taskSrc + '\'' +
                ", dataID='" + dataID + '\'' +
                ", domainID='" + domainID + '\'' +
                ", role='" + role + '\'' +
                ", params='" + params + '\'' +
                '}';
    }
}
