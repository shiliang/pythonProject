package com.chainmaker.jobservice.core.parser.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SelectQueryExpression extends Expression {
    private final List<NamedExpression> values;

    public SelectQueryExpression(List<NamedExpression> values) {
        this.values = values;
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
