package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.FederatedLearningExpression;
import org.apache.calcite.rel.RelNode;

import java.util.List;

public class FederatedLearning extends XPCPlan {
    private final FederatedLearningExpression paramsList;
    private final List<XPCPlan> children;

    public FederatedLearning(FederatedLearningExpression paramsList, List<XPCPlan> children) {
        this.paramsList = paramsList;
        this.children = children;
    }

    public FederatedLearningExpression getParamsList() {
        return paramsList;
    }

    @Override
    public List<XPCPlan> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "FederatedLearning " + paramsList.toString();
    }

    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    public RelNode accept(LogicalPlanRelVisitor visitor) {
        return visitor.visit(this);
    }
}
