package com.chainmaker.jobservice.api.model.bo.job.task;

/**
 * @author gaokang
 * @date 2022-08-13 11:30
 * @description:参与方网络信息
 * @version: 1.0.0
 */

public class ServerInfo {
    private String ip;
    private String port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
