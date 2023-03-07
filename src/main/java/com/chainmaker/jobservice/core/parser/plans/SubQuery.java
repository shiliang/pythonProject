package com.chainmaker.jobservice.core.parser.plans;

import java.util.ArrayList;
import java.util.List;

public class SubQuery extends LogicalPlan {
    private final String alias;
    private final LogicalPlan child;
    private final boolean isAlias;

    public SubQuery(String alias, LogicalPlan child) {
        this.alias = alias;
        this.child = child;
        this.isAlias = true;
    }

    public SubQuery(LogicalPlan child) {
        this.alias = "default_name";
        this.child = child;
        this.isAlias = false;
    }

    public LogicalPlan getChild() {
        return child;
    }

    public String getAlias() {
        return alias;
    }

    public Boolean getIsAlias() {
        return isAlias;
    }



    @Override
    public List<LogicalPlan> getChildren() {
        List<LogicalPlan> children = new ArrayList<>();
        children.add(child);
        return children;
    }

    @Override
    public String toString() {
        return  child.toString();
    }

    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }
}
