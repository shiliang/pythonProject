package com.chainmaker.jobservice.core.calcite.cost;

import com.chainmaker.jobservice.core.calcite.relnode.MPCFilter;
import com.chainmaker.jobservice.core.calcite.relnode.MPCProject;
import com.chainmaker.jobservice.core.calcite.relnode.MPCTableScan;
import com.chainmaker.jobservice.core.calcite.utils.RexNodeRowCounter;
import org.apache.calcite.plan.volcano.RelSubset;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.*;
import org.apache.calcite.rel.metadata.*;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeFactoryImpl;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.util.BuiltInMethod;
import org.apache.calcite.util.Util;

/**
 * 自己实现的基数估计方式，用于替换默认的RelMdRowCount
 */
public class MPCRelMdRowCount implements MetadataHandler<BuiltInMetadata.RowCount> {
    public MetadataDef<BuiltInMetadata.RowCount> getDef() {
        return BuiltInMetadata.RowCount.DEF;
    }

    public static final RelMetadataProvider SOURCE = ReflectiveRelMetadataProvider
            .reflectiveSource(BuiltInMethod.ROW_COUNT.method, new MPCRelMdRowCount());


    public Double getRowCount(RelNode rel, RelMetadataQuery mq) {
        return rel.estimateRowCount(mq);
    }

    /**
     * MPCFilter的基数估计
     * 主要需要估计cond的削减因子
     * 更规范的做法应该是重载默认的RelMdSelectivity，然后提供给RelMetadataProvider
     * 后续可以继续改进
     * @param filter
     * @param mq
     * @return
     */
    public Double getRowCount(Filter filter, RelMetadataQuery mq) {
        RexNodeRowCounter rowCounter = new RexNodeRowCounter(filter, mq.getRowCount(filter.getInput()));
        double rowCount = rowCounter.estimateRowCount();
        return rowCount;
    }

    /**
     * MPCProject的基数估计
     * 由于Project算子不会改变基数，所以直接采用其子节点的基数即可
     * @param rel
     * @param mq
     * @return
     */
    public Double getRowCount(Project rel, RelMetadataQuery mq) {
        return mq.getRowCount(rel.getInput());
    }

    /**
     * MPCJoin的基数估计
     * 暂时采用了默认的Join基数估计，之后可改进
     * @param join
     * @param mq
     * @return
     */
    public Double getRowCount(Join join, RelMetadataQuery mq) {
        return RelMdUtil.getJoinRowCount(mq, join, join.getCondition());
//        System.out.println("left = " + join.getLeft().getRowType());
//        System.out.println("right = " + join.getRight().getRowType());
//        System.out.println("cond = " + join.getCondition());
//        System.out.println(((RelSubset) join.getLeft()).getOriginal());
//        System.out.println(((RelSubset) join.getLeft()).getOriginal());
//        System.out.println(((RelSubset) join.getLeft()).getOriginal().getRowType().getFieldNames());
//        return 300.0;
//        final Double left = mq.getRowCount(join.getLeft());
//        final Double right = mq.getRowCount(join.getRight());
//        if (left == null || right == null) {
//            return null;
//        }
//        if (left <= 1D || right <= 1D) {
//            Double max = mq.getMaxRowCount(join);
//            if (max != null && max <= 1D) {
//                return max;
//            }
//        }
//
//        Double selectivity = mq.getSelectivity(join, join.getCondition());
//        if (selectivity == null) {
//            return null;
//        }
//        double innerRowCount = left * right * selectivity;
//        switch (join.getJoinType()) {
//            case INNER:
//                return innerRowCount;
//            case LEFT:
//                return left * (1D - selectivity) + innerRowCount;
//            case RIGHT:
//                return right * (1D - selectivity) + innerRowCount;
//            case FULL:
//                return (left + right) * (1D - selectivity) + innerRowCount;
//            default:
//                throw Util.unexpected(join.getJoinType());
//        }
    }

}
