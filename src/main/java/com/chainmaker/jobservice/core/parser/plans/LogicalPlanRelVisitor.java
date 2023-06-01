package com.chainmaker.jobservice.core.parser.plans;

import org.apache.calcite.rel.RelNode;

/**
 * @author gaokang
 * @date 2022-10-24 15:46
 * @description:
 * @version:
 */
public abstract class LogicalPlanRelVisitor {
    public RelNode visit(LogicalPlan node) {
        return null;
    }

    public RelNode visit(LogicalHint node) {
        return null;
    }

    public RelNode visit(LogicalProject node) {
        return null;
    }

    public RelNode visit(FederatedLearning node) {
        return null;
    }

    public RelNode visit(LogicalFilter node) {
        return null;
    }

    public RelNode visit(LogicalJoin node) {
        return null;
    }

    public RelNode visit(SubQuery node) {
        return null;
    }

    public RelNode visit(LogicalTable node) {
        return null;
    }

    public RelNode visit(NonRelation node) {
        return null;
    }

    public RelNode visit(LogicalSort node) { return null; }

    public RelNode visit(LogicalAggregate node) { return null; }
}
