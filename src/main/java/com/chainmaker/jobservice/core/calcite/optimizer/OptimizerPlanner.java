package com.chainmaker.jobservice.core.calcite.optimizer;

import com.chainmaker.jobservice.core.calcite.converter.*;
import com.chainmaker.jobservice.core.optimizer.RuleBasedOptimizer;
import org.apache.calcite.adapter.enumerable.*;
import org.apache.calcite.plan.*;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.rules.*;
import org.apache.calcite.tools.RuleSet;
import org.apache.calcite.tools.RuleSets;

import java.rmi.registry.RegistryHandler;

/**
 * 查询优化类，封装了一些繁琐的调用，使用起来更加简单
 */
public class OptimizerPlanner {

    private RelNode relNode;                // 需要优化的逻辑计划

    private VolcanoPlanner planner;         // 查询优化器

    private RelTraitSet desiredTraitSet;

    /**
     * 构造函数，完成一系列初始化操作
     * @param root 需要优化的逻辑计划
     * @param isClear 是否需要清空默认自带的优化规则
     */
    public OptimizerPlanner(RelNode root, boolean isClear) {
        relNode = root;
        planner = (VolcanoPlanner) root.getCluster().getPlanner();
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        desiredTraitSet = root.getTraitSet().replace(EnumerableConvention.INSTANCE);
        initRules(isClear);

    }

    /**
     * 优化函数，完成具体的查询优化
     * @return 返回优化后的物理计划
     */
    public RelNode optimize() {
        planner.setRoot(planner.changeTraits(relNode, desiredTraitSet));
        RelNode phyPlan = planner.findBestExp();

        return phyPlan;
    }

    /**
     * 初始化优化规则
     * 规则分为两种，一种是转换规则，另一种则是算子优化规则
     * @param isClear
     */
    public void initRules(boolean isClear) {
        if (isClear) {
            planner.clear();
            planner.addRule(MPCTableScanConverter.INSTANCE);
            planner.addRule(MPCFilterConverter.INSTANCE);
            planner.addRule(MPCProjectConverter.INSTANCE);
            planner.addRule(MPCJoinConverter.INSTANCE);
            planner.addRule(MPCUnionConverter.INSTANCE);
            planner.addRule(MPCSortConverter.INSTANCE);

//            planner.removeRule(EnumerableRules.ENUMERABLE_PROJECT_RULE);
//            planner.removeRule(EnumerableRules.ENUMERABLE_FILTER_RULE);
//            planner.removeRule(EnumerableRules.ENUMERABLE_JOIN_RULE);
//            planner.removeRule(EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE);
//            planner.removeRule(EnumerableInterpreterRule.INSTANCE);
//            planner.removeRule(Bindables.BINDABLE_TABLE_SCAN_RULE);

//            planner.addRule(EnumerableRules.ENUMERABLE_PROJECT_RULE);
//            planner.addRule(EnumerableRules.ENUMERABLE_FILTER_RULE);
//            planner.addRule(EnumerableRules.ENUMERABLE_JOIN_RULE);
//            planner.addRule(EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE);
//            planner.addRule(EnumerableRules.ENUMERABLE_VALUES_RULE);
//            planner.addRule(EnumerableRules.ENUMERABLE_SORT_RULE);
//            planner.addRule(EnumerableRules.ENUMERABLE_MERGE_JOIN_RULE);
//            planner.addRule(EnumerableRules.ENUMERABLE_AGGREGATE_RULE);
//            planner.addRule(EnumerableRules.ENUMERABLE_LIMIT_RULE);


            planner.addRule(CoreRules.JOIN_ASSOCIATE);
            planner.addRule(CoreRules.FILTER_INTO_JOIN);
            planner.addRule(CoreRules.FILTER_REDUCE_EXPRESSIONS);
            planner.addRule(CoreRules.CALC_REDUCE_EXPRESSIONS);
//            planner.addRule(CoreRules.PROJECT_REDUCE_EXPRESSIONS);
            planner.addRule(CoreRules.JOIN_CONDITION_PUSH);
            planner.addRule(CoreRules.JOIN_PUSH_TRANSITIVE_PREDICATES);
            planner.addRule(CoreRules.JOIN_COMMUTE_OUTER);
            planner.addRule(CoreRules.JOIN_COMMUTE);
            planner.addRule(CoreRules.PROJECT_MERGE);
            planner.addRule(CoreRules.PROJECT_SET_OP_TRANSPOSE);
            planner.addRule(CoreRules.UNION_MERGE);

//            planner.addRule(JoinAssociateRule.INSTANCE);
//            planner.addRule(FilterJoinRule.FILTER_ON_JOIN);
//            planner.addRule(ReduceExpressionsRule.PROJECT_INSTANCE);
//            planner.addRule(ReduceExpressionsRule.CALC_INSTANCE);
//            planner.addRule(JoinPushThroughJoinRule.LEFT);
//            planner.addRule(JoinPushThroughJoinRule.RIGHT);
//            planner.addRule(JoinCommuteRule.INSTANCE);
//            planner.addRule(JoinCommuteRule.SWAP_OUTER);
//            planner.addRule(ProjectMergeRule.INSTANCE);
//            planner.addRule(AggregateJoinRemoveRule.INSTANCE);
//            planner.addRule(ProjectSortTransposeRule.INSTANCE);

        }
    }
}
