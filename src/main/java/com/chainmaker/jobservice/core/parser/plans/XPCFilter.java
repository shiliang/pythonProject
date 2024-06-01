package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.Expression;
import org.apache.calcite.rel.RelNode;

import java.util.List;

/**
 * LogicPlan中表示WHERE条件的节点
 * FROM后面只有一个表时，子节点为UnreservedRelation
 * FROM后面有两个表时，子节点为Join
 * Filter的值为一个条件表达式，可接受子查询sunQuery
 */
public class XPCFilter extends XPCPlan {
    private final Expression condition;
    private final List<XPCPlan> children;

    public XPCFilter(Expression condition, List<XPCPlan> children) {
        this.condition = condition;
        this.children = children;
    }

    public Expression getCondition() {
        return condition;
    }


    @Override
    public List<XPCPlan> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "Filter " + "[ " + condition.toString() + " ]";
    }

    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    public RelNode accept(LogicalPlanRelVisitor visitor) {
        return visitor.visit(this);
    }
}
