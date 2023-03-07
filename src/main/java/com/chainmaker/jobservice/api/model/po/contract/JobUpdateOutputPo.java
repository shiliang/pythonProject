package com.chainmaker.jobservice.api.model.po.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.HashMap;

/**
 * @author gaokang
 * @date 2022-09-23 04:04
 * @description:更新任务输出
 * @version: 1.0.0
 */

public class JobUpdateOutputPo {
    private String jobID;
    private String taskName;
    private String partyID;
    private JSONObject output;

    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("jobID", this.getJobID().getBytes());
        contractParams.put("taskName", this.getTaskName().getBytes());
        contractParams.put("partyID", this.getPartyID().getBytes());
        contractParams.put("output", JSON.toJSONString(this.getOutput(), SerializerFeature.WriteMapNullValue).getBytes());
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

    public JSONObject getOutput() {
        return output;
    }

    public void setOutput(JSONObject output) {
        this.output = output;
    }
}
