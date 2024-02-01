package com.chainmaker.backendservice.model.dto;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-10-10 19:52
 * @description:服务信息
 * @version: 1.0
 */

public class Service {
    private String groupId;
    private String orgDID;
    private String serviceName;
    private String serviceClass;
    private String id;
    private List<ExposeEndpoint> exposeEndpoints;
    private List<Value> values;
    private List<ReferValue> referValues;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getOrgDID() {
        return orgDID;
    }

    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ExposeEndpoint> getExposeEndpoints() {
        return exposeEndpoints;
    }

    public void setExposeEndpoints(List<ExposeEndpoint> exposeEndpoints) {
        this.exposeEndpoints = exposeEndpoints;
    }

    public List<Value> getValues() {
        return values;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }

    public List<ReferValue> getReferValues() {
        return referValues;
    }

    public void setReferValues(List<ReferValue> referValues) {
        this.referValues = referValues;
    }
}
