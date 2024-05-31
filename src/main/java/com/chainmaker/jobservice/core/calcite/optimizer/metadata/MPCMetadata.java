package com.chainmaker.jobservice.core.calcite.optimizer.metadata;

import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.catalina.session.TooManyActiveSessionsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 单例类，全局共享一个MPCMetadata
 */
@Slf4j
public class MPCMetadata {
    private HashMap<String, TableInfo> tables;

    private HashMap<String, String> markIDFieldNameMap;

    private HashMap<String, FieldInfo> fieldNameInfoMap;


    private MPCMetadata() {
        tables = null;
        markIDFieldNameMap = new HashMap<>();
        fieldNameInfoMap = new HashMap<>();
    }

    private static MPCMetadata instance = new MPCMetadata();


    @Deprecated
    /**
     * 根据属性名获取已经标记的markId
     * @param fieldName
     * @return
     */
    public String getMarkId(String fieldName) {
        for (String markId : markIDFieldNameMap.keySet()) {
            if (markIDFieldNameMap.get(markId) == fieldName) {
                return markId;
            }
        }
        return null;
    }

    @Deprecated
    /**
     * 根据MarkID获取属性信息（类似$0, $1等）
     * @param markID
     * @return
     */
    public FieldInfo getFieldInfo(RexInputRef markID) {
        return getFieldInfo(markIDFieldNameMap.get(markID.toString()));
    }



    /**
     * 根据名属性获取相关属性信息
     * @param fieldName
     * @return
     */
    public FieldInfo getFieldInfo(String fieldName) {
        return fieldNameInfoMap.get(fieldName);
    }


    /**
     * 根据表名获取行数
     * @param tableName
     * @return
     */
    public double getTableRowCount(String tableName) {
        return tables.get(tableName).getRowCount();
    }

    /**
     * 根据表名获取表所属数据源
     * @param tableName
     * @return
     */
    public TableInfo getTable(String tableName) { return tables.get(tableName); }

    public String getTableOrgId(String tableName) {return getTable(tableName).getOrgDId();}
    /**
     * 初始化整个查询优化过程所需的metadata
     * @param sql 表示从sql读取，非真实数据来源
     * @param metadata 真实数据来源
     */
    public void init(String sql, HashMap<String, TableInfo> metadata) {
        if (metadata == null) {
            /**
             * 如果传入参数tables为空，则表示无元数据，直接从sql语句中读取（即会识别X.Y格式为表名和属性名）
             * 此情况默认所有表的行数为100，属性均为Integer类型，且在1-100内均匀分布
             * 主要用于测试，正常使用应该不会出现此种情况
             */
            String[] strs = sql.split(" |=|\\(|\\)|>|<|\\+|-|\\*|/|!=|,");
            HashMap<String, List<String>> sqlTableFieldMap = new HashMap<>();
            for (String s : strs) {
                if (s.contains(".")) {
                    String[] tmp = s.split("\\.");
                    String tableName = tmp[0], fieldName = tmp[1];
                    if (sqlTableFieldMap.containsKey(tableName)) {
                        if (sqlTableFieldMap.get(tableName).contains(fieldName)) {
                            continue;
                        } else {
                            sqlTableFieldMap.get(tableName).add(fieldName);
                        }
                    } else {
                        List<String> list = new ArrayList<String>();
                        list.add(fieldName);
                        sqlTableFieldMap.put(tableName, list);
                    }
                }
            }

            HashMap<String, TableInfo> tableInfos = new HashMap<>();
            int cnt = 1;
            for (String key : sqlTableFieldMap.keySet()) {
                HashMap<String, FieldInfo> fieldInfos = new HashMap<>();
                for (String fieldName : sqlTableFieldMap.get(key)) {
                    String totalName = key + "." + fieldName;
                    FieldInfo field = new FieldInfo(totalName, "Integer", 100*cnt, 1, FieldInfo.DistributionType.Uniform, key, 8, "domain1", "domainName", "", "t1", key);
                    fieldInfos.put(totalName, field);
                    fieldNameInfoMap.put(totalName, field);
                }
                double rowCount = 100*cnt;
//                cnt++;
                tableInfos.put(key, new TableInfo(fieldInfos, rowCount, key, "source1",  "1","assetName"));
            }
            setTables(tableInfos);

        } else {
            setTables(metadata);
            for (String tableName : metadata.keySet()) {
                for (FieldInfo field : metadata.get(tableName).getFields().values()) {
                    fieldNameInfoMap.put(field.getUniqueName(), field);
                }
            }
//            for (String s : fieldNameInfoMap.keySet()) {
//                System.out.println(s);
//            }

            /** 由于真实数据需要长安链内网或者VPN，以下是用于测试的模拟数据
             * ****************************************
                表名      属性      行数      来源
                TA      ID、TA     300      source1
                TB      ID、TB     200      source2
                TC      ID、TC     100      source1
             * ****************************************
             */
//            List<String> tableNames = Arrays.asList("TA", "TB", "TC");
//            HashMap<String, TableInfo> tableInfos = new HashMap();
//            int cnt = 3;
//            for (String tableName : tableNames) {
//                HashMap<String, FieldInfo> fieldInfos = new HashMap();
//                String fieldName1 = tableName + ".ID";
//                String fieldName2 = tableName + "." + tableName;
//                FieldInfo field1 = new FieldInfo(fieldName1, 2, 100*cnt, 1, FieldInfo.DistributionType.Uniform, tableName, 8);
//                fieldInfos.put(fieldName1, field1);
//                fieldNameInfoMap.put(fieldName1, field1);
//                FieldInfo field2 = new FieldInfo(fieldName2, 4, null, null, FieldInfo.DistributionType.Uniform, tableName, 8);
//                fieldInfos.put(fieldName2, field2);
//                fieldNameInfoMap.put(fieldName2, field2);
//                double rowCount = 100 * cnt;
//                cnt--;
//                tableInfos.put(tableName, new TableInfo(fieldInfos, rowCount, tableName, "source1"));
//            }
//            setTables(tableInfos);
//            this.tables.get("TB").setOrgDId("source2");
//            this.tables.get("TC").setRowCount(100);
        }
        log.info("fieldNameInfoMap:" + fieldNameInfoMap);
    }

    /**
     * 获取所有 Tables--Fields 的映射表
     * @return
     */
    public HashMap<String, HashMap<String, SqlTypeName>> getTableFieldMap() {
        HashMap<String, HashMap<String, SqlTypeName>> tableFieldMap = new HashMap<>();
        for (String key : tables.keySet()) {
            HashMap<String, SqlTypeName> fields = new HashMap<>();
            for (String fieldName : tables.get(key).getFields().keySet()) {
                fields.put(fieldName, tables.get(key).getFields().get(fieldName).getFieldType());
            }
            tableFieldMap.put(key, fields);
        }
        return tableFieldMap;
    }

    public static MPCMetadata getInstance() {
        return instance;
    }

    public static void setInstance(MPCMetadata instance) {
        MPCMetadata.instance = instance;
    }


    public HashMap<String, TableInfo> getTables() {
        return tables;
    }

    public void setTables(HashMap<String, TableInfo> tables) {
        this.tables = tables;
    }

}
