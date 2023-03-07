package com.chainmaker.jobservice.api.model.bo.graph;

import java.util.List;

public class Dag {
    private List<DagNode> nodes;
    private List<DagEdge> edges;

    public List<DagNode> getNodes() {
        return nodes;
    }

    public List<DagEdge> getEdges() {
        return edges;
    }

    public void setNodes(List<DagNode> nodes) {
        this.nodes = nodes;
    }

    public void setEdges(List<DagEdge> edges) {
        this.edges = edges;
    }
}
