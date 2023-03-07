package com.chainmaker.jobservice.core.parser.plans;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-15 10:05
 * @description:在逻辑计划中表示表名，作为不需要处理的算子
 * @version: 1.0.0
 */

public class LogicalTable extends LogicalPlan {
    private final String tableName;
    private final String alias;

    public LogicalTable(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
    }

    public LogicalTable(String tableName) {
        this.tableName = tableName;
        this.alias = tableName;
    }

    public String getAlias() {
        return alias;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public List<LogicalPlan> getChildren() {
        return null;
    }

    @Override
    public String toString() {
        return "TableScan " + "[ " + tableName + " ]";
    }

    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }
}
