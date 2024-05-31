package com.chainmaker.parser;

import org.apache.calcite.sql.type.SqlTypeName;

import java.util.ArrayList;
import java.util.List;

public class TestSqlType {
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
            SqlTypeName fieldType = SqlTypeName.get(dataType.toUpperCase());
            System.out.println("dataType: " + dataType + "   convert to: " +fieldType);
        }
    }
}
