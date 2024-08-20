package com.chainmaker.jobservice.core.optimizer.plans;

import com.chainmaker.jobservice.core.optimizer.model.InputData;
import com.chainmaker.jobservice.core.optimizer.model.OutputData;
import com.chainmaker.jobservice.core.parser.tree.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.List;

/**
 * @author gaokang
 * @date 2023-06-07 18:41
 * @description:
 * @version:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PirFilter extends PhysicalPlan {

    private Integer id;

    private Expression condition;

    private HashMap<String, List<String>> project;

    private String nodeName = "filter";

    private List<InputData> inputDataList;

    private List<OutputData> outputDataList;

    private boolean finalResult;

    private List<String> parties;

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
