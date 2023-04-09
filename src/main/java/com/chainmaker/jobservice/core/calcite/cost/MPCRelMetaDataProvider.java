package com.chainmaker.jobservice.core.calcite.cost;

import com.chainmaker.jobservice.core.calcite.converter.MPCCumulativeCost;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.apache.calcite.plan.volcano.VolcanoRelMetadataProvider;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.metadata.*;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 实现自己的元数据提供器，可根据需要修改某些计算方式
 * 此处暂时只修改了基数RowCount的计算方式
 */
public class MPCRelMetaDataProvider extends ChainedRelMetadataProvider{

    public static final RelMetadataProvider relMetaDataProvider = new MPCRelMetaDataProvider();
    public MPCRelMetaDataProvider() {
        super(
            ImmutableList.of(
                    MPCRelMdRowCount.SOURCE,
                    RelMdPercentageOriginalRows.SOURCE,
                    RelMdColumnOrigins.SOURCE,
                    RelMdExpressionLineage.SOURCE,
                    RelMdTableReferences.SOURCE,
                    RelMdNodeTypes.SOURCE,
                    RelMdMaxRowCount.SOURCE,
                    RelMdMinRowCount.SOURCE,
                    RelMdUniqueKeys.SOURCE,
                    RelMdColumnUniqueness.SOURCE,
                    RelMdPopulationSize.SOURCE,
                    RelMdSize.SOURCE,
                    RelMdParallelism.SOURCE,
                    RelMdDistribution.SOURCE,
                    RelMdMemory.SOURCE,
                    RelMdDistinctRowCount.SOURCE,
                    RelMdSelectivity.SOURCE,
                    RelMdExplainVisibility.SOURCE,
                    RelMdPredicates.SOURCE,
                    RelMdAllPredicates.SOURCE,
                    RelMdCollation.SOURCE
            ));
    }


}

