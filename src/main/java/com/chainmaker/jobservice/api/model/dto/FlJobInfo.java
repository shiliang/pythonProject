package com.chainmaker.backendservice.model.dto;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;

public class FlJobInfo {
    private String orgDID;
    private String missionID;
    private String jobID;
    private String flJobID;
    private String jobType;
    private String modelID;
    private String modelVersion;
    private String modelName;
    private String components;
    private String status;
    private String startTime;
    private String endTime;

    public String getOrgDID() {
        return orgDID;
    }

    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }

    public String getMissionID() {
        return missionID;
    }

    public void setMissionID(String missionID) {
        this.missionID = missionID;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getFlJobID() {
        return flJobID;
    }

    public void setFlJobID(String flJobID) {
        this.flJobID = flJobID;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getModelID() {
        return modelID;
    }

    public void setModelID(String modelID) {
        this.modelID = modelID;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getComponents() {
        return components;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    @Override
    public String toString() {
        return "FlJobInfo{" +
                "orgDID='" + orgDID + '\'' +
                ", missionID='" + missionID + '\'' +
                ", jobID='" + jobID + '\'' +
                ", flJobID='" + flJobID + '\'' +
                ", jobType='" + jobType + '\'' +
                ", modelID='" + modelID + '\'' +
                ", modelVersion='" + modelVersion + '\'' +
                ", modelName='" + modelName + '\'' +
                ", components='" + components + '\'' +
                ", status='" + status + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}
