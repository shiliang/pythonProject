package com.chainmaker.jobservice.api.model.graph;

import lombok.Data;

@Data
public class DagEdge {

    private String from;

    private String to;

    private String label;
}
