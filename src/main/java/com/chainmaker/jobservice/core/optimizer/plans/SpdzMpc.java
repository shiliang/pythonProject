package com.chainmaker.jobservice.core.optimizer.plans;

import com.chainmaker.jobservice.core.optimizer.model.InputData;
import com.chainmaker.jobservice.core.optimizer.model.OutputData;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-21 13:50
 * @description:使用SPDZ协议的计算节点
 * @version: 1.0.0
 */

public class SpdzMpc extends PhysicalPlan {
    private String aggregateType = "BASE";
    private List<String> expression;
    private Integer id;
    private String nodeName = "mpc-spdz";
    private List<InputData> inputDataList;
    private List<OutputData> outputDataList;
    private boolean finalResult;
    private List<String> parties;


    public boolean isFinalResult() {
        return finalResult;
    }

    public void setFinalResult(boolean finalResult) {
        this.finalResult = finalResult;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public List<String> getExpression() {
        return expression;
    }

    public void setExpression(List<String> expression) {
        this.expression = expression;
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

    @Override
    public String toString() {
        return "SpdzMpc{" +
                "aggregateType='" + aggregateType + '\'' +
                ", expression=" + expression +
                ", id=" + id +
                ", nodeName='" + nodeName + '\'' +
                ", inputDataList=" + inputDataList +
                ", outputDataList=" + outputDataList +
                ", finalResult=" + finalResult +
                '}';
    }

    @Override
    public void accept(PhysicalPlanVisitor visitor) {
        visitor.visit(this);
    }
}
