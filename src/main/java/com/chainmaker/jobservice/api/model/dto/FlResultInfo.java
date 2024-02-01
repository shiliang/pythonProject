package com.chainmaker.backendservice.model.dto;

import com.alibaba.fastjson.JSONObject;

public class FlResultInfo {
    private String orgDID;
    private String jobID;
    private String fJobID;
    private String dataID;
    private JSONObject result;

    public String getOrgDID() {
        return orgDID;
    }

    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getDataID() {
        return dataID;
    }

    public void setDataID(String dataID) {
        this.dataID = dataID;
    }

    public JSONObject getResult() {
        return result;
    }

    public void setResult(JSONObject result) {
        this.result = result;
    }

    public String getfJobID() {
        return fJobID;
    }

    public void setfJobID(String fJobID) {
        this.fJobID = fJobID;
    }

    @Override
    public String toString() {
        return "FlResultInfo{" +
                "orgDID='" + orgDID + '\'' +
                ", jobID='" + jobID + '\'' +
                ", fJobID='" + fJobID + '\'' +
                ", dataID='" + dataID + '\'' +
                ", result=" + result +
                '}';
    }
}
