package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.Expression;
import com.chainmaker.jobservice.core.parser.tree.Node;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-14 10:47
 * @description:Sort算子结构
 * @version: 1.0.0
 */

public class LogicalSort extends LogicalPlan {
    private Expression expression;
    private LogicalPlan child;

    public LogicalSort(Expression expression, LogicalPlan child) {
        this.expression = expression;
        this.child = child;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public LogicalPlan getChild() {
        return child;
    }

    public void setChild(LogicalPlan child) {
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
}
