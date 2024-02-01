package com.chainmaker.backendservice.model.dto;

/**
 * @author gaokang
 * @date 2022-10-10 19:58
 * @description:服务声明
 * @version: 1.0
 */

public class ExposeEndpoint {
    /**
     * 暴露服务方法名称
     */
    private String name;
    /**
     * 暴露服务方法描述
     */
    private String description;
    /**
     * 通信协议类型：
     * HTTP
     * GRPC
     */
    private String protocol;
    /**
     * hostname:port形式
     */
    private String address;
    /**
     * 是否使用加密通信
     */
    private String tlsEnabled;
    /**
     * TLS证书
     */
    private String caCert;
    private String serverCert;
    /**
     * 路径
     * 仅当protocol:HTTP有效
     */
    private String path;
    /**
     * GET/POST/PUT等
     * 仅当protocol:HTTP有效
     */
    private String method;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getTlsEnabled() {
        return tlsEnabled;
    }

    public void setTlsEnabled(String tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
    }

    public String getCaCert() {
        return caCert;
    }

    public void setCaCert(String caCert) {
        this.caCert = caCert;
    }

    public String getServerCert() {
        return serverCert;
    }

    public void setServerCert(String serverCert) {
        this.serverCert = serverCert;
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
}
