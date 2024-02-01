/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-09-2022/9/29 16:08
 * @Description: 本地存储的service相关参数
 * @Version: 1.0
 */

package com.chainmaker.backendservice.model.dto;

public class ServiceValueInfo {
    private String id;
    private String jobID;
    private String orgDID;
    private String name;
    private String nodePort;
    private byte[] serviceCa;
    private byte[] serviceCert;
    private byte[] serviceKey;
    private byte[] clientCa;
    private byte[] clientCert;

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

    public byte[] getClientCa() {
        return clientCa;
    }

    public void setClientCa(byte[] clientCa) {
        this.clientCa = clientCa;
    }

    public byte[] getClientCert() {
        return clientCert;
    }

    public void setClientCert(byte[] clientCert) {
        this.clientCert = clientCert;
    }
}
