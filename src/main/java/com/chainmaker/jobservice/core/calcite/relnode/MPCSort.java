package com.chainmaker.jobservice.core.calcite.relnode;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.adapter.enumerable.EnumerableSort;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rex.RexNode;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MPCSort extends Sort implements EnumerableRel {

    public MPCSort(RelOptCluster cluster, RelTraitSet traitSet,
                          RelNode input, RelCollation collation, @Nullable RexNode offset, @Nullable RexNode fetch) {
        super(cluster, traitSet, input, collation, offset, fetch);
    }

    public static MPCSort create(RelNode child, RelCollation collation,
                                 @Nullable RexNode offset, @Nullable RexNode fetch) {
        final RelOptCluster cluster = child.getCluster();
        final RelTraitSet traitSet = cluster.traitSetOf(EnumerableConvention.INSTANCE)
                .replace(collation);
        return  new MPCSort(cluster, traitSet, child, collation, offset, fetch);
    }

    @Override
    public Result implement(EnumerableRelImplementor enumerableRelImplementor, Prefer prefer) {
        return null;
    }

    @Override
    public Sort copy(RelTraitSet relTraitSet, RelNode relNode, RelCollation relCollation, @Nullable RexNode rexNode, @Nullable RexNode rexNode1) {
        return null;
    }
}
