package com.chainmaker.jobservice.api.model;

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
    private String serviceId;

    private String serviceDescription;
    private String orgId;

    private String orgName;
    private String serviceClass;

//    @JSONField(deserializeUsing = ExposeEndpointDeserializer.class)
    private List<ExposeEndpoint> exposeEndpointList = new ArrayList<>();

//    @JSONField(deserializeUsing = ReferExposeEndpointDeserializer.class)
    private List<ReferExposeEndpoint> referExposeEndpointList = new ArrayList<>();
}
