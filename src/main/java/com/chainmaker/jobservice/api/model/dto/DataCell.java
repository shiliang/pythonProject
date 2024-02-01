package com.chainmaker.backendservice.model.dto;

public class DataCell {

        private String dataType;
        private Object data;  // 数据
        private String columnName; //列名称

        public DataCell() {
        }

        public void setData(Object data) {
            this.data = data;
        }

        public void setType(String dataType) {
            this.dataType = dataType;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public Object getData() {
            return data;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public String getColumnName() {
            return columnName;
        }


        public boolean isNumber(){
            return "number".equalsIgnoreCase(dataType)
                    ||"int".equalsIgnoreCase(dataType)
                    ||"integer".equalsIgnoreCase(dataType);
        }


        public DataCell clone() {
            DataCell c=new DataCell();
            c.setColumnName(columnName);
            c.setData(data);
            c.setDataType(dataType);

            return c;
        }


}
