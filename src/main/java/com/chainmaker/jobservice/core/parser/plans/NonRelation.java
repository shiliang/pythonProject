package com.chainmaker.jobservice.core.parser.plans;

import org.apache.calcite.rel.RelNode;

import java.util.List;

public class NonRelation extends XPCPlan {

    public NonRelation() {}


    @Override
    public List<XPCPlan> getChildren() {
        return null;
    }

    @Override
    public String toString() {
        return "NonRelation";
    }

    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    public RelNode accept(LogicalPlanRelVisitor visitor) {
        return visitor.visit(this);
    }
}
