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
    private String OrgId;                           // 数据来源

    public TableInfo() {
        fields = null;
        rowCount = 0;
        name = null;
        OrgId = null;
    }

    public TableInfo(HashMap<String, FieldInfo> fields, double rowCount, String name, String belongsTo) {
        this.fields = fields;
        this.rowCount = rowCount;
        this.name = name;
        this.OrgId = belongsTo;
    }

    public String getOrgId() {
        return OrgId;
    }

    public String getOrgDID() {
        for (Map.Entry<String, FieldInfo> e: fields.entrySet()) {
            return e.getValue().getDomainID();
        }
        return null;
    }

    public void setOrgId(String orgId) {
        OrgId = orgId;
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
