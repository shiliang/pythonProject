package com.chainmaker.jobservice.core.optimizer.model;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-21 14:31
 * @description:SPDZ的输入
 * @version:
 */

public class InputData {
    private Integer nodeSrc;
    private String tableName;
    private String column;
    private String domainID;

    public String getDomainID() {
        return domainID;
    }

    public void setDomainID(String domainID) {
        this.domainID = domainID;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Integer getNodeSrc() {
        return nodeSrc;
    }

    public void setNodeSrc(Integer nodeSrc) {
        this.nodeSrc = nodeSrc;
    }

    @Override
    public String toString() {
        return "InputData{" +
                "nodeSrc=" + nodeSrc +
                ", tableName='" + tableName + '\'' +
                ", column='" + column + '\'' +
                ", domainID='" + domainID + '\'' +
                '}';
    }
}
