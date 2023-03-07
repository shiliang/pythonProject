package com.chainmaker.jobservice.api.model.bo.job;

import com.chainmaker.jobservice.api.model.bo.job.service.Service;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;

import java.util.List;

public class JobTemplate {
    private Job job;
    private List<Task> tasks;
    private List<ServiceVo> services;

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

    public List<ServiceVo> getServices() {
        return services;
    }

    public void setServices(List<ServiceVo> services) {
        this.services = services;
    }
}
