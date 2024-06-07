package com.chainmaker.jobservice.api.model.bo.job;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.model.po.contract.job.JobPo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author gaokang
 * @date 2022-08-13 11:43
 * @description:联邦SQL生成的任务信息
 * @version: 1.0.0
 */

@Data
public class Job {
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
