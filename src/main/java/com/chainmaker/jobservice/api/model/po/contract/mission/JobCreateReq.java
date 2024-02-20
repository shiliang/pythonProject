package com.chainmaker.jobservice.api.model.po.contract.mission;

import lombok.Data;

import java.util.HashMap;

@Data
public class JobCreateReq {
   private String applicationID;
    private String tasks;
    private String job;
    private String missionID;

    private String services;

    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("applicationID", this.getApplicationID().getBytes());
        contractParams.put("tasks", this.getTasks().getBytes());
        contractParams.put("job", this.getJob().getBytes());
        contractParams.put("services", this.getServices().getBytes());
        contractParams.put("missionID", this.getMissionID().getBytes());
        return contractParams;
    }
    
    public String getApplicationID() {
        return applicationID;
    }

    public String getTasks() {
        return tasks;
    }

    public String getJob() {
        return job;
    }

    public String getMissionID() {
        return missionID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public void setMissionID(String missionID) {
        this.missionID = missionID;
    }

    public String toString() {
        return "JobCreateReq(applicationID=" + this.getApplicationID() + ", tasks=" + this.getTasks() + ", job=" + this.getJob() + ", missionID=" + this.getMissionID() + ")";
    }
}
