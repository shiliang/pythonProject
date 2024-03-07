package com.chainmaker.jobservice.api.model;

import lombok.Data;

import java.util.List;

@Data
public class ReferExposeEndpoint {
    private String address;
    private String name;

    private String id;

    private String referServiceId;

    private String referEndpointName;

    private String serviceCa;

    private String serviceCert;

    private List<ReferValue> referValueList;

    private String protocol;
}
