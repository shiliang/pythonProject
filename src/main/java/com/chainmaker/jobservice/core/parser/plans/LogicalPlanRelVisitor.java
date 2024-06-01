package com.chainmaker.jobservice.core.parser.plans;

import org.apache.calcite.rel.RelNode;

/**
 * @author gaokang
 * @date 2022-10-24 15:46
 * @description:
 * @version:
 */
public abstract class LogicalPlanRelVisitor {
    public RelNode visit(XPCPlan node) {
        return null;
    }

    public RelNode visit(XPCHint node) {
        return null;
    }

    public RelNode visit(XPCProject node) {
        return null;
    }

    public RelNode visit(FederatedLearning node) {
        return null;
    }

    public RelNode visit(XPCFilter node) {
        return null;
    }

    public RelNode visit(XPCJoin node) {
        return null;
    }

    public RelNode visit(XPCSubQuery node) {
        return null;
    }

    public RelNode visit(XPCTable node) {
        return null;
    }

    public RelNode visit(NonRelation node) {
        return null;
    }

    public RelNode visit(XPCSort node) { return null; }

    public RelNode visit(XPCAggregate node) { return null; }
}
