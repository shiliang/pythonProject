package com.chainmaker.jobservice.core.analyzer.plans;

import com.chainmaker.jobservice.core.parser.plans.LogicalPlan;

/**
 * @author gaokang
 * @date 2022-08-30 19:54
 * @description:访问解析后的逻辑计划
 * @version: 1.0.0
 */

public abstract class RelPlanVisitor {
    public void visit(RelPlan node) {
    }
}
