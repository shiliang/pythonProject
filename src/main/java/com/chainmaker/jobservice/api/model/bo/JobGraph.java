package com.chainmaker.jobservice.api.model.bo;

import com.chainmaker.jobservice.api.model.Service;
import com.chainmaker.jobservice.api.model.bo.graph.Dag;
import com.chainmaker.jobservice.api.model.bo.graph.Topology;
import com.chainmaker.jobservice.api.model.bo.job.Job;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.vo.JobGraphVo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-20 17:36
 * @description:
 * @version:
 */
public class JobGraph {
    /** Job信息 */
    private JobInfo jobInfo;
    /** 任务有向无环图 */
    private Dag dag;
    /** 服务拓扑图 */
    private Topology topology;

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public static JobGraph updateFromJobGraphVo(JobGraphVo jobGraphVo, Job job) {
        JobGraph jobGraph = new JobGraph();
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJob(job);
        jobInfo.setTasks(jobGraphVo.getJobInfo().getTasks());
        List<Service> serviceList = new ArrayList<>();
        if (jobGraphVo.getJobInfo().getServices() != null) {
            for (Service service : jobGraphVo.getJobInfo().getServices()) {
                serviceList.add(service);
            }
        }
        jobInfo.setAssetDetailList(jobGraphVo.getJobInfo().getAssetDetailList());
        jobInfo.setServices(serviceList);
        jobGraph.setJobInfo(jobInfo);
        jobGraph.setDag(jobGraphVo.getDag());
        jobGraph.setTopology(jobGraphVo.getTopology());
        return jobGraph;
    }

    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    public Dag getDag() {
        return dag;
    }

    public void setDag(Dag dag) {
        this.dag = dag;
    }

    public Topology getTopology() {
        return topology;
    }

    public void setTopology(Topology topology) {
        this.topology = topology;
    }

    @Override
    public String toString() {
        return "JobGraph{" +
                "jobInfo=" + jobInfo +
                ", dag=" + dag +
                ", topology=" + topology +
                '}';
    }
}
