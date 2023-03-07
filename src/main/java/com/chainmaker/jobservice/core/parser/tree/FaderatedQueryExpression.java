package com.chainmaker.jobservice.core.parser.tree;

import java.util.List;

public class FaderatedQueryExpression extends Expression {
    private final List<NamedExpression> values;

    public FaderatedQueryExpression(List<NamedExpression> values) {
        this.values = values;
    }

    public List<NamedExpression> getValues() {
        return values;
    }

    @Override
    public List<NamedExpression> getChildren() {
        return values;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
