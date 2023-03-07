package com.chainmaker.jobservice.core.parser.tree;

public abstract class Expression extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
