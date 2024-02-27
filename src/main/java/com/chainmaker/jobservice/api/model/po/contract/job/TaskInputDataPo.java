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

    private String assetEnName;

    private String dataID;
    private String domainID;
    private String role;
    private String params;


    @Override
    public String toString() {
        return "TaskInputDataPo{" +
                "dataName='" + dataName + '\'' +
                ", taskSrc='" + taskSrc + '\'' +
                ", assetEnName='" + assetEnName + '\'' +
                ", dataID='" + dataID + '\'' +
                ", domainID='" + domainID + '\'' +
                ", role='" + role + '\'' +
                ", params='" + params + '\'' +
                '}';
    }
}
