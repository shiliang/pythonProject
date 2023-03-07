package com.chainmaker.jobservice.api.model.po.data;

import java.util.List;

public class ServiceValues {
    private List<ServiceValueParam> serviceValueParams;

    public List<ServiceValueParam> getServiceValueParams() {
        return serviceValueParams;
    }

    public void setServiceValueParams(List<ServiceValueParam> serviceValueParams) {
        this.serviceValueParams = serviceValueParams;
    }
}
