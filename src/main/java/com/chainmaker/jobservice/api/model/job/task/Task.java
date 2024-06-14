package com.chainmaker.jobservice.api.model.job.task;

import lombok.Data;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-13 10:10
 * @description:联邦SQL生成的执行任务结构
 * @version: 1.0.0
 */
@Data
public class Task {
    private String version;
    private String jobId;
    private String taskName;
    private String taskId;
    private String taskLabel;
    private Integer status;
    private String updateTime;
    private String createTime;

    private Module module;
    private Input input;
    private List<Output> outputList;
    private List<Party> partyList;


    @Override
    public String toString() {
        return "Task{" +
                "version='" + version + '\'' +
                ", jobID='" + jobId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", status='" + status + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", createTime='" + createTime + '\'' +
                ", module=" + module +
                ", taskLabel=" + taskLabel +
                ", input=" + input +
                ", output=" + outputList +
                ", parties=" + partyList +
                '}';
    }
}
