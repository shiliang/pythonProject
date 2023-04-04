package com.chainmaker.jobservice.api.model.bo.job;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.model.po.contract.job.JobPo;

import java.util.List;
import java.util.Map;

/**
 * @author gaokang
 * @date 2022-08-13 11:43
 * @description:联邦SQL生成的任务信息
 * @version: 1.0.0
 */

public class Job {
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
    private Map<String, String> common;

    public static Job converterToJob(JobPo jobPo) {
        Job job = new Job();
        job.setJobName(jobPo.getJobName());
        job.setJobID(jobPo.getJobID());
        job.setSubmitter(jobPo.getSubmitter());
        job.setCreateTime(jobPo.getCreateTime());
        job.setUpdateTime(jobPo.getUpdateTime());
        job.setJobType(jobPo.getJobType());
        job.setProjectID(jobPo.getProjectID());
        job.setStatus(jobPo.getStatus());
        job.setRequestData(jobPo.getRequestData());
        job.setTasksDAG(jobPo.getTasksDAG());
        job.setParties(jobPo.getParties());
        job.setCommon(JSONObject.parseObject(jobPo.getCommon(), Map.class, Feature.OrderedField));
        return job;
    }

    public Map<String, String> getCommon() {
        return common;
    }

    public void setCommon(Map<String, String> common) {
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
        return "Job{" +
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
                ", common=" + common +
                '}';
    }
}
