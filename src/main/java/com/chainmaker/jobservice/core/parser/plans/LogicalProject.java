package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.FaderatedQueryExpression;
import com.chainmaker.jobservice.core.parser.tree.NamedExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * LogicPlan中放SELECT内容的表达式数组，一般最后执行，作为root节点
 * 如果添加SUM、MAX等UDAF，可作为Projrct的父节点，执行顺序最后
 * 目前SELECT只支持表达式，子节点为Filter
 */
public class LogicalProject extends LogicalPlan {
    private final FaderatedQueryExpression projectList;
    private final List<LogicalPlan> children;

    public LogicalProject(FaderatedQueryExpression projectList, List<LogicalPlan> children) {
        this.projectList = projectList;
        this.children = children;
    }

    public LogicalProject(List<LogicalPlan> children) {
        this.children = children;
        this.projectList = null;
    }

    public FaderatedQueryExpression getProjectList() {
        return projectList;
    }

    @Override
    public List<LogicalPlan> getChildren() {
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
}
