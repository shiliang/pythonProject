/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-07-2022/7/16 18:18
 * @Description: TODO
 * @Version: 1.0
 */

package com.chainmaker.backendservice.model.dto;

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