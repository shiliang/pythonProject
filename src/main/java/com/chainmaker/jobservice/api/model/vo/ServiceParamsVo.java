package com.chainmaker.jobservice.api.model.vo;

/**
 * @author gaokang
 * @date 2022-09-21 14:01
 * @description:
 * @version:
 */

public class ServiceParamsVo {
    private String serviceName;
    private String serviceClass;
    private String serviceID;
    private String orgDID;

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

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getOrgDID() {
        return orgDID;
    }

    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }
}
