package com.chainmaker.jobservice.core.calcite.converter;

import com.chainmaker.jobservice.core.calcite.cost.MPCCost;
import com.chainmaker.jobservice.core.calcite.relnode.MPCFilter;
import com.chainmaker.jobservice.core.calcite.relnode.MPCJoin;
import com.chainmaker.jobservice.core.calcite.relnode.MPCProject;
import com.chainmaker.jobservice.core.calcite.relnode.MPCTableScan;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.metadata.RelMetadataQuery;

@Deprecated
public class MPCCostAccumulator {
    public MPCCostAccumulator() {
    }

    public static final MPCCostAccumulator INSTACNE = new MPCCostAccumulator();

    public RelOptCost getAccumulative(RelNode node, RelMetadataQuery mq) {
        //todo
        return null;
    }


    public RelOptCost getAccumulative(MPCTableScan node, RelMetadataQuery mq) {
        //make the most a constant value
//        System.out.println("getaccumulative mpctablescan");
        return node.computeSelfCost(node.getCluster().getPlanner(), mq);
    }

    public RelOptCost getAccumulative(MPCJoin join, RelMetadataQuery mq) {
        return join.computeSelfCost(join.getCluster().getPlanner(), mq);
    }

    public RelOptCost getAccumulative(MPCProject project, RelMetadataQuery mq) {
        return project.computeSelfCost(project.getCluster().getPlanner(), mq);
    }

    public RelOptCost getAccumulative(MPCFilter filter, RelMetadataQuery mq) {
        //make some estimate
        return filter.computeSelfCost(filter.getCluster().getPlanner(), mq);
    }
}
