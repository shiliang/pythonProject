package com.chainmaker.jobservice.api.model.vo;

/**
 * @author gaokang
 * @date 2022-09-22 21:58
 * @description:
 * @version:
 */

public class JobVo {
    private String jobID;

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    @Override
    public String toString() {
        return "JobVo{" +
                "jobID='" + jobID + '\'' +
                '}';
    }
}
