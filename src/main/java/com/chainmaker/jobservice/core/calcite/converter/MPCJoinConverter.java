package com.chainmaker.jobservice.core.calcite.converter;

import com.chainmaker.jobservice.core.calcite.relnode.MPCJoin;
import com.chainmaker.jobservice.core.calcite.relnode.MPCNestedLoopJoin;
import lombok.SneakyThrows;
import org.apache.calcite.adapter.enumerable.*;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.volcano.RelSubset;
import org.apache.calcite.rel.InvalidRelException;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.core.JoinInfo;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.logical.LogicalJoin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Join的转换规则
 * 将LogicJoin（逻辑节点）转换成MPCJoin（带物理属性的节点）
 * 用于后续的查询优化
 */
public class MPCJoinConverter extends ConverterRule {

    public static final MPCJoinConverter INSTANCE = new MPCJoinConverter(
            LogicalJoin.class,
            Convention.NONE,
            EnumerableConvention.INSTANCE,
            "MPCJoin"
    );

    public MPCJoinConverter(Class<LogicalJoin> logicalJoinClass, Convention none, EnumerableConvention instance, String mpcJoin) {
        super(logicalJoinClass, none, instance, mpcJoin);
    }

    @Override
    public boolean matches(RelOptRuleCall call) {
        return super.matches(call);
    }

    @SneakyThrows
    @Override
    public RelNode convert(RelNode relNode) {
        LogicalJoin join = (LogicalJoin)relNode;
        List<RelNode> newInputs = new ArrayList();

        RelNode left;
        for(Iterator var4 = join.getInputs().iterator(); var4.hasNext(); newInputs.add(left)) {
            left = (RelNode)var4.next();
            if (!(left.getConvention() instanceof EnumerableConvention)) {
                left = convert(left, left.getTraitSet().replace(EnumerableConvention.INSTANCE));
            }
        }

        RelOptCluster cluster = join.getCluster();
        left = (RelNode)newInputs.get(0);
        RelNode right = (RelNode)newInputs.get(1);
        JoinInfo info = JoinInfo.of(left, right, join.getCondition());

        if (!info.isEqui()) {
            // 非等值连接，好像不会用到
            try {
                return MPCNestedLoopJoin.create(left, right, join.getCondition(), join.getVariablesSet(), join.getJoinType());
            } catch (InvalidRelException e) {
                return null;
            }
        } else {
            try {
                System.out.println(((RelSubset) left).getOriginal().getRelTypeName());
                System.out.println(((RelSubset) left));
                System.out.println(((RelSubset) left).getOriginal().getTable().getQualifiedName().size());
//                System.out.println(((RelSubset) left).getOriginal().getTable().getQualifiedName().get(0));
                return MPCJoin.create(left, right, info.getEquiCondition(left, right, cluster.getRexBuilder()), join.getVariablesSet(), join.getJoinType());
            } catch (InvalidRelException e) {
                return null;
            }
        }
    }
}
