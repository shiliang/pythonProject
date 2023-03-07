package com.chainmaker.jobservice.core.parser.tree;

import java.util.ArrayList;
import java.util.List;

public class LogicalExpression extends Expression {
    public enum Operator
    {
        AND,OR
    }

    private final Expression left;
    private final Expression right;
    private final Operator operator;

    public LogicalExpression(Expression left, Expression right, Operator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public Expression getLeft() {
        return left;
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> children = new ArrayList<>();
        children.add(left);
        children.add(right);
        return children;
    }

    @Override
    public String toString() {
        return left.toString() + " " + operator +" " + right.toString();
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
