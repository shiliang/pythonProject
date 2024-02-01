package com.chainmaker.backendservice.model.dto;

/**
 * @author gaokang
 * @date 2022-10-10 19:54
 * @description:服务拓扑图节点
 * @version: 1.0
 */

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isNodeError() {
        return nodeError;
    }

    public void setNodeError(boolean nodeError) {
        this.nodeError = nodeError;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getTotalQueries() {
        return totalQueries;
    }

    public void setTotalQueries(String totalQueries) {
        this.totalQueries = totalQueries;
    }

    public String getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(String successRate) {
        this.successRate = successRate;
    }

    public String getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(String averageTime) {
        this.averageTime = averageTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}
