package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.Node;

/**
 * basic logical operator:
 * Project
 * Filter
 * Join
 */
public abstract class LogicalPlan extends Node {

    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }
}
