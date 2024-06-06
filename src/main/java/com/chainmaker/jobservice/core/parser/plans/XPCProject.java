package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.SelectQueryExpression;
import org.apache.calcite.rel.RelNode;

import java.util.List;

/**
 * LogicPlan中放SELECT内容的表达式数组，一般最后执行，作为root节点
 * 如果添加SUM、MAX等UDAF，可作为Projrct的父节点，执行顺序最后
 * 目前SELECT只支持表达式，子节点为Filter
 */
public class XPCProject extends XPCPlan {
    private final SelectQueryExpression projectList;
    private final List<XPCPlan> children;

    public XPCProject(SelectQueryExpression projectList, List<XPCPlan> children) {
        this.projectList = projectList;
        this.children = children;
    }

    public XPCProject(List<XPCPlan> children) {
        this.children = children;
        this.projectList = null;
    }

    public SelectQueryExpression getProjectList() {
        return projectList;
    }

    @Override
    public List<XPCPlan> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "Project " + projectList.toString();
    }

    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    public RelNode accept(LogicalPlanRelVisitor visitor) {
        return visitor.visit(this);
    }
}
