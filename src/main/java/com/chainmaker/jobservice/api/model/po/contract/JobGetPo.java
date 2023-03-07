package com.chainmaker.jobservice.api.model.po.contract;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;

/**
 * @author gaokang
 * @date 2022-09-23 03:14
 * @description:根据jobID查询
 * @version: 1.0.0
 */

public class JobGetPo {
    private String jobID;
    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("jobID", this.getJobID().getBytes());
        return contractParams;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }
}
