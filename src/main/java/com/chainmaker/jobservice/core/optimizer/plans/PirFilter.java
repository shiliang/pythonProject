package com.chainmaker.jobservice.core.optimizer.plans;

import com.chainmaker.jobservice.core.optimizer.model.InputData;
import com.chainmaker.jobservice.core.optimizer.model.OutputData;

import java.util.List;

/**
 * @author gaokang
 * @date 2023-06-07 18:41
 * @description:
 * @version:
 */
public class PirFilter extends PhysicalPlan {
    private Integer id;
    private String condition;
    private String project;
    private String nodeName = "filter";
    private List<InputData> inputDataList;
    private List<OutputData> outputDataList;
    private boolean finalResult;
    private List<String> parties;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
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
        return "PirFilter{" +
                "id=" + id +
                ", condition='" + condition + '\'' +
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
