package com.chainmaker.jobservice.api.builder;

import cn.hutool.core.util.ReUtil;
import org.apache.calcite.plan.volcano.RelSubset;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class CalciteUtil {

    public static String fromNumericName2FieldName(RelNode relNode, String numericalName){
        String pattern1 = "\\{(\\d+)\\}";
        String pattern2 = "\\$f(\\d+)";
        String pattern3 = "(\\d+)";
        Integer fieldRef = null;
        if(ReUtil.isMatch(pattern1, numericalName)) {
            fieldRef= Integer.valueOf(ReUtil.get(pattern1, numericalName, 1));
        }else if(ReUtil.isMatch(pattern2, numericalName)){
            fieldRef = Integer.valueOf(ReUtil.get(pattern2, numericalName, 1)) - 1;
        }else if(ReUtil.isMatch(pattern3, numericalName)){
            fieldRef = Integer.valueOf(numericalName);
        }else{
            return numericalName;
        }

        if (relNode instanceof TableScan){
            List<String> fieldNames =relNode.getRowType().getFieldNames();
            return fieldNames.get(fieldRef);
        }else if(relNode instanceof RelSubset) {
            relNode = ((RelSubset) relNode).getBest();
        }else {
            relNode = relNode.getInputs().get(0);
        }
        return fromNumericName2FieldName(relNode, numericalName);
    }

    public static Pair<String, String> getTableNameAndColumnName(String fieldName){
        if (fieldName.contains(".")){
            String[] split = StringUtils.split(fieldName , ".");
            return Pair.of(split[0], split[1]);
        }else{
            return Pair.of(null, fieldName);
        }
    }

    public static String getColumnName(String fieldName){
        return getTableNameAndColumnName(fieldName).right;
    }

    public static String getTableName(String fieldName){
        return getTableNameAndColumnName(fieldName).left;
    }

    public static String getQualifiedFieldName(String tableOrAlias, String fieldName){
        return tableOrAlias + "." + fieldName;
    }

    public static String num2qualifiedFieldName(RelNode node, String table, String numericalName){
        Pair<String, String> pair = getTableNameAndColumnName(fromNumericName2FieldName(node, numericalName));
        return table + "." + pair.right;
    }
}
