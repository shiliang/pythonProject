package com.chainmaker.jobservice.core.analyzer.catalog;

import lombok.Data;

@Data
public class DataCatalogDetailInfo {
    private String id;
    private String name;
    private int dataType;
    private String description;
    private int dataLength;
    private int primaryKey;

    private DataCatalogDataSecurityInfo dataSecurity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public int getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(int primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Override
    public String toString() {
        return "DataCatalogDetailInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", dataType=" + dataType +
                ", description='" + description + '\'' +
                ", dataLength=" + dataLength +
                ", primaryKey=" + primaryKey +
                '}';
    }
}