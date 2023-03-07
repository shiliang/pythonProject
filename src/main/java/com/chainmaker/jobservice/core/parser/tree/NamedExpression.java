package com.chainmaker.jobservice.core.parser.tree;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-11 13:38
 * @description:查询表达式
 * @version: 1.0.0
 */

public class NamedExpression extends Expression {
    private Expression expression;
    private Identifier identifier;

    public NamedExpression(Expression expression, Identifier identifier) {
        this.expression = expression;
        this.identifier = identifier;
    }

    public NamedExpression(Expression expression) {
        this.expression = expression;
        this.identifier = new Identifier("");
    }

    public Expression getExpression() {
        return expression;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public List<? extends Node> getChildren() {
        return null;
    }

    @Override
    public String toString() {
        if (identifier.toString().equals("")) {
            return expression.toString();
        } else {
            return expression.toString() + " " + identifier.toString();
        }
    }
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
