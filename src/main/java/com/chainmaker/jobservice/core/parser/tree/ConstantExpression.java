package com.chainmaker.jobservice.core.parser.tree;

import java.util.List;

/**
 * 叶子节点
 */
public class ConstantExpression extends Expression {
    private final String value;

    public ConstantExpression(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public List<Expression> getChildren() {
        return null;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
