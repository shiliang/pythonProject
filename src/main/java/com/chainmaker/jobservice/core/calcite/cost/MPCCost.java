package com.chainmaker.jobservice.core.calcite.cost;

import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptCostFactory;
import org.apache.calcite.plan.RelOptUtil;

/**
 * 自定义算子代价的计算方式
 * 现阶段主要改动是将原来用rowCount代表cost的算法
 * 改成了用rowCount+Cpu+IO代表cost
 * 主要体现在isLe函数的修改
 * 至于其他运算，和原版没什么大的区别
 */
public class MPCCost implements RelOptCost {

    // 一些静态常量的定义
    public static final MPCCost                 INFINITY = new MPCCost(Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY) {
        @Override
        public String toString() {
            return "{inf}";
        }
    };

    public static final MPCCost                 HUGE     = new MPCCost(Double.MAX_VALUE, Double.MAX_VALUE,
            Double.MAX_VALUE) {
        @Override
        public String toString() {
            return "{huge}";
        }
    };

    public static final MPCCost                 ZERO     = new MPCCost(0.0, 0.0, 0.0) {
        @Override
        public String toString() {
            return "{0}";
        }
    };

    public static final MPCCost                 TINY     = new MPCCost(1.0, 1.0, 0.0) {
        @Override
        public String toString() {
            return "{tiny}";
        }
    };

    public static final RelOptCostFactory FACTORY  = new Factory();

    // ~ Instance fields --------------------------------------------------------

    final double                          cpu;
    final double                          io;
    final double                          rowCount;

    // ~ Constructors -----------------------------------------------------------

    public MPCCost(double rowCount, double cpu, double io) {
        assert rowCount >= 0d;
        assert cpu >= 0d;
        assert io >= 0d;
        this.rowCount = rowCount;
        this.cpu = cpu;
        this.io = io;
    }

    // ~ Methods ----------------------------------------------------------------

    public double getCpu() {
        return cpu;
    }

    public boolean isInfinite() {
        return (this == INFINITY) || (this.rowCount == Double.POSITIVE_INFINITY)
                || (this.cpu == Double.POSITIVE_INFINITY) || (this.io == Double.POSITIVE_INFINITY);
    }

    public double getIo() {
        return io;
    }

    /**
     * 不同Cost之间的比较函数，只需修改比较算法即可实现新的代价计算
     * @param other
     * @return
     */
    public boolean isLe(RelOptCost other) {
//        System.out.println("enter MPCCost isLe");
        if ((this.rowCount + this.cpu + this.io < other.getRows() + other.getCpu() + other.getIo())) {
            return true;
        }
        return false;
    }

    public boolean isLt(RelOptCost other) {
        return isLe(other) && !equals(other);
    }

    public double getRows() {
        return rowCount;
    }

    // 判断cost是否相等
    public boolean equals(RelOptCost other) {
        return (this == other) ||
                ((this.cpu + this.io == other.getCpu() + other.getIo()) &&
                        (this.rowCount == other.getRows()));
    }

    public boolean isEqWithEpsilon(RelOptCost other) {
        return (this == other)
                || ((Math.abs(this.io - other.getIo()) < RelOptUtil.EPSILON)
                && (Math.abs(this.cpu - other.getCpu()) < RelOptUtil.EPSILON) && (Math
                .abs(this.rowCount - other.getRows()) < RelOptUtil.EPSILON));
    }

    // cost减法，三者分别相减即可
    public RelOptCost minus(RelOptCost other) {
        if (this == INFINITY) {
            return this;
        }

        return new MPCCost(this.rowCount - other.getRows(), this.cpu - other.getCpu(),
                this.io - other.getIo());
    }

    // cost乘以因子，三者分别乘以该因子即可
    public RelOptCost multiplyBy(double factor) {
        if (this == INFINITY) {
            return this;
        }
        return new MPCCost(rowCount * factor, cpu * factor, io * factor);
    }

    // cost除法，计算所有非零和有限因素的倍率的平均值
    public double divideBy(RelOptCost cost) {
        double d = 1;
        double n = 0;
        if ((this.rowCount != 0) && !Double.isInfinite(this.rowCount) && (cost.getRows() != 0)
                && !Double.isInfinite(cost.getRows())) {
            d *= this.rowCount / cost.getRows();
            ++n;
        }
        if ((this.cpu != 0) && !Double.isInfinite(this.cpu) && (cost.getCpu() != 0)
                && !Double.isInfinite(cost.getCpu())) {
            d *= this.cpu / cost.getCpu();
            ++n;
        }
        if ((this.io != 0) && !Double.isInfinite(this.io) && (cost.getIo() != 0)
                && !Double.isInfinite(cost.getIo())) {
            d *= this.io / cost.getIo();
            ++n;
        }
        if (n == 0) {
            return 1.0;
        }
        return Math.pow(d, 1 / n);
    }

    // cost加法，三者分别相加即可
    public RelOptCost plus(RelOptCost other) {
        if ((this == INFINITY) || (other.isInfinite())) {
            return INFINITY;
        }
        return new MPCCost(this.rowCount + other.getRows(), this.cpu + other.getCpu(), this.io
                + other.getIo());
    }

    @Override
    public String toString() {
        return "{" + rowCount + " rows, " + cpu + " cpu, " + io + " io}";
    }

    // costFactory类，在初始配置config的时候需要使用，以次将cost计算方式替换成MPCCost的计算方式
    private static class Factory implements RelOptCostFactory {
        private Factory() {
        }

        public RelOptCost makeCost(double rowCount, double cpu, double io) {
            return new MPCCost(rowCount, cpu, io);
        }

        public RelOptCost makeHugeCost() {
            return HUGE;
        }

        public MPCCost makeInfiniteCost() {
            return INFINITY;
        }

        public MPCCost makeTinyCost() {
            return TINY;
        }

        public MPCCost makeZeroCost() {
            return ZERO;
        }
    }
}
