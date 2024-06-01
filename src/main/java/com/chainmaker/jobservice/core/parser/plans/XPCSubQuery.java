package com.chainmaker.jobservice.core.parser.plans;

import org.apache.calcite.rel.RelNode;

import java.util.ArrayList;
import java.util.List;

public class XPCSubQuery extends XPCPlan {
    private final String alias;
    private final XPCPlan child;
    private final boolean isAlias;

    public XPCSubQuery(String alias, XPCPlan child) {
        this.alias = alias;
        this.child = child;
        this.isAlias = true;
    }

    public XPCSubQuery(XPCPlan child) {
        this.alias = "default_name";
        this.child = child;
        this.isAlias = false;
    }

    public XPCPlan getChild() {
        return child;
    }

    public String getAlias() {
        return alias;
    }

    public Boolean getIsAlias() {
        return isAlias;
    }



    @Override
    public List<XPCPlan> getChildren() {
        List<XPCPlan> children = new ArrayList<>();
        children.add(child);
        return children;
    }
//
//    @Override
//    public String toString() {
//        return  child.toString();
//    }


    @Override
    public String toString() {
        return  "SubQuery" + "[ " + alias+ " ]";
    }

    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    public RelNode accept(LogicalPlanRelVisitor visitor) {
        return visitor.visit(this);
    }
}
