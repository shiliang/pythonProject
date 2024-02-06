package com.chainmaker.jobservice.core.calcite.optimizer.metadata;

import org.apache.calcite.sql.type.SqlTypeName;

/**
 * 表中某项属性的相关元数据类
 */
public class FieldInfo {
    private String fieldName;               // 属性名
    private SqlTypeName fieldType;          // 类型
    private int dataLength;                 // 数据长度
    private Object maxValue;                // 最大值
    private Object minValue;                // 最小值
    private DistributionType disType;       // 分布情况（均匀分布、正态分布）
    private String tableName;               // 所属表名
    private String domainID;                // domainID

    public FieldInfo(String name, String type, Object maxValue, Object minValue, DistributionType disType, String tableName, int dataLength, String domainID) {
        fieldName = name;
        fieldType = SqlTypeName.get(type.toUpperCase());
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.disType = disType;
        this.tableName = tableName;
        this.dataLength = dataLength;
        this.domainID = domainID;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public SqlTypeName getFieldType() {
        return fieldType;
    }

    public void setFieldType(SqlTypeName fieldType) {
        this.fieldType = fieldType;
    }

    public Object getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Object maxValue) {
        this.maxValue = maxValue;
    }

    public Object getMinValue() {
        return minValue;
    }

    public void setMinValue(Object minValue) {
        this.minValue = minValue;
    }

    public DistributionType getDisType() {
        return disType;
    }

    public void setDisType(DistributionType disType) {
        this.disType = disType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDomainID() {
        return domainID;
    }

    public enum DistributionType {
        Uniform,
        Normal,
    }
}
