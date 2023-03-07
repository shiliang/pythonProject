package com.chainmaker.jobservice.api.model.bo.job.task;

/**
 * @author gaokang
 * @date 2022-08-13 11:22
 * @description:任务输出详细信息
 * @version: 1.0.0
 */

public class TaskOutputData {
    private String dataName;
    private String finalResult = "N";
    private String domainID;
    private String dataID = "";

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(String finalResult) {
        this.finalResult = finalResult;
    }

    public String getDomainID() {
        return domainID;
    }

    public void setDomainID(String domainID) {
        this.domainID = domainID;
    }

    public String getDataID() {
        return dataID;
    }

    public void setDataID(String dataID) {
        this.dataID = dataID;
    }

    @Override
    public String toString() {
        return "TaskOutputData{" +
                "dataName='" + dataName + '\'' +
                ", finalResult='" + finalResult + '\'' +
                ", domainID='" + domainID + '\'' +
                ", dataID='" + dataID + '\'' +
                '}';
    }
}
