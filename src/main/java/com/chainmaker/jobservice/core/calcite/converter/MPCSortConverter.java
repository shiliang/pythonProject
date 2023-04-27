package com.chainmaker.jobservice.core.calcite.converter;

import com.chainmaker.jobservice.core.calcite.relnode.MPCSort;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableLimitSortRule;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelTrait;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.calcite.rel.logical.LogicalUnion;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MPCSortConverter extends ConverterRule {

    public static final MPCSortConverter INSTANCE = new MPCSortConverter(
            LogicalSort.class,
            Convention.NONE,
            EnumerableConvention.INSTANCE,
            "MPCSort"
    );

    public MPCSortConverter(Class<? extends RelNode> clazz, RelTrait in, RelTrait out, String description) {
        super(clazz, in, out, description);
    }

    @Override
    public @Nullable RelNode convert(RelNode rel) {
        final Sort sort = (Sort) rel;
        if (sort.offset != null || sort.fetch != null) {
            return null;
        }
        final RelNode input = sort.getInput();
        return MPCSort.create(
                convert(
                        input,
                        input.getTraitSet().replace(EnumerableConvention.INSTANCE)),
                sort.getCollation(),
                null,
                null);
    }
}
