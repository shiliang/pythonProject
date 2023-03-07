package com.chainmaker.jobservice.core.optimizer.model;

/**
 * @author gaokang
 * @date 2022-08-22 20:14
 * @description:计算结果输出
 * @version: 1.0.0
 */

public class OutputData {
    private String tableName;
    private String domainID;
    private String outputSymbol;

    public String getOutputSymbol() {
        return outputSymbol;
    }

    public void setOutputSymbol(String outputSymbol) {
        this.outputSymbol = outputSymbol;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDomainID() {
        return domainID;
    }

    public void setDomainID(String domainID) {
        this.domainID = domainID;
    }

    @Override
    public String toString() {
        return "OutputData{" +
                "tableName='" + tableName + '\'' +
                ", domainID='" + domainID + '\'' +
                ", outputSymbol='" + outputSymbol + '\'' +
                '}';
    }
}
