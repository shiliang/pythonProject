package com.chainmaker.backendservice.model.dto;

/**
 * @author gaokang
 * @date 2022-10-10 20:01
 * @description:服务引用配置
 * @version: 1.0
 */

public class ReferValue {
    private String key;
    private String referServiceID;
    private String referKey;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getReferServiceID() {
        return referServiceID;
    }

    public void setReferServiceID(String referServiceID) {
        this.referServiceID = referServiceID;
    }

    public String getReferKey() {
        return referKey;
    }

    public void setReferKey(String referKey) {
        this.referKey = referKey;
    }
}
