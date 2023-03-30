package com.chainmaker.jobservice.core.calcite.utils;

import com.chainmaker.jobservice.core.calcite.optimizer.metadata.FieldInfo;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.MPCMetadata;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.tomcat.util.http.LegacyCookieProcessor;

import java.util.List;

import static com.chainmaker.jobservice.core.calcite.utils.ConstExprJudgement.isNumeric;
import static java.lang.Math.*;

/**
 * 处理Filter条件的类，本质上就是做了RelMdSelectivity类的工作
 * 其实通过重载RelMdSelectivity类是更好的做法，之后可以改进
 */
public class RexNodeRowCounter {
    private Filter filter;
    private double inputRowCount;
    private double outputRowCount;
    private double coeff;       //  过滤系数，最终乘上inputRowCount就是outputRowCount

    public RexNodeRowCounter(Filter filter, double inRC) {
        this.filter = filter;
        inputRowCount = inRC;
        outputRowCount = inputRowCount;
        coeff = 1.0;
    }

    public double estimateRowCount() {
        coeff = estimateRowCount(filter.getCondition());
        coeff = max(coeff, 0.05);
        outputRowCount *= coeff;
        return outputRowCount;
    }

    public double estimateRowCount(RexNode rexNode) {

        double tmpCoeff = 1.0;
        SqlKind kind = rexNode.getKind();
        List<RexNode> operands = ((RexCall) rexNode).getOperands();
        switch (kind) {
            case AND:
            case OR:
                tmpCoeff *= estimateLogcial(operands, kind);
                break;
            case EQUALS:
            case NOT_EQUALS:
                tmpCoeff *= estimateEqualOrNot(operands, kind);
                break;
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
                tmpCoeff *= estimateOther(operands, kind);
                break;
            default:
                break;
        }
        return tmpCoeff;
    }

    /**
     *估算其他的Filter会有多少基数（>, >=, <, <=）
     * @param kind 因为可以共用大部分代码，所以只需要传入kind就可以复用
     * @return 返回过滤系数，乘上outputRowCount即可
     */
    public double estimateOther(List<RexNode> operands, SqlKind kind) {
        RexNode left = operands.get(0);
        RexNode right = operands.get(1);
//        System.out.println("expr = " + left + " " + kind + " " + right);
//        System.out.println(((RexInputRef)left));
        if (right instanceof RexInputRef && left instanceof RexLiteral) {
            RexNode tmp = left;
            left = right;
            right = tmp;
        }
        MPCMetadata mpcMetadata = MPCMetadata.getInstance();
        if (left instanceof RexInputRef) {
//            FieldInfo leftInfo = mpcMetadata.getFieldInfo((RexInputRef) left);
            FieldInfo leftInfo = mpcMetadata.getFieldInfo(filter.getRowType().getFieldNames().get(((RexInputRef) left).getIndex()));
//            System.out.println(filter.getRowType().getFieldNames().get(((RexInputRef) left).getIndex()));
            if (right instanceof RexInputRef) {
                // 左右都是属性名（TA.A > TB.B）
                FieldInfo rightInfo = mpcMetadata.getFieldInfo(filter.getRowType().getFieldNames().get(((RexInputRef) right).getIndex()));
//                System.out.println("leftInfo Name = " + leftInfo.getFieldName());
//                System.out.println("rightInfo Name = " + rightInfo.getFieldName());
                if (leftInfo.getFieldType() == SqlTypeName.VARCHAR) {
                    if (rightInfo.getFieldType() != SqlTypeName.VARCHAR) {
                        return 0;
                    } else {
                        // varchar比大小无法估计，直接取0.5
                        return 0.5;
                    }
                } else {
                    if (rightInfo.getFieldType() == SqlTypeName.VARCHAR) {
                        return 0;
                    } else {
                        if (leftInfo.getMaxValue() == null && leftInfo.getMinValue() == null ||
                            rightInfo.getMaxValue() == null && rightInfo.getMinValue() == null) {
                            return 0.5;
                        }
                        double lmax = Double.parseDouble(leftInfo.getMaxValue().toString());
                        double lmin = Double.parseDouble(leftInfo.getMinValue().toString());
                        double rmax = Double.parseDouble(rightInfo.getMaxValue().toString());
                        double rmin = Double.parseDouble(rightInfo.getMinValue().toString());
                        if (leftInfo.getDisType() == FieldInfo.DistributionType.Uniform) {
                            if (rightInfo.getDisType() == FieldInfo.DistributionType.Uniform) {
                                // 左右都是均匀分布
                                switch (kind) {
                                    case GREATER_THAN:
                                        return 0;
                                    case GREATER_THAN_OR_EQUAL:
                                        return 0;
                                    case LESS_THAN:
                                        return 0;
                                    case LESS_THAN_OR_EQUAL:
                                        return 0;
                                    case DEFAULT:
                                        return 0;
                                }
                            } else {
                                // 左均匀分布+右正态分布
                            }
                        } else {
                            if (rightInfo.getDisType() == FieldInfo.DistributionType.Uniform) {
                                // 左正态分布+右均匀分布
                            } else {
                                // 左右都是正态分布
                            }
                        }
                    }
                }
            } else if (right instanceof RexLiteral) {
                // 只有左边是属性名，右边是常量（TA.A > 5）
//                System.out.println("leftInfo Name = " + leftInfo.getFieldName());
//                System.out.println("rightValue = " + ((RexLiteral) right).getValue());
                if (isNumeric(((RexLiteral) right).getValue().toString())) {
                    // 右边如果是数字
                    SqlTypeName fieldType = leftInfo.getFieldType();
                    if (fieldType != SqlTypeName.INTEGER && fieldType != SqlTypeName.DOUBLE && fieldType != SqlTypeName.FLOAT) {
                        System.err.println("error, filter type not match");     // 类型不匹配，报错
                        return 0;
                    }
                    // 不提供最大最小值，则无法估计，取一半过滤
                    if (leftInfo.getMaxValue() == null && leftInfo.getMinValue() == null) {
                        return 0.5;
                    }
                    double maxValue = Double.parseDouble(leftInfo.getMaxValue().toString());
                    double minValue = Double.parseDouble(leftInfo.getMinValue().toString());
                    double value = Double.parseDouble(((RexLiteral) right).getValue().toString());

                    if (leftInfo.getDisType() == FieldInfo.DistributionType.Uniform) {
                        // 均匀分布
                        switch (kind) {
                            case GREATER_THAN:
                                if (value < minValue) {
                                    return 1.0;
                                } else if (value >= maxValue){
                                    return 0.0;
                                } else {
                                    return min(max((maxValue-value) / (maxValue-minValue+1), 0.05), 0.95);
                                }
                            case GREATER_THAN_OR_EQUAL:
                                if (value <= minValue) {
                                    return 1.0;
                                } else if (value > maxValue){
                                    return 0.0;
                                } else {
                                    return min(max((maxValue-value+1) / (maxValue-minValue+1), 0.05), 0.95);
                                }
                            case LESS_THAN:
                                if (value > maxValue) {
                                    return 1.0;
                                } else if (value <= minValue) {
                                    return 0.0;
                                } else {
                                    return min(max((value-minValue) / (maxValue-minValue+1), 0.05), 0.95);
                                }
                            case LESS_THAN_OR_EQUAL:
                                if (value >= maxValue) {
                                    return 1.0;
                                } else if (value < minValue) {
                                    return 0.0;
                                } else {
                                    return min(max((value-minValue+1) / (maxValue-minValue+1), 0.05), 0.95);
                                }
                            case DEFAULT:
                                return 0;
                        }
                    } else if (leftInfo.getDisType() == FieldInfo.DistributionType.Normal) {
                        // 正态分布，之后再说

                    } else {
                        // 其他分布，不一定需要
                    }
                } else {
                    // 右边不是数字，greater than对字符串一般没啥意义，之后可能会有日期
                }
            } else {
                // 右边不是常量也不是属性，应该不会出现
                ;
            }
        } else {
            // 两边都没有属性，应该不会出现，会自动替换成true或者false
            ;
        }
        return 0;
    }

