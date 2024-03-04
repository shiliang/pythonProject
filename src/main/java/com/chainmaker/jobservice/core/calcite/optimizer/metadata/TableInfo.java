package com.chainmaker.jobservice.core.calcite.optimizer.metadata;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库表的元属性类
 */
@Data
public class TableInfo {
    private String assetName;
    private HashMap<String, FieldInfo> fields;      // 表中的所有属性
    private double rowCount;                        // 行数
    private String name;                            // 表名
    private String OrgDId;                          // 数据来源

    public TableInfo() {
        fields = null;
        rowCount = 0;
        name = null;
        OrgDId = null;
    }

    public TableInfo(HashMap<String, FieldInfo> fields, double rowCount, String name, String belongsTo, String assetName) {
        this.fields = fields;
        this.rowCount = rowCount;
        this.name = name;
        this.OrgDId = belongsTo;
        this.assetName =assetName;
    }
}
