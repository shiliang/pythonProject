package com.chainmaker.backendservice.model.dto;

public class SqlDetailInfo {
    private String id;
    private String logicName;
    private String logicDescription;
    private String logicText;
    private int status;
    private String orgId;
    private String orgDID;
    private String createTime;
    private String publishTime;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogicName() {
        return logicName;
    }

    public void setLogicName(String logicName) {
        this.logicName = logicName;
    }

    public String getLogicDescription() {
        return logicDescription;
    }

    public void setLogicDescription(String logicDescription) {
        this.logicDescription = logicDescription;
    }

    public String getLogicText() {
        return logicText;
    }

    public void setLogicText(String logicText) {
        this.logicText = logicText;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOrgDID() {
        return orgDID;
    }

    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    @Override
    public String toString() {
        return "SqlDetailInfo{" +
                "id='" + id + '\'' +
                ", logicName='" + logicName + '\'' +
                ", logicDescription='" + logicDescription + '\'' +
                ", logicText='" + logicText + '\'' +
                ", status=" + status +
                ", orgId='" + orgId + '\'' +
                ", orgDID='" + orgDID + '\'' +
                ", createTime='" + createTime + '\'' +
                ", publishTime=" + publishTime +
                '}';
    }
}
