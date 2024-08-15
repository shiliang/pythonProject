package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.FederatedLearningExpression;
import org.apache.calcite.rel.RelNode;

import java.util.List;

public class FederatedLearning extends XPCPlan {
    private final FederatedLearningExpression exprList;
    private final List<XPCPlan> children;

    public FederatedLearning(FederatedLearningExpression exprList, List<XPCPlan> children) {
        this.exprList = exprList;
        this.children = children;
    }

    public FederatedLearningExpression getExprList() {
        return exprList;
    }

    @Override
    public List<XPCPlan> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "FederatedLearning " + exprList.toString();
    }

    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    public RelNode accept(LogicalPlanRelVisitor visitor) {
        return visitor.visit(this);
    }
}
