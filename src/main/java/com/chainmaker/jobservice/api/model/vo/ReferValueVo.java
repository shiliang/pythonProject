package com.chainmaker.jobservice.api.model.vo;

/**
 * @author gaokang
 * @date 2022-09-24 17:25
 * @description:
 * @version:
 */

public class ReferValueVo {
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
