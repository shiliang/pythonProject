package com.chainmaker.jobservice.api.model.bo.job;

import com.chainmaker.jobservice.api.model.AssetDetail;
import com.chainmaker.jobservice.api.model.Service;
import com.chainmaker.jobservice.api.model.bo.job.task.Party;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;
import com.chainmaker.jobservice.api.model.bo.job.task.TaskInputData;
import com.chainmaker.jobservice.api.model.po.contract.JobInfoPo;
import com.chainmaker.jobservice.api.model.po.contract.job.ServicePo;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskPo;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class JobInfo {
    private Job job;
    private List<Task> tasks;
    private List<Service> services;

    private List<AssetDetail> assetDetailList;
    public void update() {
        for (Task task : this.tasks) {
            if (task.getModule().getParams().containsKey("domainID")) {
                List<Party> parties = new ArrayList<>();
                HashSet<String> partySet = new HashSet<>();
                for (TaskInputData inputData : task.getInput().getData()) {
                    partySet.add(inputData.getDomainID());
                }
                partySet.add(task.getModule().getParams().get("domainID").toString());
                for (String value : partySet) {
                    Party party = new Party();
                    party.setServerInfo(null);
                    party.setStatus(null);
                    party.setTimestamp(null);
                    party.setPartyID(value);
                    parties.add(party);
                }
                task.setParties(parties);

                if (!this.job.getParties().contains(task.getModule().getParams().get("domainID").toString())) {
                    this.job.getParties().add(task.getModule().getParams().get("domainID").toString());
                }
            }
        }
    }

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
//                services.add(Service.servicePoToService(servicePo));
            }
            jobInfo.setServices(services);
        }
        return jobInfo;
    }
    public static JobInfo jobTemplateToJobInfo(JobTemplate jobTemplate) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJob(jobTemplate.getJob());
        jobInfo.setTasks(jobTemplate.getTasks());
        jobInfo.setServices(jobTemplate.getServices());
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

    public List<AssetDetail> getAssetDetailList() {
        return assetDetailList;
    }

    public void setAssetDetailList(List<AssetDetail> assetDetailList) {
        this.assetDetailList = assetDetailList;
    }

    @Override
    public String toString() {
        return "JobInfo{" +
                "job=" + job +
                ", tasks=" + tasks +
                ", services=" + services +
                ", assetDetailList=" + assetDetailList +
                '}';
    }
}