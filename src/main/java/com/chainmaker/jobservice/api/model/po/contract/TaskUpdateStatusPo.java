package com.chainmaker.jobservice.api.model.po.contract;

import java.util.HashMap;

/**
 * @author gaokang
 * @date 2022-09-23 03:32
 * @description:更新状态
 * @version: 1.0.0
 */

public class TaskUpdateStatusPo {
    private String jobID;
    private String taskName;
    private String partyID;
    private String sts;
    private String timestamp;

    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("jobID", this.getJobID().getBytes());
        contractParams.put("taskName", this.getTaskName().getBytes());
        contractParams.put("partyID", this.getPartyID().getBytes());
        contractParams.put("sts", this.getSts().getBytes());
        contractParams.put("timestamp", this.getTimestamp().getBytes());
        return contractParams;
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

    public String getPartyID() {
        return partyID;
    }

    public void setPartyID(String partyID) {
        this.partyID = partyID;
    }

    public String getSts() {
        return sts;
    }

    public void setSts(String sts) {
        this.sts = sts;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
