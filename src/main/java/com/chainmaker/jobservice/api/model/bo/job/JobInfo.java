package com.chainmaker.jobservice.api.model.bo.job;

import com.chainmaker.jobservice.api.model.bo.job.service.Service;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;
import com.chainmaker.jobservice.api.model.po.contract.JobInfoPo;
import com.chainmaker.jobservice.api.model.po.contract.job.ServicePo;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskPo;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;

import java.util.ArrayList;
import java.util.List;

public class JobInfo {
    private Job job;
    private List<Task> tasks;
    private List<Service> services;

    public static JobInfo converterToJobInfo(JobInfoPo jobInfoPo) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJob(Job.converterToJob(jobInfoPo.getJob()));
        if (jobInfoPo.getTasks() != null) {
            List<Task> tasks = new ArrayList<>();
            for (TaskPo taskPo : jobInfoPo.getTasks()) {
                tasks.add(Task.converterToTask(taskPo));
            }
            jobInfo.setTasks(tasks);
        }
        if (jobInfoPo.getServices() != null) {
            List<Service> services = new ArrayList<>();
            for (ServicePo servicePo : jobInfoPo.getServices()) {
                services.add(Service.servicePoToService(servicePo));
            }
            jobInfo.setServices(services);
        }
        return jobInfo;
    }
    public static JobInfo jobTemplateToJobInfo(JobTemplate jobTemplate) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJob(jobTemplate.getJob());
        jobInfo.setTasks(jobTemplate.getTasks());
        List<Service> services = new ArrayList<>();
        for (ServiceVo serviceVo : jobTemplate.getServices()) {
            Service service = Service.serviceVoToService(serviceVo, jobTemplate.getJob().getJobID());
            services.add(service);
        }
        jobInfo.setServices(services);
        return jobInfo;
    }

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

    @Override
    public String toString() {
        return "JobInfo{" +
                "job=" + job +
                ", tasks=" + tasks +
                ", services=" + services +
                '}';
    }
}