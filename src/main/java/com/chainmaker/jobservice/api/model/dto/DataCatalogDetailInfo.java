package com.chainmaker.backendservice.model.dto;

import com.chainmaker.backendservice.model.dto.DataCatalogDataSecurityInfo;

public class DataCatalogDetailInfo {
    private String id;
    private String dataCatalogId;
    private String dataCatalogName;
    private String name;
    private int dataType;
    private String description;
    private int dataLength;
    private int primaryKey;
    private String dataTypeValue;

    private DataCatalogDataSecurityInfo dataSecurity;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataCatalogId() {
        return dataCatalogId;
    }

    public void setDataCatalogId(String datacatalogId) {
        this.dataCatalogId = dataCatalogId;
    }

    public String getDataCatalogName() {
        return dataCatalogName;
    }

    public void setDataCatalogName(String dataCatalogName) {
        this.dataCatalogName = dataCatalogName;
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

    public String getDataTypeValue() {return dataTypeValue;}

    public void setDataTypeValue(String dataTypeValue) {this.dataTypeValue = dataTypeValue;}

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

    public DataCatalogDataSecurityInfo getDataSecurity() {
        return dataSecurity;
    }

    public void setDataSecurity(DataCatalogDataSecurityInfo dataSecurity) {
        this.dataSecurity = dataSecurity;
    }


    @Override
    public String toString() {
        return "DataCatalogDetailInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", datacatalogId='"+ dataCatalogId+ '\''+
                ", datacatalogName='"+ dataCatalogName+
                ", dataType=" + dataType +
                ", description='" + description + '\'' +
                ", dataLength=" + dataLength + '\'' +
                ", primaryKey=" + primaryKey + '\'' +
                ", dataTypeValue=" + dataTypeValue + '\'' +
                ", dataSecurity=" + dataSecurity +
                '}';
    }
}