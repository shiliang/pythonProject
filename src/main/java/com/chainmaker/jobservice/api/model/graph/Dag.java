package com.chainmaker.jobservice.api.model.graph;

import java.util.List;

public class Dag {
    private List<DagNode> nodeList;
    private List<DagEdge> edgeList;

    public List<DagNode> getNodeList() {
        return nodeList;
    }

    public List<DagEdge> getEdgeList() {
        return edgeList;
    }

    public void setNodeList(List<DagNode> nodeList) {
        this.nodeList = nodeList;
    }

    public void setEdgeList(List<DagEdge> edgeList) {
        this.edgeList = edgeList;
    }
}
