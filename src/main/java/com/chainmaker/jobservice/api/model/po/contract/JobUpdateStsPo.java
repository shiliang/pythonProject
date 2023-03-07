package com.chainmaker.jobservice.api.model.po.contract;

import java.util.HashMap;

/**
 * @author gaokang
 * @date 2022-12-01 20:57
 * @description:
 * @version:
 */
public class JobUpdateStsPo {
    private String jobID;
    private String timestamp;
    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("jobID", this.getJobID().getBytes());
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
}
