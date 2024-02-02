package com.chainmaker.jobservice.api.model;

import lombok.Data;

import java.util.List;

/**
 * @author zhangwei
 * 数据资产的数据来源信息
 */
@Data
public class DataInfo {
    /**
     * 数据库名称
     */
    private String dbName;
    /**
     * 数据表名称
     */
    private String tableName;
    /**
     * 字段信息
     */
    private List<SaveTableColumnItem> itemList;
}
