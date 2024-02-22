package com.chainmaker.jobservice.api.model.bo.job.service;

import lombok.Data;

/**
 * @author gaokang
 * @date 2022-09-21 16:36
 * @description:
 * @version:
 */

@Data
public class ReferValue {
    private String referServiceID;
    private String key;
    private String value;

    @Override
    public String toString() {
        return "ReferValue{" +
                "referServiceID='" + referServiceID + '\'' +
                ", key='" + key + '\'' +
                ", valye='" + value + '\'' +
                '}';
    }
}
