package com.chainmaker.jobservice.core.calcite.converter;

import com.chainmaker.jobservice.core.calcite.optimizer.metadata.MPCMetadata;
import com.chainmaker.jobservice.core.calcite.relnode.MPCTableScan;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableTableScan;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.plan.*;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.schema.Table;

/**
 * TableScan
 * 将LogicTableScan（逻辑节点）转换成MPCTableScan（带物理属性的节点）
 * 用于后续的查询优化
 */
public class MPCTableScanConverter extends ConverterRule {
    public static final MPCTableScanConverter INSTANCE = new MPCTableScanConverter(
            LogicalTableScan.class,
            Convention.NONE,
            EnumerableConvention.INSTANCE,
            "MPCTableScan"
    );

    public MPCTableScanConverter(Class<? extends RelNode> clazz, RelTrait in, RelTrait out, String description) {
        super(clazz, in, out, description);
    }

    @Override
    public boolean matches(RelOptRuleCall call) {
        return super.matches(call);
    }


    @Override
    public RelNode convert(RelNode rel) {
        LogicalTableScan scan = (LogicalTableScan)rel;
        RelOptTable relOptTable = scan.getTable();
        Table table = (Table)relOptTable.unwrap(Table.class);
        String tableName = relOptTable.getQualifiedName().get(0);
        double rowCount = MPCMetadata.getInstance().getTableRowCount(tableName);
        String orgId = MPCMetadata.getInstance().getTableOrgId(tableName);
        if (!EnumerableTableScan.canHandle(table)) {
            return null;
        } else {
            Expression expression = relOptTable.getExpression(Object.class);
            return expression == null ? null : MPCTableScan.create(scan.getCluster(), relOptTable, rowCount);
        }
    }
}
