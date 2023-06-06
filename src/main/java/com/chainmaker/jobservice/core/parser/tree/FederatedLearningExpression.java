package com.chainmaker.jobservice.core.parser.tree;

import java.util.ArrayList;
import java.util.List;

public class FederatedLearningExpression extends Expression {
    private final List<FlExpression> fl;
    private final List<List<FlExpression>> labels;
    private final List<FlExpression>  psi;
    private final List<FlExpression> feat;
    private final List<FlExpression> model;
    private final List<FlExpression> eval;

    public FederatedLearningExpression(List<FlExpression> fl, List<List<FlExpression>> labels, List<FlExpression> psi, List<FlExpression> feat, List<FlExpression> model, List<FlExpression> eval) {
        this.fl = fl;
        this.labels = labels;
        this.psi = psi;
        this.feat = feat;
        this.model = model;
        this.eval = eval;
    }

    public List<FlExpression> getFl() {
        return fl;
    }

    public List<List<FlExpression>> getLabels() {
        return labels;
    }

    public List<FlExpression> getPsi() {
        return psi;
    }

    public List<FlExpression> getFeat() {
        return feat;
    }

    public List<FlExpression> getModel() {
        return model;
    }

    public List<FlExpression> getEval() {
        return eval;
    }

    @Override
    public List<? extends Node> getChildren() {
        return null;
    }

    @Override
    public String toString() {
        return "FederatedLearningExpression{" +
                "fl=" + fl +
                ", labels=" + labels +
                ", psi=" + psi +
                ", feat=" + feat +
                ", model=" + model +
                ", eval=" + eval +
                '}';
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
