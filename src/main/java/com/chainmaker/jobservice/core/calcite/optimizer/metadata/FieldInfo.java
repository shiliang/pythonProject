package com.chainmaker.jobservice.core.calcite.optimizer.metadata;

import lombok.Data;
import org.apache.calcite.sql.type.SqlTypeName;

/**
 * 表中某项属性的相关元数据类
 */
@Data
public class FieldInfo {
    private String fieldName;               // 属性名
    private SqlTypeName fieldType;          // 类型
    private int dataLength;                 // 数据长度
    private Object maxValue;                // 最大值
    private Object minValue;                // 最小值
    private DistributionType disType;       // 分布情况（均匀分布、正态分布）
    private String tableName;               // 所属表名
    private String domainID;                // domainID
    private String databaseName;
    private String comments;

    public FieldInfo(String name, String type, Object maxValue, Object minValue, DistributionType disType, String tableName, int dataLength, String domainID, String comments, String databaseName) {
        fieldName = name;
        fieldType = SqlTypeName.get(type.toUpperCase());
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.disType = disType;
        this.tableName = tableName;
        this.dataLength = dataLength;
        this.domainID = domainID;
        this.comments = comments;
        this.databaseName = databaseName;
    }

    public enum DistributionType {
        Uniform,
        Normal,
    }
}
