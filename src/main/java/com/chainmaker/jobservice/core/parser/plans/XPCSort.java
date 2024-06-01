package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.Expression;
import com.chainmaker.jobservice.core.parser.tree.Node;
import org.apache.calcite.rel.RelNode;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-14 10:47
 * @description:Sort算子结构
 * @version: 1.0.0
 */

public class XPCSort extends XPCPlan {
    private Expression expression;
    private XPCPlan child;

    public XPCSort(Expression expression, XPCPlan child) {
        this.expression = expression;
        this.child = child;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public XPCPlan getChild() {
        return child;
    }

    public void setChild(XPCPlan child) {
        this.child = child;
    }


    @Override
    public List<? extends Node> getChildren() {
        return null;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    public RelNode accept(LogicalPlanRelVisitor visitor) {
        return visitor.visit(this);
    }
}
