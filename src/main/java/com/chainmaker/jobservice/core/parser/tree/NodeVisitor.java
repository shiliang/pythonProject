package com.chainmaker.jobservice.core.parser.tree;

public class NodeVisitor<T> {
    public T visit(Node node) {
        return visit(node);
    }

    public T visit(Expression node) {
        return visit(node);
    }

    public T visit(FlExpression node) { return visit(node); }

    public T visit(FederatedLearningExpression node) { return visit(node); }

    public T visit(ArithmeticBinaryExpression node) {
        return visit(node);
    }

    public T visit(ArithmeticUnaryExpression node) {
        return visit(node);
    }

    public T visit(ComparisonExpression node) {
        return visit(node);
    }

    public T visit(DereferenceExpression node) {
        return visit(node);
    }

    public T visit(Identifier node) {
        return visit(node);
    }

    public T visit(LogicalExpression node) {
        return visit(node);
    }

    public T visit(NotExpression node) {
        return visit(node);
    }

    public T visit(SubQueryExpression node) {
        return visit(node);
    }

    public T visit(ConstantExpression node) {
        return visit(node);
    }

    public T visit(ParenthesizedExpression node) {return visit(node);}

    public T visit(FunctionCallExpression node) {return visit(node);}
    public T visit(HintExpression node) {
        return visit(node);
    }

}
