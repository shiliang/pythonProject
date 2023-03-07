package com.chainmaker.jobservice.api.model.po.contract;

import java.util.HashMap;

/**
 * @author gaokang
 * @date 2022-09-23 03:25
 * @description:参与方操作job
 * @version: 1.0.0
 */

public class JobUpdatePo {
    private String jobID;
    private String timestamp;
    private String partyID;

    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("jobID", this.getJobID().getBytes());
        contractParams.put("partyID", this.getPartyID().getBytes());
        contractParams.put("timestamp", this.getTimestamp().getBytes());
        return contractParams;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getPartyID() {
        return partyID;
    }

    public void setPartyID(String partyID) {
        this.partyID = partyID;
    }
}
