package com.chainmaker.jobservice.core.parser.plans;

import com.chainmaker.jobservice.core.parser.tree.Expression;
import com.chainmaker.jobservice.core.parser.tree.HintExpression;
import com.chainmaker.jobservice.core.parser.tree.Identifier;
import org.apache.calcite.rel.RelNode;

import java.util.List;

/**
 * @author gaokang
 * @date 2023-04-01 17:46
 * @description:
 * @version:
 */
public class LogicalHint extends LogicalPlan {
    private final List<HintExpression> values;
    private final List<LogicalPlan> children;

    public LogicalHint(List<HintExpression> values, List<LogicalPlan> children) {
        this.values = values;
        this.children = children;
    }


    public List<HintExpression> getValues() {
        return values;
    }

    @Override
    public List<LogicalPlan> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "Hint" + "[" + values.toString() + "]";
    }


    @Override
    public void accept(LogicalPlanVisitor visitor) {
        visitor.visit(this);
    }

    public RelNode accept(LogicalPlanRelVisitor visitor) {
        return visitor.visit(this);
    }
}
