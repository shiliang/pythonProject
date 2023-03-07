package com.chainmaker.jobservice.api.model.vo;

import com.chainmaker.jobservice.api.model.bo.graph.Dag;
import com.chainmaker.jobservice.api.model.bo.graph.Topology;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;

/**
 * @author gaokang
 * @date 2022-09-23 02:09
 * @description:
 * @version:
 */

public class JobGraphVo {
    /** Job信息 */
    private JobInfoVo jobInfo;
    /** 任务有向无环图 */
    private Dag dag;
    /** 服务拓扑图 */
    private Topology topology;

    public JobInfoVo getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobInfoVo jobInfo) {
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
        return "JobGraphVo{" +
                "jobInfo=" + jobInfo +
                ", dag=" + dag +
                ", topology=" + topology +
                '}';
    }
}
