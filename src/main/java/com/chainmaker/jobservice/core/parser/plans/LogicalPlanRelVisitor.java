package com.chainmaker.jobservice.core.parser.plans;

/**
 * @author gaokang
 * @date 2022-10-24 15:46
 * @description:
 * @version:
 */
public abstract class LogicalPlanRelVisitor {
    public LogicalPlan visit(LogicalPlan node) {
        return null;
    }
    public LogicalPlan visit(LogicalProject node) {
        return null;
    }
    public LogicalPlan visit(FederatedLearning node) {
        return null;
    }

    public LogicalPlan visit(LogicalFilter node) {
        return null;
    }

    public LogicalPlan visit(LogicalJoin node) {
        return null;
    }

    public LogicalPlan visit(SubQuery node) {
        return null;
    }

    public LogicalPlan visit(LogicalTable node) {
        return null;
    }

    public LogicalPlan visit(NonRelation node) {
        return null;
    }
}
