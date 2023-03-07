package com.chainmaker.jobservice.core.parser.tree;

import java.util.List;

public class FlLabelExpression extends Expression {
    private final List<FlExpression> values;

    public FlLabelExpression(List<FlExpression> values) {
        this.values = values;
    }

    public List<FlExpression> getValues() {
        return values;
    }

    @Override
    public List<FlExpression> getChildren() {
        return (List<FlExpression>) values;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
