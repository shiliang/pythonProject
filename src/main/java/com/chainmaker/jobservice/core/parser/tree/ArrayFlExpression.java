package com.chainmaker.jobservice.core.parser.tree;

import java.util.List;

public class ArrayFlExpression extends Expression {
    private final List<FlExpression> values;

    public ArrayFlExpression(List<FlExpression> values) {
        this.values = values;
    }

    public List<FlExpression> getValues() {
        return values;
    }

    @Override
    public List<FlExpression> getChildren() {
        return values;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
