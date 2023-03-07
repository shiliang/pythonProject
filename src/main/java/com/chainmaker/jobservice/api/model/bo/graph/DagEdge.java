package com.chainmaker.jobservice.api.model.bo.graph;

public class DagEdge {
    private int from;
    private int to;
    private String label;

    public void setFrom(int from) {
        this.from = from;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public String getLabel() {
        return label;
    }
}
