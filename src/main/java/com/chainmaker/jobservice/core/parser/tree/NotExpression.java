package com.chainmaker.jobservice.core.parser.tree;

import java.util.ArrayList;
import java.util.List;

public class NotExpression extends Expression {
    private final Expression value;

    public NotExpression(Expression value) {
        this.value = value;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> children = new ArrayList<>();
        children.add(value);
        return children;
    }

    @Override
    public String toString() {
        return "NOT " + value.toString();
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
