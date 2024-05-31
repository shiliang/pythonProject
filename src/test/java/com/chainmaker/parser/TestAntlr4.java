package com.chainmaker.parser;

import com.chainmaker.jobservice.core.parser.LogicalPlanBuilderV2;
import com.chainmaker.jobservice.core.parser.plans.LogicalPlan;
import com.chainmaker.jobservice.core.parser.printer.LogicalPlanPrinter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TestAntlr4 {
    public static void main(String[] args) {
//        String sql = " select whh_enterprise_3.balance, tmp_table.socialid " +
//                "from whh_enterprise_3, whh_security_1,(select tmp_inner.* from (select sum(socialid),count(socialid),min(socialid),max(socialid) from whh_security_1 ) tmp_inner ) tmp_table " +
//                "where whh_enterprise_3.socialid= whh_security_1.socialid " +
//                "and tmp_table.socialid= whh_security_1.socialid and tmp_table.socialid = 888";

        String sql = "select whh_enterprise_3.balance, tmp_table.socialid from whh_enterprise_3, whh_security_1,(select tmp_inner.* from (select * from whh_security_1 ) tmp_inner ) tmp_table where whh_enterprise_3.socialid= whh_security_1.socialid and tmp_table.socialid= whh_security_1.socialid";
        LogicalPlanBuilderV2 logicalPlanBuilder = new LogicalPlanBuilderV2(sql);
        LogicalPlan logicalPlan = logicalPlanBuilder.getLogicalPlan();

        LogicalPlanPrinter printer = new LogicalPlanPrinter();
        printer.visitTree(logicalPlan, 0);
        log.info(String.valueOf(printer.logicalPlanString));
    }
}
