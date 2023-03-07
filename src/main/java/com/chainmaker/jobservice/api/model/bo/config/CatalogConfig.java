package com.chainmaker.jobservice.api.model.bo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author gaokang
 * @date 2022-11-21 15:44
 * @description:
 * @version:
 */
@Component
@ConfigurationProperties(prefix = "catalog")
public class CatalogConfig {
    private String address;
    private String port;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
