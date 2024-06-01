package com.chainmaker.jobservice.core.analyzer.plans;

import com.chainmaker.jobservice.core.parser.plans.XPCPlan;

/**
 * @author gaokang
 * @date 2022-08-30 19:50
 * @description:解析后的逻辑计划，绑定元数据
 * @version: 1.0.0
 */

public abstract class RelPlan extends XPCPlan {
    public void accept(RelPlanVisitor visitor) {
        visitor.visit(this);
    }
}
