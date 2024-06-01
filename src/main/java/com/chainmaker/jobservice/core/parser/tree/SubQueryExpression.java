package com.chainmaker.jobservice.core.parser.tree;

import com.chainmaker.jobservice.core.parser.plans.XPCPlan;

import java.util.ArrayList;
import java.util.List;

public class SubQueryExpression extends Expression {
    private final XPCPlan plan;

    public SubQueryExpression(XPCPlan plan) {
        this.plan = plan;
    }

    public XPCPlan getPlan() {
        return plan;
    }

    @Override
    public List<XPCPlan> getChildren() {
        List<XPCPlan> children = new ArrayList<>();
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
