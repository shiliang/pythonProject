package com.chainmaker.jobservice.core.calcite.converter;

import com.chainmaker.jobservice.core.calcite.relnode.MPCUnion;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableUnion;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelTrait;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.core.Union;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.logical.LogicalUnion;
import org.apache.calcite.util.Util;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class MPCUnionConverter extends ConverterRule {

    public static final MPCUnionConverter INSTANCE = new MPCUnionConverter(
            LogicalUnion.class,
            Convention.NONE,
            EnumerableConvention.INSTANCE,
            "MPCUnion"
    );

    public MPCUnionConverter(Class<? extends RelNode> clazz, RelTrait in, RelTrait out, String description) {
        super(clazz, in, out, description);
    }

    @Override
    public @Nullable RelNode convert(RelNode rel) {
        final Union union = (Union) rel;
        final EnumerableConvention out = EnumerableConvention.INSTANCE;
        final RelTraitSet traitSet = rel.getCluster().traitSet().replace(out);
        final List<RelNode> newInputs =
                Util.transform(union.getInputs(), n -> convert(n, traitSet));
        return new MPCUnion(rel.getCluster(), traitSet,
                newInputs, union.all);
    }
}
