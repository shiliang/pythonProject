package com.chainmaker.jobservice.api.model.po.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author gaokang
 * @date 2022-10-31 19:52
 * @description:
 * @version:
 */
public class Record {
    @JsonProperty(value = "StartTimeStamp")
    private Integer StartTimeStamp;
    @JsonProperty(value = "EndTimeStamp")
    private Integer EndTimeStamp;
    @JsonProperty(value = "NodeDataTotal")
    private String NodeDataTotal;
    @JsonProperty(value = "NodeSuccessTotal")
    private String NodeSuccessTotal;
    @JsonProperty(value = "NodeProcessAvgTime")
    private Integer NodeProcessAvgTime;

    public Integer getStartTimeStamp() {
        return StartTimeStamp;
    }

    public void setStartTimeStamp(Integer startTimeStamp) {
        StartTimeStamp = startTimeStamp;
    }

    public Integer getEndTimeStamp() {
        return EndTimeStamp;
    }

    public void setEndTimeStamp(Integer endTimeStamp) {
        EndTimeStamp = endTimeStamp;
    }

    public String getNodeDataTotal() {
        return NodeDataTotal;
    }

    public void setNodeDataTotal(String nodeDataTotal) {
        NodeDataTotal = nodeDataTotal;
    }

    public String getNodeSuccessTotal() {
        return NodeSuccessTotal;
    }

    public void setNodeSuccessTotal(String nodeSuccessTotal) {
        NodeSuccessTotal = nodeSuccessTotal;
    }

    public Integer getNodeProcessAvgTime() {
        return NodeProcessAvgTime;
    }

    public void setNodeProcessAvgTime(Integer nodeProcessAvgTime) {
        NodeProcessAvgTime = nodeProcessAvgTime;
    }

    @Override
    public String toString() {
        return "Record{" +
                "StartTimeStamp=" + StartTimeStamp +
                ", EndTimeStamp=" + EndTimeStamp +
                ", NodeDataTotal='" + NodeDataTotal + '\'' +
                ", NodeSuccessTotal='" + NodeSuccessTotal + '\'' +
                ", NodeProcessAvgTime=" + NodeProcessAvgTime +
                '}';
    }
}
