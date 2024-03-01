package com.chainmaker.jobservice.api.model.bo;

import com.chainmaker.jobservice.api.model.bo.job.Job;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;
import lombok.Data;

import java.util.List;

@Data
public class JobRunner {
    private Job job;
    private List<TaskRunner> tasks;
    private List<ServiceRunner> services;
//    public static JobRunner JobInfoToJobRunner(JobInfo jobInfo) {
//        JobRunner jobRunner = new JobRunner();
//        jobRunner.setJob(jobInfo.getJob());
//        if (jobInfo.getTasks() != null) {
//            jobRunner.setTasks(jobInfo.getTasks());
//        }
//        if (jobInfo.getServices().size() > 0) {
//
//        }
//        return jobRunner;
//    }
//
//    public static List<ServiceRunner> converterToServiceRunners(List<Service> services) {
//        return null;
//    }
}
