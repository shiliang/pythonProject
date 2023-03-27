package com.chainmaker.jobservice.core.calcite.converter;

import com.chainmaker.jobservice.core.calcite.cost.MPCCost;
import com.chainmaker.jobservice.core.calcite.relnode.MPCFilter;
import com.chainmaker.jobservice.core.calcite.relnode.MPCJoin;
import com.chainmaker.jobservice.core.calcite.relnode.MPCProject;
import com.chainmaker.jobservice.core.calcite.relnode.MPCTableScan;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.rel.metadata.*;
import org.apache.calcite.util.BuiltInMethod;

@Deprecated
public class MPCCumulativeCost implements MetadataHandler<BuiltInMetadata.CumulativeCost> {
    public static final RelMetadataProvider SOURCE =
            ReflectiveRelMetadataProvider.reflectiveSource(
                    BuiltInMethod.CUMULATIVE_COST.method, new MPCCumulativeCost());

    public MPCCumulativeCost() {
    }

    @Override
    public MetadataDef<BuiltInMetadata.CumulativeCost> getDef() {
        return BuiltInMetadata.CumulativeCost.DEF;
    }

    public RelOptCost getCumulativeCost(MPCTableScan tableScan, RelMetadataQuery mq) {
        return MPCCostAccumulator.INSTACNE.getAccumulative(tableScan, mq);
    }

    public RelOptCost getCumulativeCost(MPCProject dogProject, RelMetadataQuery mq) {
        return MPCCostAccumulator.INSTACNE.getAccumulative(dogProject, mq);
    }

    public RelOptCost getCumulativeCost(MPCFilter filter, RelMetadataQuery mq) {
        return MPCCostAccumulator.INSTACNE.getAccumulative(filter, mq);
    }

    public RelOptCost getCumulativeCost(MPCJoin join, RelMetadataQuery mq) {
        return MPCCostAccumulator.INSTACNE.getAccumulative(join, mq);
    }
}
