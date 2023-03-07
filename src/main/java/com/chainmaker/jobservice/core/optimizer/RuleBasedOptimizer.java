package com.chainmaker.jobservice.core.optimizer;

import com.chainmaker.jobservice.core.parser.plans.LogicalPlan;
import com.chainmaker.jobservice.core.parser.plans.LogicalPlanVisitor;
import com.chainmaker.jobservice.core.parser.plans.LogicalProject;

/**
 * @author gaokang
 * @date 2022-08-30 16:47
 * @description:对逻辑计划树做简单规则优化，生成优化后的逻辑计划树
 * @version: 1.0.0
 */

public class RuleBasedOptimizer extends LogicalPlanVisitor {
    public void visit(LogicalPlan plan) {
        plan.accept(this);
    }

    public void visit(LogicalProject node) {
        for (LogicalPlan child: node.getChildren()) {
            child.accept(this);
        }
    }


}
