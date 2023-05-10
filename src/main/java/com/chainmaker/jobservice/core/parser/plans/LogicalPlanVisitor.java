package com.chainmaker.jobservice.core.parser.plans;

import org.apache.calcite.rel.RelNode;

public abstract class LogicalPlanVisitor {

    public void visit(LogicalPlan node) {
    }
    public void visit(LogicalHint node) {
    }
    public void visit(LogicalProject node) {
    }
    public void visit(FederatedLearning node) { }

    public void visit(LogicalFilter node) {
    }

    public void visit(LogicalJoin node) {
    }

    public void visit(SubQuery node) {
    }

    public void visit(LogicalTable node) {
    }

    public void visit(NonRelation node) {
    }

    public void visit(LogicalAggregate node) {

    }
}
