package com.chainmaker.jobservice.core.calcite.utils;

import com.chainmaker.jobservice.core.optimizer.plans.PhysicalPlan;
import com.chainmaker.jobservice.core.parser.plans.LogicalPlan;
import org.apache.calcite.rel.RelNode;

public class parserWithOptimizerReturnValue {

    private RelNode phyPlan;            // 优化后的物理计划

    private LogicalPlan OriginPlan;      // 无法参与优化的联邦查询&TEE相关计划

    public parserWithOptimizerReturnValue(RelNode plan, LogicalPlan OriPlan) {
        this.phyPlan = plan;
        this.OriginPlan = OriPlan;
    }

    public RelNode getPhyPlan() {
        return phyPlan;
    }

    public void setPhyPlan(RelNode phyPlan) {
        this.phyPlan = phyPlan;
    }

    public LogicalPlan getOriginPlan() {
        return OriginPlan;
    }

    public void setOriginPlan(LogicalPlan originPlan) {
        OriginPlan = originPlan;
    }
}
