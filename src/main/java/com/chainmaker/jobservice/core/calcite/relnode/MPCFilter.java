package com.chainmaker.jobservice.core.calcite.relnode;

import com.chainmaker.jobservice.core.calcite.cost.MPCCost;
import com.chainmaker.jobservice.core.calcite.cost.MPCRelMetadataQuery;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rel.metadata.RelMdCollation;
import org.apache.calcite.rel.metadata.RelMdDistribution;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexNode;

/**
 * MPCFilter类，仿照EnumerableFilter类设计，具有EnumerableConvention物理属性
 * 查询优化时会将LogicalFilter转换为MPCFilter
 */
public class MPCFilter extends Filter implements EnumerableRel {
    private RelOptCost cost;

    public MPCFilter(RelOptCluster cluster, RelTraitSet traitSet, RelNode child, RexNode condition) {
        super(cluster, traitSet, child, condition);
        cost = null;
    }

    @Override
    public Filter copy(RelTraitSet relTraitSet, RelNode relNode, RexNode rexNode) {
        return new MPCFilter(getCluster(), traitSet, input, condition);
    }

    public static MPCFilter create(final RelNode input, RexNode condition) {
        RelOptCluster cluster = input.getCluster();
        RelMetadataQuery mq = MPCRelMetadataQuery.INSTANCE;
        RelTraitSet traitSet = cluster.traitSetOf(EnumerableConvention.INSTANCE).replaceIfs(RelCollationTraitDef.INSTANCE, () -> {
            return RelMdCollation.filter(mq, input);
        }).replaceIf(RelDistributionTraitDef.INSTANCE, () -> {
            return RelMdDistribution.filter(mq, input);
        });
        return new MPCFilter(cluster, traitSet, input, condition);
    }


    @Override
    public double estimateRowCount(RelMetadataQuery mq) {
        return mq.getRowCount(this);
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {
        double dRows = mq.getRowCount(this);
        double dCpu = mq.getRowCount(this.getInput());
        double dIo = dCpu;

//        System.out.println("dRows = " + dRows + " dCpu = " + dCpu + " dIo = " + dIo);
        cost = planner.getCostFactory().makeCost(dRows, dCpu, dIo);
        return cost;
    }

    @Override
    public Result implement(EnumerableRelImplementor enumerableRelImplementor, Prefer prefer) {
        throw new UnsupportedOperationException();
    }
}
