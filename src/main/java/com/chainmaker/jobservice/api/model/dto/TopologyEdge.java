package com.chainmaker.backendservice.model.dto;

/**
 * @author gaokang
 * @date 2022-10-10 19:55
 * @description:服务拓扑图边
 * @version: 1.0
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
