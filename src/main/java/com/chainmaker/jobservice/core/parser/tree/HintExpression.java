package com.chainmaker.jobservice.core.parser.tree;

import java.util.List;

/**
 * @author gaokang
 * @date 2023-04-01 17:56
 * @description:
 * @version:
 */
public class HintExpression extends Expression {
    private final String key;
    private final List<String> values;

    public HintExpression(String key, List<String> values) {
        this.key = key;
        this.values = values;
    }

    public String getKey() {
        return key;
    }

    public List<String> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return key+values;
    }
    @Override
    public List<Expression> getChildren() {
        return null;
    }


    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
