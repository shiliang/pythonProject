package com.chainmaker.jobservice.core.optimizer.plans;

import com.chainmaker.jobservice.core.optimizer.model.InputData;
import com.chainmaker.jobservice.core.optimizer.model.OutputData;
import lombok.Data;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-21 10:27
 * @description:物理表数据源
 * @version: 1.0.0
 */
@Data
public class TableScan extends PhysicalPlan {
    private Integer id;
    private String nodeName = "reader";
    private List<InputData> inputDataList;
    private List<OutputData> outputDataList;
    private String tableName;
    private String domainID;
    private String domainName;
    private boolean finalResult;
    public boolean isFinalResult() {
        return finalResult;
    }

    public void setFinalResult(boolean finalResult) {
        this.finalResult = finalResult;
    }
    public String getDomainID() {
        return domainID;
    }

    public void setDomainID(String domainID) {
        this.domainID = domainID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public List<InputData> getInputDataList() {
        return inputDataList;
    }

    public void setInputDataList(List<InputData> inputDataList) {
        this.inputDataList = inputDataList;
    }

    public List<OutputData> getOutputDataList() {
        return outputDataList;
    }

    public void setOutputDataList(List<OutputData> outputDataList) {
        this.outputDataList = outputDataList;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return "TableScan{" +
                "id=" + id +
                ", nodeName='" + nodeName + '\'' +
                ", inputDataList=" + inputDataList +
                ", outputDataList=" + outputDataList +
                ", tableName='" + tableName + '\'' +
                ", domainID='" + domainID + '\'' +
                ", finalResult=" + finalResult +
                '}';
    }

    @Override
    public void accept(PhysicalPlanVisitor visitor) {
        visitor.visit(this);
    }
}
