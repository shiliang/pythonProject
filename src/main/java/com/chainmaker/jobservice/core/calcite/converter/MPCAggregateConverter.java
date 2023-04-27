package com.chainmaker.jobservice.core.calcite.converter;

import com.chainmaker.jobservice.core.calcite.relnode.MPCAggregate;
import com.google.common.collect.ImmutableList;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.core.Aggregate;
import org.apache.calcite.rel.logical.LogicalAggregate;
import org.apache.calcite.rel.logical.LogicalProject;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MPCAggregateConverter extends ConverterRule {

    public static final MPCAggregateConverter INSTANCE = new MPCAggregateConverter(
            LogicalAggregate.class,
            Convention.NONE,
            EnumerableConvention.INSTANCE,
            "MPCAggregate"
    );

    public MPCAggregateConverter(Class<LogicalAggregate> logicalAggregateClass, Convention none, EnumerableConvention instance, String description) {
        super(logicalAggregateClass, none, instance, description);
    }

    @Override
    public @Nullable RelNode convert(RelNode rel) {
        final Aggregate agg = (Aggregate) rel;
        final RelTraitSet traitSet = rel.getCluster()
                .traitSet().replace(EnumerableConvention.INSTANCE);
        return new MPCAggregate(
                rel.getCluster(),
                traitSet,
                ImmutableList.of(),
                convert(agg.getInput(), traitSet),
                agg.getGroupSet(),
                agg.getGroupSets(),
                agg.getAggCallList());
    }
}
