package com.chainmaker.jobservice.core.optimizer.model;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-23 09:52
 * @description:spdz任务输入
 * @version: 1.0.0
 */

public class SpdzInputData extends InputData {
    private Integer nodeSrc;
    private String tableName;
    private String column;
    private List<Integer> index;
    private String domainID;

    public Integer getNodeSrc() {
        return nodeSrc;
    }

    public void setNodeSrc(Integer nodeSrc) {
        this.nodeSrc = nodeSrc;
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

    public List<Integer> getIndex() {
        return index;
    }

    public void setIndex(List<Integer> index) {
        this.index = index;
    }

    public String getDomainID() {
        return domainID;
    }

    public void setDomainID(String domainID) {
        this.domainID = domainID;
    }

    @Override
    public String toString() {
        return "SpdzInputData{" +
                "nodeSrc=" + nodeSrc +
                ", tableName='" + tableName + '\'' +
                ", column='" + column + '\'' +
                ", index=" + index +
                ", domainID='" + domainID + '\'' +
                '}';
    }
}
