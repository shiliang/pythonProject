package com.chainmaker.jobservice.core.calcite.optimizer.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库表的元属性类
 */
public class TableInfo {
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

    public TableInfo(HashMap<String, FieldInfo> fields, double rowCount, String name, String belongsTo) {
        this.fields = fields;
        this.rowCount = rowCount;
        this.name = name;
        this.OrgDId = belongsTo;
    }

    public String getOrgDId() {
        return OrgDId;
    }

    public void setOrgDId(String orgDId) {
        OrgDId = orgDId;
    }

    public HashMap<String, FieldInfo> getFields() {
        return fields;
    }

    public void setFields(HashMap<String, FieldInfo> fields) {
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRowCount() {
        return rowCount;
    }

    public void setRowCount(double rowCount) {
        this.rowCount = rowCount;
    }
}
