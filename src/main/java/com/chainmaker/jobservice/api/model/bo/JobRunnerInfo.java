package com.chainmaker.jobservice.api.model.bo;

import com.chainmaker.jobservice.api.model.AssetDetail;
import com.chainmaker.jobservice.api.model.Service;
import com.chainmaker.jobservice.api.model.bo.job.Job;
import com.chainmaker.jobservice.api.model.bo.job.JobTemplate;
import com.chainmaker.jobservice.api.model.bo.job.task.Party;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;
import com.chainmaker.jobservice.api.model.bo.job.task.TaskInputData;
import com.chainmaker.jobservice.api.model.po.contract.JobInfoPo;
import com.chainmaker.jobservice.api.model.po.contract.job.ServicePo;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskPo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Data
public class JobRunnerInfo {
    private Job job;
    private List<TaskRunner> tasks;
    private List<ServiceRunner> services;

    private List<AssetDetail> assetDetailList;

    public static JobRunnerInfo converterToJobInfo(JobInfoPo jobInfoPo, String orgId) {
        JobRunnerInfo jobInfo = new JobRunnerInfo();
        jobInfo.setJob(Job.converterToJob(jobInfoPo.getJob()));
        if (jobInfoPo.getTasks() != null) {
            List<TaskRunner> tasks = new ArrayList<>();
            for (TaskPo taskPo : jobInfoPo.getTasks()) {
                tasks.add(TaskRunner.converterToTaskRunner(taskPo));
            }
            jobInfo.setTasks(tasks);
        }
        if (jobInfoPo.getServices() != null) {
            List<ServiceRunner> services = new ArrayList<>();
            for (ServicePo servicePo : jobInfoPo.getServices()) {
                ServiceRunner serviceRunner = ServiceRunner.converterToServiceRunner(servicePo, orgId);
                if (null != serviceRunner) {
                    services.add(serviceRunner);
                }
            }
            jobInfo.setServices(services);
        }
        return jobInfo;
    }
}