package com.chainmaker.jobservice.core.optimizer.plans;

import com.chainmaker.jobservice.core.optimizer.model.InputData;
import com.chainmaker.jobservice.core.optimizer.model.OutputData;
import lombok.Data;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-21 13:50
 * @description:使用SPDZ协议的计算节点
 * @version: 1.0.0
 */

@Data
public class SpdzMpc extends PhysicalPlan {

    private String aggregateType;

    private String expression;

    private String constants;

    private String variables;

    private Integer id;

    private String nodeName = "mpc-spdz";

    private List<InputData> inputDataList;

    private List<OutputData> outputDataList;

    private boolean finalResult;

    private List<String> parties;


    public boolean isFinalResult() {
        return finalResult;
    }

    @Override
    public String toString() {
        return "SpdzMpc{" +
//                "aggregateType='" + aggregateType + '\'' +
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
