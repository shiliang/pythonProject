package com.chainmaker.jobservice.core.parser.plans;

public abstract class LogicalPlanVisitor {

    public void visit(XPCPlan node) {
    }
    public void visit(XPCHint node) {
    }
    public void visit(XPCProject node) {
    }
    public void visit(FederatedLearning node) { }

    public void visit(XPCFilter node) {
    }

    public void visit(XPCJoin node) {
    }

    public void visit(XPCSubQuery node) {
    }

    public void visit(XPCTable node) {
    }

    public void visit(NonRelation node) {
    }

    public void visit(XPCAggregate node) {

    }
}