    /**
     * 估算带逻辑运算的Filter会有多少基数（TA.A = 5 AND TB.ID > 10）
     * @return
     */
    public double estimateLogcial(List<RexNode> operands, SqlKind kind) {
        double ans = 1;
        if (kind == SqlKind.AND) {
            for (RexNode op : operands) {
                ans *= estimateRowCount(op);
            }
        } else {
            double antiAns = 1;
            for (RexNode op : operands) {
                antiAns *= (1-estimateRowCount(op));
            }
            ans = 1-antiAns;
        }
//        System.out.println("logical estimate return " + ans);
        return ans;
    }

    /**
     * 估算相等的Filter会有多少基数（TA.A = 5）
     * @return
     */
    public double estimateEqualOrNot(List<RexNode> operands, SqlKind kind) {
        RexNode left = operands.get(0);
        RexNode right = operands.get(1);
        if (right instanceof RexInputRef && left instanceof RexLiteral) {
            RexNode tmp = left;
            left = right;
            right = tmp;
        }
        MPCMetadata mpcMetadata = MPCMetadata.getInstance();
        if (left instanceof RexInputRef) {
            FieldInfo leftInfo = mpcMetadata.getFieldInfo(filter.getRowType().getFieldNames().get(((RexInputRef) left).getIndex()));
            if (right instanceof RexInputRef) {
                // 左右都是属性名（TA.ID = TB.ID）
                FieldInfo rightInfo = mpcMetadata.getFieldInfo(filter.getRowType().getFieldNames().get(((RexInputRef) right).getIndex()));
//                System.out.println("leftInfo Name = " + leftInfo.getFieldName());
//                System.out.println("rightInfo Name = " + rightInfo.getFieldName());
                SqlTypeName leftType = leftInfo.getFieldType();
                SqlTypeName rightType = rightInfo.getFieldType();
                if (leftType != rightType) {
                    System.err.println("error, filter type not match");     // 类型不匹配，报错
                    return 0;
                }
                // 如果是字符串类型
                if (leftType == SqlTypeName.VARCHAR) {
                    if (kind == SqlKind.EQUALS) {
                        return (random()+0.5) / 2;      // 0.25 - 0.75
                    } else {
                        return (random()/4) + 0.5;      // 0.5 - 0.75
                    }
                } else if (leftType == SqlTypeName.INTEGER || leftType == SqlTypeName.DOUBLE || leftType == SqlTypeName.FLOAT){
                    // 如果是数字
                    if (leftInfo.getMaxValue() == null && leftInfo.getMinValue() == null ||
                            rightInfo.getMaxValue() == null && rightInfo.getMinValue() == null) {
                        return 0.5;
                    }
                    double lmax = Double.parseDouble(leftInfo.getMaxValue().toString());
                    double lmin = Double.parseDouble(leftInfo.getMinValue().toString());
                    double rmax = Double.parseDouble(rightInfo.getMaxValue().toString());
                    double rmin = Double.parseDouble(rightInfo.getMinValue().toString());
                    double minValue = max(lmin, rmin);
                    double maxValue = min(lmax, rmax);
                    if (leftInfo.getDisType() == FieldInfo.DistributionType.Uniform) {
                        if (rightInfo.getDisType() == FieldInfo.DistributionType.Uniform) {
                            return min(max((maxValue-minValue) / (max(lmax, rmax) - min(lmin, rmin)), 0.05), 0.95);
                        } else {
                            ;
                        }
                    } else {
                        if (rightInfo.getDisType() == FieldInfo.DistributionType.Uniform) {
                            ;
                        } else {
                            ;
                        }
                    }
                } else {
                    // 如果是其他类型
                    ;
                }


            } else if (right instanceof RexLiteral) {
                // 只有左边是属性名，右边是常量（TA.A = 5）
//                System.out.println("leftInfo Name = " + leftInfo.getFieldName());
//                System.out.println("rightValue = " + ((RexLiteral) right).getValue());
                if (isNumeric(((RexLiteral) right).getValue().toString())) {
                    // 右边如果是数字
                    SqlTypeName fieldType = leftInfo.getFieldType();
                    if (fieldType != SqlTypeName.INTEGER && fieldType != SqlTypeName.DOUBLE && fieldType != SqlTypeName.FLOAT) {
                        System.err.println("error, filter type not match");     // 类型不匹配，报错
                        return 0;
                    }
                    if (leftInfo.getMaxValue() == null && leftInfo.getMinValue() == null) {
                        return 0.5;
                    }
                    double maxValue = Double.parseDouble(leftInfo.getMaxValue().toString());
                    double minValue = Double.parseDouble(leftInfo.getMinValue().toString());
                    double value = Double.parseDouble(((RexLiteral) right).getValue().toString());
                    if (leftInfo.getDisType() == FieldInfo.DistributionType.Uniform) {
                        // 均匀分布
                        if (kind == SqlKind.EQUALS) {
                            if (value < minValue || value > maxValue) {
                                return 0;
                            } else {
                                return 1.0 / (maxValue - minValue+1);
                            }
                        } else {
                            if (value < minValue || value > maxValue) {
                                return 1;
                            } else {
                                return 1 - (1.0 / (maxValue-minValue+1));
                            }
                        }
                    } else if (leftInfo.getDisType() == FieldInfo.DistributionType.Normal) {
                        // 正态分布，之后再说

                    } else {
                        // 其他分布，不一定需要
                    }
                } else {
                    // 右边不是数字，那只能是字符串，之后可能会有日期
                    SqlTypeName fieldType = leftInfo.getFieldType();
                    if (fieldType != SqlTypeName.VARCHAR) {
                        System.err.println("error, filter type not match");     // 类型不匹配，报错
                        return 0;
                    }
                    if (kind == SqlKind.EQUALS) {
                        return (random() + 0.5) / 5;
                    } else {
                        return (random() / 5) + 0.7;
                    }
                    /*
                    0.0 - 1.0       0.0 - 1.0
                    0.5 - 1.5       0.0 - 0.2
                    0.1 - 0.3       0.7 - 0.9
                     */
                }
            } else {
                // 右边不是常量也不是属性，应该不会出现
                ;
            }
        } else {
            // 两边都没有属性，应该不会出现，会自动替换成true或者false
            ;
        }
        return 0;
    }

    public double getInputRowCount() {
        return inputRowCount;
    }

    public void setInputRowCount(double inputRowCount) {
        this.inputRowCount = inputRowCount;
    }

    public double getOutputRowCount() {
        return max(coeff * outputRowCount, 1.0);
    }

    public void setOutputRowCount(double outputRowCount) {
        this.outputRowCount = outputRowCount;
    }

    public double getCoeff() {
        return coeff;
    }

    public void setCoeff(double coeff) {
        this.coeff = coeff;
    }
}
