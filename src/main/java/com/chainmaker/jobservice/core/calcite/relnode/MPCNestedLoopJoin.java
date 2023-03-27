package com.chainmaker.jobservice.core.calcite.relnode;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.adapter.enumerable.*;
import org.apache.calcite.linq4j.function.Predicate2;
import org.apache.calcite.linq4j.tree.*;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.InvalidRelException;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.CorrelationId;
import org.apache.calcite.rel.core.Join;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.metadata.RelMdCollation;
import org.apache.calcite.rel.metadata.RelMdUtil;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexProgramBuilder;
import org.apache.calcite.util.BuiltInMethod;
import org.apache.calcite.util.Pair;

import java.util.Set;

/**
 * 非等值连接时才会用到，但应该没有这种情况
 */
@Deprecated
public class MPCNestedLoopJoin extends Join implements EnumerableRel {

    public MPCNestedLoopJoin(RelOptCluster cluster, RelTraitSet traits, RelNode left, RelNode right, RexNode condition, Set<CorrelationId> variablesSet, JoinRelType joinType) throws InvalidRelException {
        super(cluster, traits, left, right, condition, variablesSet, joinType);
    }

    public static MPCNestedLoopJoin create(RelNode left, RelNode right, RexNode condition, Set<CorrelationId> variablesSet, JoinRelType joinType) throws InvalidRelException {
        RelOptCluster cluster = left.getCluster();
        RelMetadataQuery mq = cluster.getMetadataQuery();
        RelTraitSet traitSet = cluster.traitSetOf(EnumerableConvention.INSTANCE).replaceIfs(RelCollationTraitDef.INSTANCE, () -> RelMdCollation.enumerableNestedLoopJoin(mq, left, right, joinType));
        return new MPCNestedLoopJoin(cluster, traitSet, left, right, condition, variablesSet, joinType);
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {
//        return super.computeSelfCost(planner, mq);
        double rowCount = mq.getRowCount(this);
        switch(this.joinType) {
            case RIGHT:
                rowCount = RelMdUtil.addEpsilon(rowCount);
                break;
            default:
                if (this.left.getId() > this.right.getId()) {
                    rowCount = RelMdUtil.addEpsilon(rowCount);
                }
        }

        double rightRowCount = this.right.estimateRowCount(mq);
        double leftRowCount = this.left.estimateRowCount(mq);
        if (Double.isInfinite(leftRowCount)) {
            rowCount = leftRowCount;
        }

        if (Double.isInfinite(rightRowCount)) {
            rowCount = rightRowCount;
        }

        return planner.getCostFactory().makeCost(rowCount, 0.0D, 0.0D);
    }

    @Override
    public Result implement(EnumerableRelImplementor implementor, Prefer pref) {
        return null;
    }

    @Override
    public Join copy(RelTraitSet relTraitSet, RexNode rexNode, RelNode relNode, RelNode relNode1, JoinRelType joinRelType, boolean b) {
        try {
            return new MPCNestedLoopJoin(this.getCluster(), traitSet, left, right, condition, this.variablesSet, joinType);
        } catch (InvalidRelException var8) {
            throw new AssertionError(var8);
        }
    }
}
