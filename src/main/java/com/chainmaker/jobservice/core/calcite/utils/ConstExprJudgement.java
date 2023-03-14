package com.chainmaker.jobservice.core.calcite.utils;

/**
 * 常量判断工具类
 */
public class ConstExprJudgement {

    /**
     * 判断常量表达式是否是数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * 判断常量表达式是否是整数
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {
        return str != null && str.matches("^[-\\+]?[\\d]*$");
    }
}
