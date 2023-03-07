package com.chainmaker.jobservice.core.parser.tree;

import java.util.List;

public class FunctionCallExpression extends Expression {
    public FunctionCallExpression(String function, List<Expression> expressions) {
        this.function = function;
        this.expressions = expressions;
    }

    private final String function;
    private final List<Expression> expressions;

    public String getFunction() {
        return function;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public String toString() {
        return function + expressions.toString();
    }
    @Override
    public List<Expression> getChildren() {
        return expressions;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
