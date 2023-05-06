package com.chainmaker.jobservice.core.calcite.relnode;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.adapter.enumerable.EnumerableAggregate;
import org.apache.calcite.adapter.enumerable.EnumerableAggregateBase;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.InvalidRelException;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Aggregate;
import org.apache.calcite.rel.core.AggregateCall;
import org.apache.calcite.rel.hint.RelHint;
import org.apache.calcite.util.ImmutableBitSet;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class MPCAggregate extends EnumerableAggregateBase implements EnumerableRel {

    public MPCAggregate(RelOptCluster cluster, RelTraitSet traitSet, List<RelHint> hints, RelNode input, ImmutableBitSet groupSet, @Nullable List<ImmutableBitSet> groupSets, List<AggregateCall> aggCalls) {
        super(cluster, traitSet, hints, input, groupSet, groupSets, aggCalls);
    }

    @Override
    public Result implement(EnumerableRelImplementor implementor, Prefer pref) {
        return null;
    }

    @Override
    public Aggregate copy(RelTraitSet traitSet, RelNode input, ImmutableBitSet groupSet, @Nullable List<ImmutableBitSet> groupSets, List<AggregateCall> aggCalls) {
        return new MPCAggregate(getCluster(), traitSet, ImmutableList.of(), input,
                groupSet, groupSets, aggCalls);
    }
}
