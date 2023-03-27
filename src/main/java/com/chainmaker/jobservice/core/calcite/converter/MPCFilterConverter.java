package com.chainmaker.jobservice.core.calcite.converter;

import com.chainmaker.jobservice.core.calcite.relnode.MPCFilter;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.plan.*;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.logical.LogicalFilter;

/**
 * Filter的转换规则
 * 将LogicFilter（逻辑节点）转换成MPCFilter（带物理属性的节点）
 * 用于后续的查询优化
 */
public class MPCFilterConverter extends ConverterRule {

    public static final MPCFilterConverter INSTANCE = new MPCFilterConverter(
            LogicalFilter.class,
            Convention.NONE,
            EnumerableConvention.INSTANCE,
            "MPCFilter"
    );

    public MPCFilterConverter(Class<? extends RelNode> clazz, RelTrait in, RelTrait out, String description) {
        super(clazz, in, out, description);
    }

    @Override
    public boolean matches(RelOptRuleCall call) {
        return super.matches(call);
    }

    @Override
    public RelNode convert(RelNode relNode) {
        final LogicalFilter filter = (LogicalFilter) relNode;

        return new MPCFilter(filter.getCluster(),
                filter.getTraitSet().replace(EnumerableConvention.INSTANCE),
                convert(filter.getInput(),
                        filter.getInput().getTraitSet()
                                .replace(EnumerableConvention.INSTANCE)),
                filter.getCondition());
    }
}
