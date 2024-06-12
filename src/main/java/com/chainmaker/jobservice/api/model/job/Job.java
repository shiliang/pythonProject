package com.chainmaker.jobservice.api.model.job;

import com.chainmaker.jobservice.api.model.AssetDetail;
import com.chainmaker.jobservice.api.model.graph.Dag;
import com.chainmaker.jobservice.api.model.graph.Topology;
import com.chainmaker.jobservice.api.model.job.service.Service;
import com.chainmaker.jobservice.api.model.job.task.Party;
import com.chainmaker.jobservice.api.model.job.task.Task;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Job {
    private String jobName;
    private String jobId;
    private String submitter;
    private String description;
    private String createTime;
    private String updateTime;

    private String createUserId;
    private String createPartyName;
    private String createPartyId;
    private Integer modelType;

    private String projectID;
    private Integer status;
    private String requestData;
    private String tasksDAG;
    private List<Party> partyList;
    private Map<String, String> common;
    private List<Task> taskList;
    private List<Service> serviceList;

    private List<AssetDetail> assetDetailList;

    private Dag dag;
    /** 服务拓扑图 */
    private Topology topology;

}