package com.chainmaker.jobservice.core.optimizer.plans;

import com.chainmaker.jobservice.core.optimizer.model.InputData;
import com.chainmaker.jobservice.core.optimizer.model.OutputData;
import com.chainmaker.jobservice.core.optimizer.model.TeeModel;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-21 18:20
 * @description:TEE多方计算
 * @version: 1.0.0
 */

public class TeeMpc extends PhysicalPlan {
    private Integer id;
    private TeeModel teeModel;
    private String nodeName = "mpc-tee";
    private List<InputData> inputDataList;
    private List<OutputData> outputDataList;
    private boolean finalResult;
    private List<String> parties;

    public List<String> getParties() {
        return parties;
    }

    public void setParties(List<String> parties) {
        this.parties = parties;
    }
    public boolean isFinalResult() {
        return finalResult;
    }

    public void setFinalResult(boolean finalResult) {
        this.finalResult = finalResult;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TeeModel getTeeModel() {
        return teeModel;
    }

    public void setTeeModel(TeeModel teeModel) {
        this.teeModel = teeModel;
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
        return "TeeMpc{" +
                "id=" + id +
                ", teeModel=" + teeModel +
                ", nodeName='" + nodeName + '\'' +
                ", inputDataList=" + inputDataList +
                ", outputDataList=" + outputDataList +
                ", finalResult=" + finalResult +
                ", parties=" + parties +
                '}';
    }

    @Override
    public void accept(PhysicalPlanVisitor visitor) {
        visitor.visit(this);
    }
}
