package com.chainmaker.jobservice.api.model.job.service;

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
    private String serviceCertName;

    private List<ReferValue> referValueList;

    private String protocol;
}
