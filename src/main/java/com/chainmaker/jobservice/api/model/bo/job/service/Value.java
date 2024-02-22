package com.chainmaker.jobservice.api.model.bo.job.service;

/**
 * @author gaokang
 * @date 2022-10-10 20:00
 * @description:用户自定义配置
 * @version: 1.0
 */

public class Value {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
