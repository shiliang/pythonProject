package com.chainmaker.jobservice.core.calcite.relnode;

import com.chainmaker.jobservice.core.calcite.cost.MPCCost;
import com.chainmaker.jobservice.core.calcite.cost.MPCRelMetadataQuery;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.MPCMetadata;
import lombok.SneakyThrows;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.InvalidRelException;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.*;
import org.apache.calcite.rel.metadata.RelMdCollation;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.util.Util;

import java.util.Set;

/**
 * MPCJoin，仿照EnumerableJoin类设计，具有EnumerableConvention物理属性
 * 查询优化时会将LogicalJoin转换为MPCJoin
 */
public class MPCJoin extends Join implements EnumerableRel {

    private RelOptCost cost;

    public MPCJoin(RelOptCluster cluster, RelTraitSet traits, RelNode left, RelNode right, RexNode condition, Set<CorrelationId> variablesSet, JoinRelType joinType) throws InvalidRelException {
        super(cluster, traits, left, right, condition, variablesSet, joinType);
    }

    public static MPCJoin create(RelNode left, RelNode right, RexNode condition, Set<CorrelationId> variablesSet, JoinRelType joinType) throws InvalidRelException {
        RelOptCluster cluster = left.getCluster();
//        RelMetadataQuery mq = cluster.getMetadataQuery();
        RelMetadataQuery mq = MPCRelMetadataQuery.INSTANCE;
        RelTraitSet traitSet = cluster.traitSetOf(EnumerableConvention.INSTANCE).replaceIfs(RelCollationTraitDef.INSTANCE, () -> RelMdCollation.enumerableHashJoin(mq, left, right, joinType));
        return new MPCJoin(cluster, traitSet, left, right, condition, variablesSet, joinType);
    }


    @SneakyThrows
    @Override
    public MPCJoin copy(RelTraitSet relTraitSet, RexNode rexNode, RelNode relNode, RelNode relNode1, JoinRelType joinRelType, boolean b) {
        return new MPCJoin(getCluster(), traitSet, left, right,
                condition, variablesSet, joinType);
    }


    @Override
    public double estimateRowCount(RelMetadataQuery mq) {
        return mq.getRowCount(this);
    }

    /**
     * 代价计算函数
     * 需要区分Join双方是否处于同一数据源
     * 是的话则采用正常HashJoin的代价，否则需要PSI，暂时将PSI的代价设为普通HashJoin的5倍
     * @param planner
     * @param mq
     * @return
     */
    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {

        MPCMetadata metadata = MPCMetadata.getInstance();
        boolean isSameSource = true;
        String leftOrg = null;
        for (String tableField : (this.getLeft().getRowType().getFieldNames())) {
            String tableName = tableField.split("\\.")[0];
            if (leftOrg != null && leftOrg != metadata.getTableOrgId(tableName)) {
                isSameSource = false;
                break;
            }
            if (leftOrg == null) {
                leftOrg = metadata.getTableOrgId(tableName);
            }
        }
        String rightOrg = null;
        for (String tableField : (this.getRight().getRowType().getFieldNames())) {
            if (!isSameSource) {
                break;
            }
            String tableName = tableField.split("\\.")[0];
            if (rightOrg != null && rightOrg != metadata.getTableOrgId(tableName)) {
                isSameSource = false;
                break;
            }
            if (rightOrg == null) {
                rightOrg = metadata.getTableOrgId(tableName);
            }
        }
        if (leftOrg != rightOrg) {
            isSameSource = false;
        }
//        System.out.println("isSameSource ? " + isSameSource);

        double rowCount = mq.getRowCount(this);

        double rightRowCount = this.right.estimateRowCount(mq);
        double leftRowCount = this.left.estimateRowCount(mq);
        if (Double.isInfinite(leftRowCount)) {
            rowCount = leftRowCount;
        } else {
            rowCount += Util.nLogN(leftRowCount);
        }

        if (Double.isInfinite(rightRowCount)) {
            rowCount = rightRowCount;
        } else {
            rowCount += rightRowCount;
        }

        if (isSameSource) {
            // 位于同一数据源下
            cost = planner.getCostFactory().makeCost(rowCount, rowCount, rowCount);
        } else {
            // 位于不同数据源，需要隐私求交
            cost = planner.getCostFactory().makeCost(rowCount*5, rowCount*5, rowCount*5);
        }

        return cost;
    }


    @Override
    public Result implement(EnumerableRelImplementor enumerableRelImplementor, Prefer prefer) {
        return null;
    }
}
