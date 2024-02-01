package com.chainmaker.backendservice.model.dto;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-10-10 19:54
 * @description:服务拓扑图
 * @version: 1.0
 */

public class Topology {
    private List<TopologyNode> nodes;
    private List<TopologyEdge> edges;

    public List<TopologyNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<TopologyNode> nodes) {
        this.nodes = nodes;
    }

    public List<TopologyEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<TopologyEdge> edges) {
        this.edges = edges;
    }
}
