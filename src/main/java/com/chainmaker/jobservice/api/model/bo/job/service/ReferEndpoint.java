package com.chainmaker.jobservice.api.model.bo.job.service;

/**
 * @author gaokang
 * @date 2022-09-17 20:06
 * @description:引用的服务
 * @version: 1.0.0
 */

public class ReferEndpoint {
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

    @Override
    public String toString() {
        return "ReferEndpoint{" +
                "name='" + name + '\'' +
                ", referServiceID='" + referServiceID + '\'' +
                ", referEndpointName='" + referEndpointName + '\'' +
                ", protocol='" + protocol + '\'' +
                '}';
    }
}
