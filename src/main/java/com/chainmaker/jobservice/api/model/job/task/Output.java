package com.chainmaker.jobservice.api.model.job.task;

import lombok.Data;

/**
 * @author gaokang
 * @date 2022-08-13 11:22
 * @description:任务输出详细信息
 * @version: 1.0.0
 */
@Data
public class Output {
    private String dataName;
    private String finalResult = "N";
    private Boolean isFinalResult = false;
    private String domainId;
    private String domainName;
    private String dataId = "";
    private String columnName;
    private String type;
    private Integer length;
    private String pubKey = "";
}
