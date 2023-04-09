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
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.metadata.RelMdCollation;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexNode;

import java.util.List;

/**
 * MPCProject类，仿照EnumerableProject类设计，具有EnumerableConvention物理属性
 * 查询优化时会将LogicalProject转换为MPCProject
 */
public class MPCProject extends Project implements EnumerableRel {

    private RelOptCost cost;

    public MPCProject(
            RelOptCluster cluster,
            RelTraitSet traitSet,
            RelNode input,
            List<? extends RexNode> projects,
            RelDataType rowType) {
        super(cluster, traitSet, input, projects, rowType);
    }

    @Override
    public double estimateRowCount(RelMetadataQuery mq) {
        return mq.getRowCount(this);
    }

    @Override
    public Project copy(RelTraitSet relTraitSet, RelNode relNode, List<RexNode> list, RelDataType relDataType) {
        return new MPCProject(getCluster(), traitSet, input, list, rowType);
    }

    public static MPCProject create(RelNode input, List<? extends RexNode> projects, RelDataType rowType) {
        final RelOptCluster cluster = input.getCluster();
//        cluster.setMetadataProvider(MPCRelMetaDataProvider.relMetaDataProvider);
        RelMetadataQuery mq = MPCRelMetadataQuery.INSTANCE;
            final RelTraitSet traitSet =
                    cluster.traitSet().replace(EnumerableConvention.INSTANCE)
                            .replaceIfs(RelCollationTraitDef.INSTANCE,
                                    () -> RelMdCollation.project(mq, input, projects));
            return new MPCProject(cluster, traitSet, input, projects, rowType);
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {
        double dRows = mq.getRowCount(this.getInput());
        double dCpu = dRows * (double)this.exps.size();
        double dIo = dRows * (double)this.exps.size();
        cost = planner.getCostFactory().makeCost(dRows, dCpu, dIo);
//        cost = new MPCCost(dRows, dCpu, dIo);
        return cost;
    }

    @Override
    public Result implement(EnumerableRelImplementor enumerableRelImplementor, Prefer prefer) {
        throw new UnsupportedOperationException();
    }
}
