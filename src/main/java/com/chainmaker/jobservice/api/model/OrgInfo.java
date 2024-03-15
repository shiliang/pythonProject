package com.chainmaker.jobservice.api.model;

import lombok.Data;

@Data
public class OrgInfo {
    private String orgId;
    private String orgName;

    public OrgInfo(String orgId, String orgName) {
        this.orgId = orgId;
        this.orgName = orgName;
    }
}
