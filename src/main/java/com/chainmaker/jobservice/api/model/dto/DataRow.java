package com.chainmaker.backendservice.model.dto;

import java.util.List;

public class DataRow {

    List<DataCell> cells;
    List<DataField> fieldNames;

    public DataRow() {
    }

    public void setCells(List<DataCell> cells) {
        this.cells = cells;
    }

    public List<DataCell> getCells() {
        return cells;
    }

    public void setFields(List<DataField> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public List<DataField> getFields() {
        return fieldNames;
    }

    public String getFieldNames() {
        if (fieldNames.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (DataField df:this.fieldNames) {
            if(sb.length()>0) {
                sb.append(',');
            }
            sb.append(df.getColumnName());
        }
        return sb.toString();
    }

    public String getValues() {
        if (cells.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(DataCell cell:cells) {
            if(sb.length()>0) {
                sb.append(",");
            }
            Object v=cell.getData();

            if(v==null||v.toString().trim().length()==0) {
                sb.append("NULL");
            }else
            if(cell.isNumber()) {
                sb.append(v);
            }else
                sb.append("'").append(v).append("'");
        }

        return sb.toString();
    }

}
