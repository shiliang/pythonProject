package com.chainmaker.jobservice.core.parser.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-30 20:06
 * @description:用来表示解析后逻辑计划的表达式，一般是具体的{表名.字段名}
 * @version: 1.0.0
 */

public class RelExpression extends Expression {
    private final Integer id;

    public RelExpression(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "#" + id;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public List<? extends Node> getChildren() {
        return null;
    }
}
