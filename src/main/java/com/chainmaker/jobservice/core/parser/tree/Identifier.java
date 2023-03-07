package com.chainmaker.jobservice.core.parser.tree;

import java.util.List;

/**
 * 叶子节点
 */
public class Identifier extends Expression {
    private final String identifier;

    public Identifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public List<Expression> getChildren() {
        return null;
    }

    @Override
    public String toString() {
        return identifier;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
