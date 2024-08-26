package com.chainmaker.jobservice.api.builder;

import com.chainmaker.jobservice.api.Constant;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;

public class TemplateUtils {

    public static ServiceVo buildServiceVo(int templateId, String templateType, int serviceId, String serviceTime) {
        return buildServiceVo(String.valueOf(templateId), templateType, serviceId, serviceTime);
    }

    public static ServiceVo buildServiceVo(String templateId, String templateType, int serviceId, String serviceTime) {
        ServiceVo serviceVo = ServiceVo.fromTemplateFile(String.valueOf(templateId), templateType);
        String serviceVersion = "1.0.0";

        serviceVo.setServiceId(String.valueOf(serviceId));
        serviceVo.setVersion(serviceVersion);
        serviceVo.setStatus(Constant.SERVICE_STATUS);
        serviceVo.setCreateTime(serviceTime);
        serviceVo.setUpdateTime(serviceTime);
        return serviceVo;
    }

    public static ServiceVo buildPirClientServiceVo( int serviceId, String serviceTime) {
        ServiceVo serviceVo =  buildServiceVo(2, "PIRClient", serviceId, serviceTime);
//        serviceVo.setServiceClass();
//        serviceVo.setServiceName();
//        serviceVo.
        return serviceVo;
    }

    public static ServiceVo buildPirServerServiceVo( int serviceId, String serviceTime) {
        return buildServiceVo(2, "PIR", serviceId, serviceTime);
    }

    public static ServiceVo buildTeePirCientServiceVo( int serviceId, String serviceTime) {
        return buildServiceVo(2, "PIR", serviceId, serviceTime);
    }

    public static ServiceVo buildTeePirServerServiceVo( int serviceId, String serviceTime) {
        return buildServiceVo(2, "PIR", serviceId, serviceTime);
    }

}
