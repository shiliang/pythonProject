package com.chainmaker.jobservice.api.model.bo.graph;

/**
 * @author gaokang
 * @date 2022-09-17 19:33
 * @description:拓扑图边
 * @version: 1.0.0
 */

public class TopologyEdge {
    private String source;
    private String target;
    private TopologyEdgeData data;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public TopologyEdgeData getData() {
        return data;
    }

    public void setData(TopologyEdgeData data) {
        this.data = data;
    }
}
