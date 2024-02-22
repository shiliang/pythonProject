package com.chainmaker.jobservice.api.model.vo;

import com.chainmaker.jobservice.api.model.bo.job.Job;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.bo.job.JobTemplate;
import com.chainmaker.jobservice.api.model.bo.job.service.Service;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-22 21:58
 * @description:
 * @version:
 */

public class JobInfoVo {
    private Job job;
    private List<Task> tasks;
    private List<ServiceVo> services;

    public static JobInfoVo converterToJobInfoVo(JobTemplate jobTemplate) {
        JobInfoVo jobInfoVo = new JobInfoVo();
        jobInfoVo.setJob(jobTemplate.getJob());
        if (jobTemplate.getTasks() != null) {
            jobInfoVo.setTasks(jobTemplate.getTasks());
        }
        if (jobTemplate.getServices().size() > 0) {
            jobInfoVo.setServices(jobTemplate.getServices());
        }
        return jobInfoVo;
    }
    public static JobInfoVo jobInfoToJobInfoVo(JobInfo jobInfo) {

        JobInfoVo jobInfoVo = new JobInfoVo();
        jobInfoVo.setJob(jobInfo.getJob());
        if (jobInfo.getTasks() != null) {
            jobInfoVo.setTasks(jobInfo.getTasks());
        }
        if (jobInfo.getServices() != null) {
            if (jobInfo.getServices().size() > 0) {
                String templateId = "1";
                if (jobInfo.getJob().getCommon().get("method_name").equals("pir")) {
                    templateId = "2";
                } else if (jobInfo.getJob().getCommon().get("method_name").equals("teepir")) {
                    templateId = "3";
                }
                List<ServiceVo> serviceVos = new ArrayList<>();
//                for (Service service : jobInfo.getServices()) {
//                    serviceVos.add(ServiceVo.serviceToServiceVo(service, templateId));
//                }
                jobInfoVo.setServices(serviceVos);
            }
        }
        return jobInfoVo;
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

    public List<ServiceVo> getServices() {
        return services;
    }

    public void setServices(List<ServiceVo> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "JobInfoVo{" +
                "job=" + job +
                ", tasks=" + tasks +
                ", services=" + services +
                '}';
    }

}
