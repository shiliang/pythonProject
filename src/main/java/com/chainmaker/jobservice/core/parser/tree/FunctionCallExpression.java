package com.chainmaker.jobservice.core.parser.tree;

import cn.hutool.core.util.StrUtil;

import java.util.List;
import java.util.stream.Collectors;

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
        List<String> exprs = expressions.stream().map(x -> x.toString()).collect(Collectors.toList());
        return function + StrUtil.format( "({})",String.join(",", exprs));
//        return function + expressions.toString();
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
