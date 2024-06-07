package com.chainmaker.jobservice.core.calcite.optimizer.metadata;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.ArrayList;
import java.util.List;

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
    private String domainName;
    private String databaseName;
    private String comments;
    private String uniqueName;

    public FieldInfo(String name, String type, Object maxValue, Object minValue, DistributionType disType, String tableName, int dataLength, String domainID, String domainName, String comments, String databaseName, String assetName) {
        fieldName = name;
        uniqueName = assetName+"."+name;
        if (!type.equalsIgnoreCase("BIGINT") && type.toUpperCase().contains("INT")){
            type = "INTEGER";
        }else if(type.toUpperCase().contains("TEXT")){
            type = "VARCHAR";
        }
        fieldType = SqlTypeName.get(type.toUpperCase());
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.disType = disType;
        this.tableName = tableName;
        this.dataLength = dataLength;
        this.domainID = domainID;
        this.comments = comments;
        this.databaseName = databaseName;
        this.domainName = domainName;
    }

    public static FieldInfo defaultValueField() {
        return new FieldInfo(
                "defaultValue",
                "VARCHAR",
                null,
                null,
                DistributionType.Uniform,
                null,
                0,
                null,
                null,
                null,
                null,
                null
        );
    }

    public enum DistributionType {
        Uniform,
        Normal,
    }

    public static void main(String[] args) {
        List<String> mysqlDataTypes = new ArrayList<>();
        // 整数类型
        mysqlDataTypes.add("TINYINT");
        mysqlDataTypes.add("SMALLINT");
        mysqlDataTypes.add("MEDIUMINT");
        mysqlDataTypes.add("INT");
        mysqlDataTypes.add("INT UNSIGNED");
        mysqlDataTypes.add("INTEGER");
        mysqlDataTypes.add("BIGINT");

        // 浮点数类型
        mysqlDataTypes.add("FLOAT");
        mysqlDataTypes.add("DOUBLE");
        mysqlDataTypes.add("DECIMAL");
        mysqlDataTypes.add("NUMERIC");

        // 字符串类型
        mysqlDataTypes.add("CHAR");
        mysqlDataTypes.add("VARCHAR");
        mysqlDataTypes.add("TEXT");
        mysqlDataTypes.add("BINARY");
        mysqlDataTypes.add("VARBINARY");
        mysqlDataTypes.add("TINYTEXT");
        mysqlDataTypes.add("TEXT");
        mysqlDataTypes.add("MEDIUMTEXT");
        mysqlDataTypes.add("LONGTEXT");
        mysqlDataTypes.add("TINYBLOB");
        mysqlDataTypes.add("BLOB");
        mysqlDataTypes.add("MEDIUMBLOB");
        mysqlDataTypes.add("LONGBLOB");

        // 其他类型
        mysqlDataTypes.add("TEXT");
        mysqlDataTypes.add("DATE");
        mysqlDataTypes.add("TIME");
        mysqlDataTypes.add("DATETIME");
        mysqlDataTypes.add("TIMESTAMP");
        mysqlDataTypes.add("YEAR");

        // 打印存储的类型
        for (String dataType : mysqlDataTypes) {
            FieldInfo fieldType = new FieldInfo("test", dataType, null, null, null, null, 0, null, null, null, null, null);
            System.out.println("dataType: " + dataType + "   convert to: " + JSON.toJSONString(fieldType));
        }

    }
}
