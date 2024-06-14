package com.chainmaker.jobservice.api.model.graph;

import lombok.Data;

/**
 * @author gaokang
 * @date 2022-09-17 19:32
 * @description:拓扑图节点
 * @version: 1.0.0
 */

@Data
public class TopologyNode {
    private String name;

    private String status;

    private boolean nodeError;

    private String dataType;

    private String totalQueries;

    private String successRate;

    private String averageTime;

    private String id;

    private String serviceType;


}
