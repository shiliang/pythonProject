package com.chainmaker.jobservice.api.model.po.contract;

import com.alibaba.fastjson.JSON;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.bo.job.service.Service;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;
import com.chainmaker.jobservice.api.model.po.contract.job.JobPo;
import com.chainmaker.jobservice.api.model.po.contract.job.ServicePo;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskPo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-20 10:47
 * @description:上链存储job结构
 * @version: 1.0.0
 */

public class JobInfoPo {
    private String applicationID;
    private JobPo job;
    private List<TaskPo> tasks;
    private List<ServicePo> services;

    public static JobInfoPo converterToJobInfoPo(JobInfo jobInfo) {
        JobInfoPo jobInfoPo = new JobInfoPo();
        jobInfoPo.setApplicationID(jobInfo.getJob().getJobID());
        jobInfoPo.setJob(JobPo.converterToJobPo(jobInfo.getJob()));

        if (jobInfo.getTasks() != null) {
            List<TaskPo> taskPoList = new ArrayList<>();
            for (Task task : jobInfo.getTasks()) {
                TaskPo taskPo = TaskPo.converterToTaskPo(task);
                taskPoList.add(taskPo);
            }
            jobInfoPo.setTasks(taskPoList);
        }

        if (jobInfo.getServices() != null) {
            List<ServicePo> servicePoList = new ArrayList<>();
            for (Service service : jobInfo.getServices()) {
                ServicePo servicePo = ServicePo.converterToServicePo(service);
                servicePoList.add(servicePo);
            }
            jobInfoPo.setServices(servicePoList);
        }
        return jobInfoPo;
    }

    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("applicationID", this.getApplicationID().getBytes());
        contractParams.put("job", JSON.toJSONString(this.getJob()).getBytes());
        contractParams.put("tasks", JSON.toJSONString(this.getTasks()).getBytes());
        contractParams.put("services", JSON.toJSONString(this.getServices()).getBytes());
        return contractParams;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public JobPo getJob() {
        return job;
    }

    public void setJob(JobPo job) {
        this.job = job;
    }

    public List<TaskPo> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskPo> tasks) {
        this.tasks = tasks;
    }

    public List<ServicePo> getServices() {
        return services;
    }

    public void setServices(List<ServicePo> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "JobInfoPo{" +
                "applicationID='" + applicationID + '\'' +
                ", job=" + job +
                ", tasks=" + tasks +
                ", services=" + services +
                '}';
    }
}
