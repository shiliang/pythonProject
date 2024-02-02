package com.chainmaker.jobservice.api.model;

import lombok.Data;

/**
 * 数据库的表的信息
 */
@Data
public class SaveTableColumnItem {

    /**
     * 字段名称
     */
    private String name;
    /**
     * 字段类型
     */
    private String dataType;
    /**
     * 字段长度
     */
    private Integer dataLength;
    /**
     * 字段描述
     */
    private String description;
    /**
     * 是否为主键：1.是，0.否
     */
    private Integer isPrimaryKey;
    /**
     * 是否隐私查询：1是，0否。 默认1
     */
    private Integer privacyQuery;

}
