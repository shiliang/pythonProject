package com.chainmaker.jobservice.core.parser.tree;

import java.util.List;

public abstract class Node {
    protected <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public abstract List<? extends Node> getChildren();

    public abstract String toString();
}
