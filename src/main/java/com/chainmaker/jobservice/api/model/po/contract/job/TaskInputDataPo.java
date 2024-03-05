package com.chainmaker.jobservice.api.model.po.contract.job;

import lombok.Data;

/**
 * @author gaokang
 * @date 2022-09-19 19:27
 * @description:
 * @version:
 */
@Data
public class TaskInputDataPo {
    private String dataName;
    private String taskSrc;
    private String dataID;
    private String domainID;
    private String role;
    private String params;
    private String assetName;
    private String databaseName;
    private String tableName;
    private String columnName;
    private String type;
    private String length;
    private String comments;
}
