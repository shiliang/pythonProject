package com.chainmaker.backendservice.model.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


public class Task {
    private String version;
    private String jobID;
    private String taskName;
    private String status;
    private String updateTime;
    private String createTime;

    private JSONObject module;
    private JSONObject input;
    private JSONObject output;
    private JSONArray parties;

    public Task() {}

    public Task(String version, String jobID, String taskName, String status, String updateTime, String createTime, JSONObject module, JSONObject input, JSONObject output, JSONArray parties) {
        this.version = version;
        this.jobID = jobID;
        this.taskName = taskName;
        this.status = status;
        this.updateTime = updateTime;
        this.createTime = createTime;
        this.module = module;
        this.input = input;
        this.output = output;
        this.parties = parties;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getStatus() {
        return status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getJobID() {
        return jobID;
    }

    public JSONArray getParties() {
        return parties;
    }

    public JSONObject getInput() {
        return input;
    }

    public JSONObject getOutput() {
        return output;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getVersion() {
        return version;
    }

    public JSONObject getModule() {
        return module;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setParties(JSONArray parties) {
        this.parties = parties;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setInput(JSONObject input) {
        this.input = input;
    }

    public void setModule(JSONObject module) {
        this.module = module;
    }

    public void setOutput(JSONObject output) {
        this.output = output;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
