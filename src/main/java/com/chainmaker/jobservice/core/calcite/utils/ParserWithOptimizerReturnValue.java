package com.chainmaker.jobservice.core.calcite.utils;

import com.chainmaker.jobservice.core.parser.plans.XPCPlan;
import org.apache.calcite.rel.RelNode;

public class ParserWithOptimizerReturnValue {

    private RelNode phyPlan;            // 优化后的物理计划

    private XPCPlan OriginPlan;      // 无法参与优化的联邦查询&TEE相关计划

    public ParserWithOptimizerReturnValue(RelNode plan, XPCPlan OriPlan) {
        this.phyPlan = plan;
        this.OriginPlan = OriPlan;
    }

    public RelNode getPhyPlan() {
        return phyPlan;
    }

    public void setPhyPlan(RelNode phyPlan) {
        this.phyPlan = phyPlan;
    }

    public XPCPlan getOriginPlan() {
        return OriginPlan;
    }

    public void setOriginPlan(XPCPlan originPlan) {
        OriginPlan = originPlan;
    }
}
