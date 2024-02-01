package com.chainmaker.backendservice.model.dto;

import java.util.ArrayList;
import java.util.List;

public class DataContent {

    List<DataRow> rows;
    List<DataField> fields;

    public DataContent(List<DataField> fieldList, List<DataRow> dataList) {
        this.fields=fieldList;
        this.rows=dataList;
    }

    public DataContent() {

    }

    public void setDataRows(List<DataRow> dataRows) {
        rows = dataRows;
    }

    public List<DataRow> getDataRows() {
        return rows;
    }
    /**
     *
     * @param tableColumns
     * @param
     * @return
     */
    public List<DataRow> getDataRows(  String tableColumns) {
        List<DataRow> rs=new ArrayList<DataRow>();
        List<DataField> fieldsNew=new ArrayList<DataField>();

        String[] cols=tableColumns.split(",");
        for(String col:cols) {
            for(DataField df:fields) {
                if(col.equalsIgnoreCase(df.getColumnName())) {
                    fieldsNew.add(df.clone());
                }
            }
        }

        for(DataRow r:rows) {
            List<DataCell> cells=r.getCells();

            List<DataCell> cellsNew=new ArrayList<DataCell>();
            for(DataCell c:cells) {

                if(c.getColumnName()==null) {
                    continue;
                }
                for(DataField df:fieldsNew) {

                    if(df.getName()==null) {
                        continue;
                    }



                    if(c.getColumnName().equalsIgnoreCase(df.getName())) {
                        cellsNew.add(c.clone());
                        break;
                    }
                }
            }

            DataRow row=new DataRow();
            row.setFields(fieldsNew);
            row.setCells(cellsNew);

            rs.add(row);

        }


        return rs;
    }


    public void setField(List<DataField> fieldNames) {
        this.fields = fieldNames;
    }

    public List<DataField> getFieldNames() {
        return fields;
    }

    public void setDataFields(List<DataField> fieldNames) {
        this.fields = fieldNames;
    }

}
