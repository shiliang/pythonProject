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
    private String orgName;


    public TableInfo() {

    }

    public TableInfo(HashMap<String, FieldInfo> fields, double rowCount, String name, String belongsTo, String orgId, String assetName) {
        this.fields = fields;
        this.rowCount = rowCount;
        this.name = name;
        this.OrgDId = orgId;
        this.orgName = belongsTo;
        this.assetName =assetName;
    }
}
