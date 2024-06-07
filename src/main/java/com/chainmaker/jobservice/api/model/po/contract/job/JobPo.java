package com.chainmaker.jobservice.api.model.po.contract.job;

import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.model.bo.job.Job;
import lombok.Data;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-19 19:06
 * @description:数据库和链上持久化的Job结构
 * @version: 1.0.0
 */

@Data
public class JobPo {
    private String jobName;
    private String jobID;
    private String submitter;
    private String createTime;
    private String updateTime;
    private String jobType;
    private String projectID;
    private Integer status;
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
