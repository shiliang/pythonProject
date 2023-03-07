package com.chainmaker.jobservice.core.optimizer.plans;

import com.chainmaker.jobservice.core.optimizer.model.FL.FateModel;
import com.chainmaker.jobservice.core.optimizer.model.FL.FlInputData;
import com.chainmaker.jobservice.core.optimizer.model.InputData;
import com.chainmaker.jobservice.core.optimizer.model.OutputData;
import com.chainmaker.jobservice.core.optimizer.model.TeeModel;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-06 11:24
 * @description:Fate执行的任务
 * @version: 1.0.0
 */

public class Fate extends PhysicalPlan {
    private Integer id;
    private FateModel fateModel;
    private String nodeName = "fate-fl";
    private List<InputData> inputDataList;
    private List<OutputData> outputDataList;
    private boolean finalResult;
    private List<String> parties;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public FateModel getFateModel() {
        return fateModel;
    }

    public void setFateModel(FateModel fateModel) {
        this.fateModel = fateModel;
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    @Override
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public List<InputData> getInputDataList() {
        return inputDataList;
    }

    @Override
    public void setInputDataList(List<InputData> inputDataList) {
        this.inputDataList = inputDataList;
    }

    @Override
    public List<OutputData> getOutputDataList() {
        return outputDataList;
    }

    @Override
    public void setOutputDataList(List<OutputData> outputDataList) {
        this.outputDataList = outputDataList;
    }

    @Override
    public boolean isFinalResult() {
        return finalResult;
    }

    @Override
    public void setFinalResult(boolean finalResult) {
        this.finalResult = finalResult;
    }

    @Override
    public List<String> getParties() {
        return parties;
    }

    @Override
    public void setParties(List<String> parties) {
        this.parties = parties;
    }

    @Override
    public String toString() {
        return "Fate{" +
                "id=" + id +
                ", fateModel=" + fateModel +
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
