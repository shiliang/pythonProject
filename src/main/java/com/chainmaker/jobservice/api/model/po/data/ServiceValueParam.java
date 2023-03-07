package com.chainmaker.jobservice.api.model.po.data;

import java.util.Arrays;

public class ServiceValueParam {
    private String id;
    private String jobID;
    private String orgDID;
    private String name;
    private String nodePort;
    private byte[] serviceCa;
    private byte[] serviceCert;
    private byte[] serviceKey;

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

    public String getOrgDID() {
        return orgDID;
    }

    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodePort() {
        return nodePort;
    }

    public void setNodePort(String nodePort) {
        this.nodePort = nodePort;
    }

    public byte[] getServiceCa() {
        return serviceCa;
    }

    public void setServiceCa(byte[] serviceCa) {
        this.serviceCa = serviceCa;
    }

    public byte[] getServiceCert() {
        return serviceCert;
    }

    public void setServiceCert(byte[] serviceCert) {
        this.serviceCert = serviceCert;
    }

    public byte[] getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(byte[] serviceKey) {
        this.serviceKey = serviceKey;
    }

    @Override
    public String toString() {
        return "ServiceValueParam{" +
                "id='" + id + '\'' +
                ", jobID='" + jobID + '\'' +
                ", orgDID='" + orgDID + '\'' +
                ", name='" + name + '\'' +
                ", nodePort='" + nodePort + '\'' +
                ", serviceCa=" + Arrays.toString(serviceCa) +
                ", serviceCert=" + Arrays.toString(serviceCert) +
                ", serviceKey=" + Arrays.toString(serviceKey) +
                '}';
    }
}
