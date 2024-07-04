package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.Node;
import org.apache.calcite.rel.RelNode;

import java.util.List;

/**
 * basic logical operator:
 * Project
 * Filter
 * Join
 */
public abstract class XPCPlan extends Node {

    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    public RelNode accept(LogicalPlanRelVisitor visitor) {
        return visitor.visit(this);
    }

    public List<XPCPlan> getChildren() {
        return null;
    }
}
