package com.chainmaker.jobservice.core.parser.printer;

import com.chainmaker.jobservice.core.parser.plans.XPCPlan;

/**
 * @author gaokang
 * @date 2022-08-19 14:32
 * @description:打印逻辑计划树
 * @version: 1.0.0
 */

public class LogicalPlanPrinter {
    public StringBuilder logicalPlanString = new StringBuilder();

    public void visitTree(XPCPlan plan, Integer level) {
        for (int i=0; i<level; i++) {
            logicalPlanString.append("\t");
        }
        logicalPlanString.append("+-");
        logicalPlanString.append(plan.toString() + "\n");
        level = level + 1;
        if (plan.getChildren() != null) {
            for (int i = 0; i < plan.getChildren().size(); i++) {
                visitTree((XPCPlan) plan.getChildren().get(i), level);
            }
        }
    }
}
