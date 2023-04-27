package com.chainmaker.jobservice.core.calcite.relnode;

import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.adapter.enumerable.EnumerableUnion;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.SetOp;
import org.apache.calcite.rel.core.Union;

import java.util.List;

/**
 * MPCFilter类，仿照EnumerableUnion类设计，具有EnumerableConvention物理属性
 * 查询优化时会将LogicalUnion转换为MPCUnion
 */
public class MPCUnion extends Union implements EnumerableRel {

    public MPCUnion(RelOptCluster cluster, RelTraitSet traitSet, List<RelNode> inputs, boolean all) {
        super(cluster, traitSet, inputs, all);
    }

    @Override
    public Result implement(EnumerableRelImplementor enumerableRelImplementor, Prefer prefer) {
        return null;
    }

    @Override
    public SetOp copy(RelTraitSet relTraitSet, List<RelNode> list, boolean b) {
        return new MPCUnion(getCluster(), traitSet, inputs, all);
    }
}
