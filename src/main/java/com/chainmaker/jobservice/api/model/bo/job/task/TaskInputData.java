package com.chainmaker.jobservice.api.model.bo.job.task;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author gaokang
 * @date 2022-08-13 11:01
 * @description:任务输入详细信息
 * @version: 1.0.0
 */
@Data
public class TaskInputData {
    private String dataName;
    private String taskSrc = "";
    private String dataID = "";
    private String domainName;
    private String domainID;
    private String databaseName;
    private String tableName;
    private String columnName;
    private String type;
    private Integer length;
    private String comments;
    private String role = "client";
    private JSONObject params;
    private String assetName;
}
