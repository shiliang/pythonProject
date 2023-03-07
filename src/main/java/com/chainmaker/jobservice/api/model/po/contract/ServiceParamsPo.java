package com.chainmaker.jobservice.api.model.po.contract;

import com.alibaba.fastjson.JSON;
import com.chainmaker.jobservice.api.model.bo.job.service.ReferValue;
import com.chainmaker.jobservice.api.model.bo.job.service.Service;
import com.chainmaker.jobservice.api.model.vo.ReferValueVo;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;
import com.chainmaker.jobservice.api.model.vo.ValueVo;

import java.util.HashMap;

/**
 * @author gaokang
 * @date 2022-09-26 09:58
 * @description:
 * @version:
 */

public class ServiceParamsPo {
    private String id;
    private String jobID;
    private String timestamp;

    private String exposeEndpoints;
    private String values;
    private String referValues;


    public static ServiceParamsPo converterToServiceParamsPo(Service service) {
        ServiceParamsPo serviceParamsPo = new ServiceParamsPo();
        if (service.getId() != null) {
            serviceParamsPo.setId(service.getId());
        }
        serviceParamsPo.setJobID(service.getJobID());
        serviceParamsPo.setTimestamp(String.valueOf(System.currentTimeMillis()));
        for (HashMap<String, String> exposeEndpoint : service.getExposeEndpoints().values()) {
            exposeEndpoint.remove("serviceKey");
        }
        serviceParamsPo.setExposeEndpoints(JSON.toJSONString(service.getExposeEndpoints()));
        serviceParamsPo.setValues(JSON.toJSONString(service.getValues()));
        serviceParamsPo.setReferValues(JSON.toJSONString(service.getReferValues()));
        return serviceParamsPo;
    }

    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("id", this.getId().getBytes());
        contractParams.put("jobID", this.getJobID().getBytes());
        contractParams.put("timestamp", this.getTimestamp().getBytes());
        contractParams.put("exposeEndpoints", this.getExposeEndpoints().getBytes());
        contractParams.put("values", this.getValues().getBytes());
        contractParams.put("referValues", this.getReferValues().getBytes());
        return contractParams;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getExposeEndpoints() {
        return exposeEndpoints;
    }

    public void setExposeEndpoints(String exposeEndpoints) {
        this.exposeEndpoints = exposeEndpoints;
    }


    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getReferValues() {
        return referValues;
    }

    public void setReferValues(String referValues) {
        this.referValues = referValues;
    }
}
