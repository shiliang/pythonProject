package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.Expression;
import org.apache.calcite.rel.RelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 逻辑树中FROM部分超过一个表时用Join，两个子节点
 * from a, b:                               等价于a inner join b，子节点为UnreservedRelation
 * from a join b:                           与上等价，左右子节点都为叶子节点UnreservedRelation，表名
 * from (select...from...where...) join b:  左子节点为Project，右子节点为UnreservedRelation
 * from (a join b on...) join c:            左节点为Join节点，右节点为UnreservedRelation
 */
public class LogicalJoin extends LogicalPlan {
    public enum Type {
        INNER, LEFT, RIGHT, FULL
    }
    private final Expression condition;
    private final Type joinType;
    private final LogicalPlan left;
    private final LogicalPlan right;
    private String alias;

    public LogicalJoin(Expression condition, Type joinType, LogicalPlan left, LogicalPlan right, String alias) {
        this.condition = condition;
        this.joinType = joinType;
        this.left = left;
        this.right = right;
        this.alias = alias;
    }

    public LogicalJoin(Expression condition, Type joinType, LogicalPlan left, LogicalPlan right) {
        this.condition = condition;
        this.joinType = joinType;
        this.left = left;
        this.right = right;
        this.alias = "";
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }


    public Expression getCondition() {
        return condition;
    }

    public Type getJoinType() {
        return joinType;
    }

    public LogicalPlan getLeft() {
        return left;
    }

    public LogicalPlan getRight() {
        return right;
    }


    @Override
    public List<LogicalPlan> getChildren() {
        List<LogicalPlan> children = new ArrayList<>();
        children.add(left);
        children.add(right);
        return children;
    }

    @Override
    public String toString() {
        return "Join " + "[ " + condition + "," + joinType + " " + alias + " ]";
    }

    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    public RelNode accept(LogicalPlanRelVisitor visitor) {
        return visitor.visit(this);
    }

}
