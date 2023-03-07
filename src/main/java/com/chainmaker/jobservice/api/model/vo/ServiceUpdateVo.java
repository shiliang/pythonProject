package com.chainmaker.jobservice.api.model.vo;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-26 11:49
 * @description:审批更新service
 * @version: 1.0.0
 */

public class ServiceUpdateVo {
    private String orgDID;
    private JobInfoVo jobInfo;

    public String getOrgDID() {
        return orgDID;
    }

    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }

    public JobInfoVo getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobInfoVo jobInfo) {
        this.jobInfo = jobInfo;
    }
}
