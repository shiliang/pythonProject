package com.chainmaker.jobservice.api.model.bo.job.task;

import com.alibaba.fastjson.JSONObject;

/**
 * @author gaokang
 * @date 2022-08-13 11:01
 * @description:任务输入详细信息
 * @version: 1.0.0
 */

public class TaskInputData {
    private String dataName;
    private String taskSrc = "";
    private String dataID = "";
    private String domainID;
    private String role = "client";
    private JSONObject params;

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

    public JSONObject getParams() {
        return params;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "TaskInputData{" +
                "dataName='" + dataName + '\'' +
                ", taskSrc='" + taskSrc + '\'' +
                ", dataID='" + dataID + '\'' +
                ", domainID='" + domainID + '\'' +
                ", role='" + role + '\'' +
                ", params=" + params +
                '}';
    }
}
