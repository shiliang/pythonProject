package com.chainmaker.jobservice.api.model.bo;

import com.chainmaker.jobservice.api.model.bo.job.Job;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;

import java.util.List;

public class JobRunner {
    private Job job;
    private List<Task> tasks;
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

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<ServiceRunner> getServices() {
        return services;
    }

    public void setServices(List<ServiceRunner> services) {
        this.services = services;
    }
}
