package com.chainmaker.backendservice.model.dto;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-10-10 19:48
 * @description:Job信息
 * @version: 1.0
 */

public class JobInfo {
    private Job job;
    private List<Task> tasks;
    private List<Service> services;

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

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }
}
