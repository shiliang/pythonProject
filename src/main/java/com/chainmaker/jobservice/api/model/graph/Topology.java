package com.chainmaker.jobservice.api.model.graph;


import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-17 19:31
 * @description:服务拓扑图
 * @version: 1.0.0
 */

public class Topology {
    private List<TopologyNode> nodeList;
    private List<TopologyEdge> edgeList;

    public List<TopologyNode> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<TopologyNode> nodeList) {
        this.nodeList = nodeList;
    }

    public List<TopologyEdge> getEdgeList() {
        return edgeList;
    }

    public void setEdgeList(List<TopologyEdge> edgeList) {
        this.edgeList = edgeList;
    }
}
