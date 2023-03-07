package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.FederatedLearningExpression;

import java.util.ArrayList;
import java.util.List;

public class FederatedLearning extends LogicalPlan {
    private final FederatedLearningExpression paramsList;
    private final List<LogicalPlan> children;

    public FederatedLearning(FederatedLearningExpression paramsList, List<LogicalPlan> children) {
        this.paramsList = paramsList;
        this.children = children;
    }

    public FederatedLearningExpression getParamsList() {
        return paramsList;
    }

    @Override
    public List<LogicalPlan> getChildren() {
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
}
