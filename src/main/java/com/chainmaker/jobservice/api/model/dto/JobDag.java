/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-07-2022/7/16 18:22
 * @Description: TODO
 * @Version: 1.0
 */

package com.chainmaker.backendservice.model.dto;

public class JobDag {
    private JobInfo jobInfo;
    private Dag dag;

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public Dag getDag() {
        return dag;
    }

    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    public void setDag(Dag dag) {
        this.dag = dag;
    }

    @Override
    public String toString() {
        return "JobDag{" +
                "jobInfo=" + jobInfo +
                ", dag=" + dag +
                '}';
    }
}