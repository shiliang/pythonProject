package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.Expression;
import com.chainmaker.jobservice.core.parser.tree.Node;
import org.apache.calcite.rel.RelNode;

import java.util.List;

public class LogicalAggregate extends LogicalPlan{

    private final List<Expression> groupKeys;
    private final Expression aggCalls;
    private final List<LogicalPlan> children;

    public List<Expression> getGroupKeys() {
        return groupKeys;
    }

    public Expression getAggCalls() {
        return aggCalls;
    }

    public LogicalAggregate(List<Expression> groupKeys, Expression aggCalls, List<LogicalPlan> children) {
        this.groupKeys = groupKeys;
        this.aggCalls = aggCalls;
        this.children = children;
    }

    @Override
    public List<LogicalPlan> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        String ans = "Group By [ ";
        for (Expression key : groupKeys) {
            ans += key;
            ans += ", ";
        }
        ans = ans.substring(0, ans.length()-2);
        ans += " ] ";
        if (aggCalls != null) {
            ans += "Having [ " + aggCalls + " ]";
        }
        return ans;
    }

    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    public RelNode accept(LogicalPlanRelVisitor visitor) {
        return visitor.visit(this);
    }
}
