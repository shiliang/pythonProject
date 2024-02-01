package com.chainmaker.backendservice.model.dto;

public class DataField {

    private String name; // 属性名称
    private String dataType; // 属性的数据类型
    private String columnName;  // 表列名称

    public DataField() {
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }

    public String getName() {
        return name;
    }

    public void setColumnName(String name) {
        this.columnName = name;
    }

    public String getColumnName() {
        return columnName;
    }

    public DataField clone() {
        DataField df=new DataField();
        df.setColumnName(columnName);
        df.setName(name);
        df.setDataType(dataType);

        return df;
    }

}
