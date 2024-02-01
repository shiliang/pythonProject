package com.chainmaker.backendservice.model.dto;

import com.alibaba.fastjson.JSONObject;


public class FlModelCatalogInfo {
    private String missionId;
    private String jobId;
    private String orgDID;
    private int modelAuthority; // 0:private model; 1:public model;
    private String modelId;
    private String modelVersion;
    private String modelName;
    private String modelComponents;
    private JSONObject modelFeatures;
    private JSONObject parties;
    private String modelMetrics;
    private JSONObject modelParams;
    private int status;
    private float auc;
    private float ks;
    private String description;
    private String createTime;

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getModelComponents() {
        return modelComponents;
    }

    public void setModelComponents(String modelComponents) {
        this.modelComponents = modelComponents;
    }

    public float getAuc() {
        return auc;
    }

    public void setAuc(float auc) {
        this.auc = auc;
    }

    public float getKs() {
        return ks;
    }

    public void setKs(float ks) {
        this.ks = ks;
    }

    public JSONObject getModelFeatures() {
        return modelFeatures;
    }

    public void setModelFeatures(JSONObject modelFeatures) {
        this.modelFeatures = modelFeatures;
    }

    public JSONObject getParties() {
        return parties;
    }

    public void setParties(JSONObject parties) {
        this.parties = parties;
    }

    public String getModelMetrics() {
        return modelMetrics;
    }

    public void setModelMetrics(String modelMetrics) {
        this.modelMetrics = modelMetrics;
    }

    public JSONObject getModelParams() {
        return modelParams;
    }

    public void setModelParams(JSONObject modelParams) {
        this.modelParams = modelParams;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getOrgDID() {
        return orgDID;
    }

    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }

    public int getModelAuthority() {
        return modelAuthority;
    }

    public void setModelAuthority(int modelAuthority) {
        this.modelAuthority = modelAuthority;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
