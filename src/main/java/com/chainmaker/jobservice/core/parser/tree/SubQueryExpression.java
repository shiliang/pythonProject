package com.chainmaker.jobservice.core.parser.tree;

import com.chainmaker.jobservice.core.parser.plans.LogicalPlan;

import java.util.ArrayList;
import java.util.List;

public class SubQueryExpression extends Expression {
    private final LogicalPlan plan;

    public SubQueryExpression(LogicalPlan plan) {
        this.plan = plan;
    }

    public LogicalPlan getPlan() {
        return plan;
    }

    @Override
    public List<LogicalPlan> getChildren() {
        List<LogicalPlan> children = new ArrayList<>();
        children.add(plan);
        return children;
    }

    @Override
    public String toString() {
        return plan.toString();
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
