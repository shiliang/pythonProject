package com.chainmaker.jobservice.api.model.po.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;

import java.util.HashMap;

/**
 * @author gaokang
 * @date 2022-09-23 04:04
 * @description:更新任务输出
 * @version: 1.0.0
 */
@Data
public class JobUpdateFailedReasonPo {
    private String jobID;
    private String failedReason;

    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("jobID", this.getJobID().getBytes());
        contractParams.put("reason", this.failedReason.getBytes());
        return contractParams;
    }
}
