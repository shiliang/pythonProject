package com.chainmaker.jobservice.core.calcite.utils;

import com.chainmaker.jobservice.core.optimizer.plans.PhysicalPlan;
import com.chainmaker.jobservice.core.parser.plans.LogicalPlan;
import org.apache.calcite.rel.RelNode;

public class parserWithOptimizerReturnValue {

    private RelNode phyPlan;            // 优化后的物理计划

    private LogicalPlan FLLogicPlan;      // 无法参与优化的联邦查询相关物理计划

    public parserWithOptimizerReturnValue(RelNode plan, LogicalPlan FLplan) {
        this.phyPlan = plan;
        this.FLLogicPlan = FLplan;
    }

    public RelNode getPhyPlan() {
        return phyPlan;
    }

    public void setPhyPlan(RelNode phyPlan) {
        this.phyPlan = phyPlan;
    }

    public LogicalPlan getFLLogicPlan() {
        return FLLogicPlan;
    }

    public void setFLPhyPlan(LogicalPlan LogicPlan) {
        this.FLLogicPlan = LogicPlan;
    }
}
