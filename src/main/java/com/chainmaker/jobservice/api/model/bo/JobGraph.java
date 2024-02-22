package com.chainmaker.jobservice.api.model.bo;

import com.chainmaker.jobservice.api.model.bo.graph.Dag;
import com.chainmaker.jobservice.api.model.bo.graph.Topology;
import com.chainmaker.jobservice.api.model.bo.job.Job;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.bo.job.service.ReferValue;
import com.chainmaker.jobservice.api.model.bo.job.service.Service;
import com.chainmaker.jobservice.api.model.vo.JobGraphVo;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;
import com.chainmaker.jobservice.api.model.vo.ValueVo;
import com.chainmaker.jobservice.api.response.ParserException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
            for (ServiceVo serviceVo : jobGraphVo.getJobInfo().getServices()) {
                serviceList.add(Service.serviceVoToService(serviceVo, job.getJobID()));
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
