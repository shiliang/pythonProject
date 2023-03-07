package com.chainmaker.jobservice.core.parser.tree;

import java.util.ArrayList;
import java.util.List;

public class ArithmeticUnaryExpression extends Expression {
    @Override
    public List<Expression> getChildren() {
        List<Expression> children = new ArrayList<>();
        children.add(value);
        return children;
    }

    @Override
    public String toString() {
        return sign + value.toString();
    }

    public enum Sign {
        PLUS, MINUS
    }
    private final Sign sign;
    private final Expression value;

    public ArithmeticUnaryExpression(Expression value, Sign sign) {
        this.value = value;
        this.sign = sign;
    }

    public Expression getValue() {
        return value;
    }

    public Sign getSign() {
        return sign;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
