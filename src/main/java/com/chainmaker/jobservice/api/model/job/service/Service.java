package com.chainmaker.jobservice.api.model.job.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-10-10 19:52
 * @description:服务信息
 * @version: 1.0
 */
@Data
public class Service {

    private String serviceName;
    private String serviceLabel;
    private String serviceId;

    private String serviceDescription;
    private String partyId;
    private Boolean manual;
    private String version;

    private String partyName;
    private String serviceClass;
    private String status;

//    @JSONField(deserializeUsing = ExposeEndpointDeserializer.class)
    private List<ExposeEndpoint> exposeEndpointList = new ArrayList<>();

//    @JSONField(deserializeUsing = ReferExposeEndpointDeserializer.class)
    private List<ReferExposeEndpoint> referExposeEndpointList = new ArrayList<>();
}
