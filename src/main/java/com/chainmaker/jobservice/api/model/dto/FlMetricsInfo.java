package com.chainmaker.backendservice.model.dto;

import com.alibaba.fastjson.JSONObject;

public class FlMetricsInfo {
    private String orgDID;
    private String jobID;
    private String component;
    private JSONObject metrics;

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

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public JSONObject getMetrics() {
        return metrics;
    }

    public void setMetrics(JSONObject metrics) {
        this.metrics = metrics;
    }
}
