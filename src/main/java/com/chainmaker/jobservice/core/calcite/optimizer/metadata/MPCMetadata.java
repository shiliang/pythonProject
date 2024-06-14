package com.chainmaker.jobservice.core.calcite.optimizer.metadata;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 单例类，全局共享一个MPCMetadata
 */

@Data
@Slf4j
public class MPCMetadata {
    private HashMap<String, TableInfo> tableInfoMap = Maps.newHashMap();

    private HashMap<String, List<FieldInfo>> tableFieldsMap = Maps.newHashMap();

    private HashMap<String, FieldInfo> fieldNameInfoMap = Maps.newHashMap();

    //    private HashMap<String, String> markIDFieldNameMap;
    private MPCMetadata() {

    }

    @Getter
    private static MPCMetadata instance = new MPCMetadata();


//    @Deprecated
//    /**
//     * 根据属性名获取已经标记的markId
//     * @param fieldName
//     * @return
//     */
//    public String getMarkId(String fieldName) {
//        for (String markId : markIDFieldNameMap.keySet()) {
//            if (markIDFieldNameMap.get(markId) == fieldName) {
//                return markId;
//            }
//        }
//        return null;
//    }

//    @Deprecated
//    /**
//     * 根据MarkID获取属性信息（类似$0, $1等）
//     * @param markID
//     * @return
//     */
//    public FieldInfo getFieldInfo(RexInputRef markID) {
//        return getFieldInfo(markIDFieldNameMap.get(markID.toString()));
//    }



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
        return tableInfoMap.get(tableName).getRowCount();
    }

    /**
     * 根据表名获取表所属数据源
     * @param tableName
     * @return
     */
    public TableInfo getTable(String tableName) { return tableInfoMap.get(tableName); }

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
            setTableInfoMap(tableInfos);

        } else {
            setTableInfoMap(metadata);
            for (String tableName : metadata.keySet()) {
                List<FieldInfo> fields = new ArrayList<>();
                for (FieldInfo field : metadata.get(tableName).getFields().values()) {
                    fieldNameInfoMap.put(field.getUniqueName(), field);
                    fields.add(field);
                }
                tableFieldsMap.put(tableName, fields);
            }
        }

        log.info("MPCMetadata init..., tableInfoMap:" + tableInfoMap);
        log.info("MPCMetadata init..., tableInfoMap:" + tableFieldsMap);
        log.info("MPCMetadata init..., fieldNameInfoMap:" + fieldNameInfoMap);
    }

    /**
     * 获取所有 Tables--Fields 的映射表
     * @return
     */
    public HashMap<String, HashMap<String, SqlTypeName>> getTableFieldMap() {
        HashMap<String, HashMap<String, SqlTypeName>> tableFieldMap = new HashMap<>();
        for (String key : tableInfoMap.keySet()) {
            HashMap<String, SqlTypeName> fields = new HashMap<>();
            for (String fieldName : tableInfoMap.get(key).getFields().keySet()) {
                fields.put(fieldName, tableInfoMap.get(key).getFields().get(fieldName).getFieldType());
            }
            tableFieldMap.put(key, fields);
        }
        return tableFieldMap;
    }

    public static void setInstance(MPCMetadata instance) {
        MPCMetadata.instance = instance;
    }


}
