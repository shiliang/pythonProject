package com.chainmaker.jobservice.api.model.bo.job.task;

import lombok.Data;

/**
 * @author gaokang
 * @date 2022-08-13 11:22
 * @description:任务输出详细信息
 * @version: 1.0.0
 */
@Data
public class TaskOutputData {
    private String dataName;
    private String finalResult = "N";
    private String domainID;
    private String dataID = "";
    private String columnName;
    private String type;
}
