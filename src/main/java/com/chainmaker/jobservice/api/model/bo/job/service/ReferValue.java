package com.chainmaker.jobservice.api.model.bo.job.service;

/**
 * @author gaokang
 * @date 2022-09-21 16:36
 * @description:
 * @version:
 */

public class ReferValue {
    private String referServiceID;
    private String key;

    public String getReferServiceID() {
        return referServiceID;
    }

    public void setReferServiceID(String referServiceID) {
        this.referServiceID = referServiceID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "ReferValue{" +
                "referServiceID='" + referServiceID + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
