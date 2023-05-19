package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.DereferenceExpression;
import com.chainmaker.jobservice.core.parser.tree.Expression;
import com.chainmaker.jobservice.core.parser.tree.Node;
import org.apache.calcite.rel.RelNode;

import java.util.HashSet;
import java.util.List;

public class LogicalAggregate extends LogicalPlan{

    private final List<Expression> groupKeys;   // 分组键
    private final List<Expression> aggCallList;          // 聚合列
    private final List<LogicalPlan> children;

    public List<Expression> getGroupKeys() {
        return groupKeys;
    }

    public List<Expression> getAggCallList() {
        return aggCallList;
    }

    public LogicalAggregate(List<Expression> groupKeys, List<Expression> aggCallList, List<LogicalPlan> children) {
        this.groupKeys = groupKeys;
        this.aggCallList = aggCallList;
        this.children = children;
    }

    public void addAggCallAll(HashSet<Expression> calls) {
        aggCallList.addAll(calls);
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

        ans += "agg = [ ";
        for (Expression e : aggCallList) {
            ans += e;
        }
        ans += " ]";
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
