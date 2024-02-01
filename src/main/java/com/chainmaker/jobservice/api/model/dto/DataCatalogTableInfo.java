package com.chainmaker.backendservice.model.dto;

public class DataCatalogTableInfo {
    private String field;
    private String type;
    private String nullable;
    private String key;
    private String defaultInfo;
    private String extra;

    public String getDefaultInfo() {
        return defaultInfo;
    }

    public String getExtra() {
        return extra;
    }

    public String getField() {
        return field;
    }

    public String getKey() {
        return key;
    }

    public String getNullable() {
        return nullable;
    }

    public String getType() {
        return type;
    }

    public void setDefaultInfo(String defaultInfo) {
        this.defaultInfo = defaultInfo;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setNullable(String nullable) {
        this.nullable = nullable;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "DataCatalogTablePO{" +
                "field='" + field + '\'' +
                ", type='" + type + '\'' +
                ", nullable='" + nullable + '\'' +
                ", key='" + key + '\'' +
                ", defaultInfo='" + defaultInfo + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }

}
