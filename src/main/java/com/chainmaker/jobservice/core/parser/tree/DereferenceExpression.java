package com.chainmaker.jobservice.core.parser.tree;

import java.util.ArrayList;
import java.util.List;

public class DereferenceExpression extends Expression {
    private final Expression base;
    private final String fieldName;

    public DereferenceExpression(Expression base, String fieldName) {
        this.base = base;
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Expression getBase() {
        return base;
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> children = new ArrayList<>();
        children.add(base);
        return children;
    }

    @Override
    public String toString() {
        return base.toString()+"."+fieldName;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
