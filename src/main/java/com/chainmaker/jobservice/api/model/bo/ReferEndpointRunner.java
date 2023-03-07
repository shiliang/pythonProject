package com.chainmaker.jobservice.api.model.bo;

public class ReferEndpointRunner {
    /**
     * 引用名称
     */
    private String name;
    /**
     * 不可为空
     * 被引用的service名称
     */
    private String referServiceID;
    /**
     * 不可为空
     * 被引用的service Endpoint名称
     */
    private String referEndpointName;
    /**
     * 协议类型：
     * HTTP
     * GRPC
     */
    private String protocol;
    private String address;
    private String path;
    private String method;

    private String serviceCa;
    private String serviceCert;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferServiceID() {
        return referServiceID;
    }

    public void setReferServiceID(String referServiceID) {
        this.referServiceID = referServiceID;
    }

    public String getReferEndpointName() {
        return referEndpointName;
    }

    public void setReferEndpointName(String referEndpointName) {
        this.referEndpointName = referEndpointName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getServiceCa() {
        return serviceCa;
    }

    public void setServiceCa(String serviceCa) {
        this.serviceCa = serviceCa;
    }

    public String getServiceCert() {
        return serviceCert;
    }

    public void setServiceCert(String serviceCert) {
        this.serviceCert = serviceCert;
    }

    @Override
    public String toString() {
        return "ReferEndpointRunner{" +
                "name='" + name + '\'' +
                ", referServiceID='" + referServiceID + '\'' +
                ", referEndpointName='" + referEndpointName + '\'' +
                ", protocol='" + protocol + '\'' +
                ", address='" + address + '\'' +
                ", path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", serviceCa='" + serviceCa + '\'' +
                ", serviceCert='" + serviceCert + '\'' +
                '}';
    }
}
