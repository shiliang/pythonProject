package com.chainmaker.jobservice.api.model.po.contract.job;

import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.model.bo.job.Job;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-19 19:06
 * @description:数据库和链上持久化的Job结构
 * @version: 1.0.0
 */

public class JobPo {
    private String jobName;
    private String jobID;
    private String submitter;
    private String createTime;
    private String updateTime;
    private String jobType;
    private String projectID;
    private String status;
    private String requestData;
    private String tasksDAG;
    private List<String> parties;
    private String common;

    public static JobPo converterToJobPo(Job job) {
        JobPo jobPo = new JobPo();
        jobPo.setJobName(job.getJobName());
        jobPo.setJobID(job.getJobID());
        jobPo.setSubmitter(job.getSubmitter());
        jobPo.setCreateTime(job.getCreateTime());
        jobPo.setUpdateTime(job.getUpdateTime());
        jobPo.setJobType(job.getJobType());
        jobPo.setProjectID(job.getProjectID());
        jobPo.setStatus(job.getStatus());
        jobPo.setRequestData(job.getRequestData());
        jobPo.setTasksDAG(job.getTasksDAG());
        jobPo.setParties(job.getParties());
        jobPo.setCommon(JSONObject.toJSONString(job.getCommon()));
        return jobPo;
    }

    public String getCommon() {
        return common;
    }

    public void setCommon(String common) {
        this.common = common;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getTasksDAG() {
        return tasksDAG;
    }

    public void setTasksDAG(String tasksDAG) {
        this.tasksDAG = tasksDAG;
    }

    public List<String> getParties() {
        return parties;
    }

    public void setParties(List<String> parties) {
        this.parties = parties;
    }

    @Override
    public String toString() {
        return "JobPo{" +
                "jobName='" + jobName + '\'' +
                ", jobID='" + jobID + '\'' +
                ", submitter='" + submitter + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", jobType='" + jobType + '\'' +
                ", projectID='" + projectID + '\'' +
                ", status='" + status + '\'' +
                ", requestData='" + requestData + '\'' +
                ", tasksDAG='" + tasksDAG + '\'' +
                ", parties=" + parties +
                ", common='" + common + '\'' +
                '}';
    }
}
