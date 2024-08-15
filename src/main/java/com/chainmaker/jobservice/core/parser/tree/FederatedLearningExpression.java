package com.chainmaker.jobservice.core.parser.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FederatedLearningExpression extends Expression {
    private final List<FlExpression> fl;
    private final List<List<FlExpression>> exprs;

    @Override
    public List<? extends Node> getChildren() {
        return null;
    }

    @Override
    public String toString() {
        return "FederatedLearningExpression{" +
                "fl=" + fl +
                ", model=" + exprs +
                '}';
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
