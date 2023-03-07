package com.chainmaker.jobservice.core.optimizer.plans;

/**
 * @author gaokang
 * @date 2022-08-22 15:22
 * @description:遍历执行计划节点
 * @version: 1.0.0
 */

public abstract class PhysicalPlanVisitor {
    public void visit(PhysicalPlan node){}
    public void visit(TableScan node){}
    public void visit(Project node){}
    public void visit(SpdzMpc node){}
    public void visit(TeeMpc node){}
    public void visit(Fate node){}

}
