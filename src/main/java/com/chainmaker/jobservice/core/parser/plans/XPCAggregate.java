package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.Expression;
import com.chainmaker.jobservice.core.parser.tree.FunctionCallExpression;
import com.chainmaker.jobservice.core.parser.tree.Identifier;
import com.chainmaker.jobservice.core.parser.tree.NamedExpression;
import org.apache.calcite.rel.RelNode;

import java.util.HashSet;
import java.util.List;

public class XPCAggregate extends XPCPlan {

    private final List<Expression> groupKeys;   // 分组键
    private final List<NamedExpression> aggCallList;          // 聚合列
    private final List<XPCPlan> children;

    public List<Expression> getGroupKeys() {
        return groupKeys;
    }

    public List<NamedExpression> getAggCallList() {
        return aggCallList;
    }

    public XPCAggregate(List<Expression> groupKeys, List<NamedExpression> aggCallList, List<XPCPlan> children) {
        this.groupKeys = groupKeys;
        this.aggCallList = aggCallList;
        this.children = children;
    }

    public void addAggCallAll(HashSet<NamedExpression> calls) {
        aggCallList.addAll(calls);
    }

    @Override
    public List<XPCPlan> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder("Group By [ ");
        for (Expression key : groupKeys) {
            ans.append(key).append(", ");
        }
        ans = new StringBuilder(ans.substring(0, ans.length() - 2));
        ans.append(" ] ");

        ans.append("Local Agg = [");
        for (NamedExpression e : aggCallList) {
            ans.append(" ")
                    .append(e.getExpression())
                    .append(" as ")
                    .append(e.getIdentifier())
                    .append(" ");
        }
        ans.append("]");
        return ans.toString();
    }

    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    public RelNode accept(LogicalPlanRelVisitor visitor) {
        return visitor.visit(this);
    }
}
