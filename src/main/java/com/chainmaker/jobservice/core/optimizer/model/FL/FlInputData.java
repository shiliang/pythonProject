package com.chainmaker.jobservice.core.optimizer.model.FL;

import com.chainmaker.jobservice.core.optimizer.model.InputData;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-06 14:20
 * @description:
 * @version:
 */

public class FlInputData extends InputData {
    private Integer nodeSrc;
    private String tableName;
    private String column;
    private String domainID;
    private String role;
    private String label_type;
    private String output_format;
    private String with_label;
    private String namespace;

    public Integer getNodeSrc() {
        return nodeSrc;
    }

    public void setNodeSrc(Integer nodeSrc) {
        this.nodeSrc = nodeSrc;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLabel_type() {
        return label_type;
    }

    public void setLabel_type(String label_type) {
        this.label_type = label_type;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getColumn() {
        return column;
    }

    @Override
    public void setColumn(String column) {
        this.column = column;
    }

    public String getOutput_format() {
        return output_format;
    }

    public void setOutput_format(String output_format) {
        this.output_format = output_format;
    }

    public String getWith_label() {
        return with_label;
    }

    public void setWith_label(String with_label) {
        this.with_label = with_label;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDomainID() {
        return domainID;
    }

    public void setDomainID(String domainID) {
        this.domainID = domainID;
    }

    @Override
    public String toString() {
        return "FlInputData{" +
                "nodeSrc=" + nodeSrc +
                ", tableName='" + tableName + '\'' +
                ", column='" + column + '\'' +
                ", domainID='" + domainID + '\'' +
                ", role='" + role + '\'' +
                ", label_type='" + label_type + '\'' +
                ", output_format='" + output_format + '\'' +
                ", with_label='" + with_label + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
