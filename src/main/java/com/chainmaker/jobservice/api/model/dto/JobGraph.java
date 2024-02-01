package com.chainmaker.backendservice.model.dto;

import com.alibaba.fastjson.JSON;
import com.chainmaker.api.model.Job;
import com.chainmaker.backendservice.model.vo.JobGraphVo;

/**
 * @author gaokang
 * @date 2022-10-10 19:47
 * @description:链上Job信息以及拓扑图
 * @version: 1.0
 */

public class JobGraph {
    /** Job信息 */
    private JobInfo jobInfo;
    /** 任务有向无环图 */
    private Dag dag;
    /** 服务拓扑图 */
    private Topology topology;

    public static JobGraph converterToJobGraph(JobGraphVo jobGraphVo) {
        return JSON.parseObject(JSON.toJSONString(jobGraphVo), JobGraph.class);
    }

    public JobInfo getJobInfo() {
        return jobInfo;
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
}
