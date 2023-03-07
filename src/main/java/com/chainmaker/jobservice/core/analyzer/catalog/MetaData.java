package com.chainmaker.jobservice.core.analyzer.catalog;

/**
 * @author gaokang
 * @date 2022-08-30 18:26
 * @description:从数据目录获取元数据信息
 * @version: 1.0.0
 */

public class MetaData {
    private Integer id;
    private Integer databaseType = 0;
    private String tableName;
    private String columnName;
    private String domainID;
    private String dataType;
    private Integer primaryKey;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(Integer databaseType) {
        this.databaseType = databaseType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDomainID() {
        return domainID;
    }

    public void setDomainID(String domainID) {
        this.domainID = domainID;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Integer primaryKey) {
        this.primaryKey = primaryKey;
    }
}
