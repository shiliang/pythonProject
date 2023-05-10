package com.chainmaker.jobservice.core.parser;

import com.chainmaker.jobservice.api.response.ParserException;
import com.chainmaker.jobservice.core.antlr4.gen.SqlBaseLexer;
import com.chainmaker.jobservice.core.antlr4.gen.SqlBaseParser;
import com.chainmaker.jobservice.core.antlr4.gen.SqlBaseParserBaseVisitor;
import com.chainmaker.jobservice.core.parser.plans.*;
import com.chainmaker.jobservice.core.parser.tree.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author gaokang
 * @date 2023-04-01 15:06
 * @description:
 * @version:
 */
public class LogicalPlanBuilderV2 extends SqlBaseParserBaseVisitor {
    private HashMap<String, String> tableNameMap = new HashMap<>();
    public List<String> modelNameList = new ArrayList<>();
    private static final String DEFAULT_ERROR = "SQL语法暂不支持";
    private LogicalPlan logicalPlan;

    public HashMap<String, String> getTableNameMap() {
        return tableNameMap;
    }

    public List<String> getModelNameList() {
        return modelNameList;
    }

    public LogicalPlan getLogicalPlan() {
        return logicalPlan;
    }

    public LogicalPlanBuilderV2(String sqlText) {
        SqlBaseLexer lexer = new SqlBaseLexer(new ANTLRInputStream(sqlText));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SqlBaseParser parser = new SqlBaseParser(tokens);
        parser.removeErrorListeners();
        ErrorListener customErrorListener = new ErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(customErrorListener);
        this.logicalPlan = this.visitSingleStatement(parser.singleStatement());
    }

    @Override
    public LogicalPlan visitSingleStatement(SqlBaseParser.SingleStatementContext context) {
        if (context.statement() instanceof SqlBaseParser.StatementDefaultContext) {
            return visitStatementDefault((SqlBaseParser.StatementDefaultContext) context.statement());
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.statement().getChild(0).getText());
        }
    }

    @Override
    public LogicalPlan visitStatementDefault(SqlBaseParser.StatementDefaultContext context) {
        return visitQuery(context.query());
    }

    @Override
    public LogicalPlan visitQuery(SqlBaseParser.QueryContext context) {
        if (context.queryTerm() instanceof SqlBaseParser.QueryTermDefaultContext) {
            return visitQueryTermDefault((SqlBaseParser.QueryTermDefaultContext) context.queryTerm());
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.queryTerm().getChild(0).getText());
        }
    }

    @Override
    public LogicalPlan visitQueryTermDefault(SqlBaseParser.QueryTermDefaultContext context) {
        if (context.queryPrimary() instanceof SqlBaseParser.QueryPrimaryDefaultContext) {
            return visitQueryPrimaryDefault((SqlBaseParser.QueryPrimaryDefaultContext)context.queryPrimary());
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.queryPrimary().getText());
        }
    }
    @Override
    public LogicalPlan visitQueryPrimaryDefault(SqlBaseParser.QueryPrimaryDefaultContext context) {
        if (context.querySpecification() instanceof SqlBaseParser.RegularQuerySpecificationContext) {
            if (((SqlBaseParser.RegularQuerySpecificationContext) context.querySpecification()).selectClause().hints.size() == 0) {
                return visitRegularQuerySpecification((SqlBaseParser.RegularQuerySpecificationContext) context.querySpecification());
            } else {
                LogicalPlan child = visitRegularQuerySpecification((SqlBaseParser.RegularQuerySpecificationContext) context.querySpecification());
                List<HintExpression> hintExpressions = new ArrayList<>();
                for (SqlBaseParser.HintStatementContext hintStatementContext: ((SqlBaseParser.RegularQuerySpecificationContext) context.querySpecification()).selectClause().hint(0).hintStatement()) {
                    List<String> values = new ArrayList<>();
                    String key = hintStatementContext.hintName.getText();
                    for (SqlBaseParser.PrimaryExpressionContext primaryExpressionContext: hintStatementContext.parameters) {
                        values.add(primaryExpressionContext.getText());
                    }
                    HintExpression hintExpression = new HintExpression(key, values);
                    hintExpressions.add(hintExpression);
                };
                List<LogicalPlan> children = new ArrayList<>();
                children.add(child);
                return new LogicalHint(hintExpressions, children);
            }

        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.querySpecification().getText());
        }
    }

    @Override
    public LogicalPlan visitRegularQuerySpecification(SqlBaseParser.RegularQuerySpecificationContext context) {
//        System.out.println(context.selectClause().hint.hintStatement(0).getText());
        List<LogicalPlan> children = new ArrayList<>();

//        if (context.fromClause() == null) {
//            throw new ParserException(DEFAULT_ERROR + ": " + "缺少From");
//        } else if (context.whereClause() == null) {
//            List<LogicalPlan> fromList = visitFrom(context.fromClause());
//            children.addAll(fromList);
//        } else {
//            children.add(visitWhere(context));
//        }

        // add aggregate
        if (context.fromClause() == null) {
            throw new ParserException(DEFAULT_ERROR + ": " + "缺少From");
        }

        if (context.aggregationClause() == null) {
            if (context.whereClause() == null) {
                List<LogicalPlan> fromList = visitFrom(context.fromClause());
                children.addAll(fromList);
            } else {
                children.add(visitWhere(context));
            }
        } else if (context.havingClause() == null) {
            children.add(visitAggregate(context, null));
        } else {
            children.add(visitHaving(context));
        }

        if (context.selectClause().namedExpressionSeq() instanceof SqlBaseParser.FederatedLearningExpressionContext) {
            FederatedLearningExpression expression = visitFederatedLearningExpression((SqlBaseParser.FederatedLearningExpressionContext) context.selectClause().namedExpressionSeq());
            return new FederatedLearning(expression, children);
        } else {
            FaderatedQueryExpression projectList = visitFederatedQueryExpression((SqlBaseParser.FederatedQueryExpressionContext) context.selectClause().namedExpressionSeq());
            return new LogicalProject(projectList, children);
        }
    }

    private LogicalPlan visitHaving(SqlBaseParser.RegularQuerySpecificationContext context) {
        Expression aggCalls = visitBooleanExpression(context.havingClause().booleanExpression());
        return visitAggregate(context, aggCalls);
    }

    public LogicalPlan visitAggregate(SqlBaseParser.RegularQuerySpecificationContext context, Expression aggCalls) {
        List<LogicalPlan> children = new ArrayList<>();
        if (context.whereClause() == null) {
            List<LogicalPlan> fromList = visitFrom(context.fromClause());
            children.addAll(fromList);
        } else {
            children.add(visitWhere(context));
        }

        List<Expression> groupKeys = new ArrayList<>();
        System.out.println(context.aggregationClause().groupByClause());
        for (SqlBaseParser.GroupByClauseContext ctx : context.aggregationClause().groupByClause()) {
            groupKeys.add(visitExpression(ctx.expression()));
        }
        return new LogicalAggregate(groupKeys, aggCalls, children);
    }

    public LogicalPlan visitWhere(SqlBaseParser.RegularQuerySpecificationContext context) {
        Expression expression = visitBooleanExpression(context.whereClause().booleanExpression());
        List<LogicalPlan> children = visitFrom(context.fromClause());
        return new LogicalFilter(expression, children);
    }

    public Expression visitBooleanExpression(SqlBaseParser.BooleanExpressionContext context) {
        if (context instanceof SqlBaseParser.LogicalBinaryContext) {
            return visitLogicalBinary((SqlBaseParser.LogicalBinaryContext) context);
        } else if (context instanceof SqlBaseParser.LogicalNotContext) {
            return visitLogicalNot((SqlBaseParser.LogicalNotContext) context);
        } else if (context instanceof SqlBaseParser.PredicatedContext) {
            return visitPredicated((SqlBaseParser.PredicatedContext) context);
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
        }
    }

    @Override
    public LogicalExpression visitLogicalBinary(SqlBaseParser.LogicalBinaryContext context) {
        Expression left = visitBooleanExpression(context.left);
        LogicalExpression.Operator operator = getLogicalOperator(context.operator);
        Expression right = visitBooleanExpression(context.right);
        return new LogicalExpression(left, right, operator);
    }
    @Override
    public NotExpression visitLogicalNot(SqlBaseParser.LogicalNotContext context) {
        Expression expression = visitBooleanExpression(context.booleanExpression());
        return new NotExpression(expression);
    }

    @Override
    public Expression visitPredicated(SqlBaseParser.PredicatedContext context) {
        if (context.predicate() == null) {
            return visitValueExpression(context.valueExpression());
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
        }
    }
    public Expression visitValueExpression(SqlBaseParser.ValueExpressionContext context) {
        if (context instanceof SqlBaseParser.ValueExpressionDefaultContext) {
            return visitValueExpressionDefault((SqlBaseParser.ValueExpressionDefaultContext) context);
        } else if (context instanceof SqlBaseParser.ArithmeticBinaryContext) {
            return visitArithmeticBinary((SqlBaseParser.ArithmeticBinaryContext) context);
        } else if (context instanceof SqlBaseParser.ArithmeticUnaryContext) {
            return visitArithmeticUnary((SqlBaseParser.ArithmeticUnaryContext) context);
        } else if (context instanceof SqlBaseParser.ComparisonContext) {
            return visitComparison((SqlBaseParser.ComparisonContext) context);
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
        }
    }
    @Override
    public ArithmeticBinaryExpression visitArithmeticBinary(SqlBaseParser.ArithmeticBinaryContext context) {
        Expression left = visitValueExpression(context.left);
        ArithmeticBinaryExpression.Operator operator = getArithmeticBinaryOperator(context.operator);
        Expression right = visitValueExpression(context.right);
        return new ArithmeticBinaryExpression(left, right, operator);
    }
    @Override
    public ArithmeticUnaryExpression visitArithmeticUnary(SqlBaseParser.ArithmeticUnaryContext context) {
        ArithmeticUnaryExpression.Sign sign = getArithmeticUnaryOperator(context.operator);
        Expression expression = visitValueExpression(context.valueExpression());
        return new ArithmeticUnaryExpression(expression, sign);
    }
    @Override
    public ComparisonExpression visitComparison(SqlBaseParser.ComparisonContext context) {
        Expression left = visitValueExpression(context.left);
        ComparisonExpression.Operator operator = getComparisonOperator(((TerminalNode) context.comparisonOperator().getChild(0)).getSymbol());
        Expression right = visitValueExpression(context.right);
        return new ComparisonExpression(left, right, operator);
    }
    @Override
    public Expression visitValueExpressionDefault(SqlBaseParser.ValueExpressionDefaultContext context) {
        return visitPrimaryExpression(context.primaryExpression());
    }

    public Expression visitPrimaryExpression(SqlBaseParser.PrimaryExpressionContext context) {
        if (context instanceof SqlBaseParser.ColumnReferenceContext) {
            return visitColumnReference((SqlBaseParser.ColumnReferenceContext) context);
        } else if (context instanceof SqlBaseParser.DereferenceContext) {
            return visitDereference((SqlBaseParser.DereferenceContext) context);
        } else if (context instanceof SqlBaseParser.ConstantDefaultContext) {
            return visitConstantDefault((SqlBaseParser.ConstantDefaultContext) context);
        } else if (context instanceof SqlBaseParser.ParenthesizedExpressionContext) {
            return visitParenthesizedExpression((SqlBaseParser.ParenthesizedExpressionContext) context);
        } else if (context instanceof SqlBaseParser.FunctionCallContext) {
            return visitFunctionCall((SqlBaseParser.FunctionCallContext) context);
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
        }
    }
    @Override
    public Identifier visitColumnReference(SqlBaseParser.ColumnReferenceContext context) {
        String identifier = context.identifier().getChild(0).getText();
        return new Identifier(identifier);
    }
    @Override
    public DereferenceExpression visitDereference(SqlBaseParser.DereferenceContext context) {
        Expression base = visitPrimaryExpression(context.base);
        String fieldName = context.fieldName.getChild(0).getText();
        return new DereferenceExpression(base, fieldName);
    }
    @Override
    public ConstantExpression visitConstantDefault(SqlBaseParser.ConstantDefaultContext context) {
        String identifier = context.constant().getChild(0).getText();
        return new ConstantExpression(identifier);
    }
    @Override
    public ParenthesizedExpression visitParenthesizedExpression(SqlBaseParser.ParenthesizedExpressionContext context) {
        Expression expression = visitExpression(context.expression());
        return new ParenthesizedExpression(expression);
    }
    @Override
    public FunctionCallExpression visitFunctionCall(SqlBaseParser.FunctionCallContext context) {
        String function = context.functionName().getChild(0).getText();
        modelNameList.add(function);
        List<Expression> arrayExpression = new ArrayList<>();
        for (int i=0; i<context.expression().size(); i++) {
            Expression expression = visitExpression(context.expression(i));
            arrayExpression.add(expression);
        }
        return new FunctionCallExpression(function, arrayExpression);
    }

    @Override
    public FaderatedQueryExpression visitFederatedQueryExpression(SqlBaseParser.FederatedQueryExpressionContext context) {
        List<NamedExpression> namedExpressions = new ArrayList<>();
        for (int i=0; i<context.namedExpression().size(); i++) {
            NamedExpression namedExpression = visitNamedExpression(context.namedExpression(i));
            namedExpressions.add(namedExpression);
        }
        return new FaderatedQueryExpression(namedExpressions);
    }

    public List<LogicalPlan> visitFrom(SqlBaseParser.FromClauseContext context) {
        List<LogicalPlan> logicalPlans = new ArrayList<>();
        for (int i=0; i<context.relation().size(); i++) {
            logicalPlans.add(visitRelation(context.relation(i)));
        }
        return logicalPlans;
    }

    @Override
    public LogicalPlan visitRelation(SqlBaseParser.RelationContext context) {
        if (context.relationExtension().size() == 0) {
            return visitRelationPrimary(context.relationPrimary());
        } else {
            if (context.relationExtension(0).joinRelation().NATURAL() == null && context.relationExtension(0).joinRelation().joinCriteria() != null) {
                LogicalJoin.Type joinType;
                if (context.relationExtension(0).joinRelation().joinType().LEFT() != null) {
                    joinType = LogicalJoin.Type.LEFT;
                } else if (context.relationExtension(0).joinRelation().joinType().RIGHT() != null) {
                    joinType = LogicalJoin.Type.RIGHT;
                } else if (context.relationExtension(0).joinRelation().joinType().FULL() != null) {
                    joinType = LogicalJoin.Type.FULL;
                } else {
                    joinType = LogicalJoin.Type.INNER;
                }
                LogicalPlan left = visitRelationPrimary(context.relationPrimary());
                LogicalPlan right = visitRelationPrimary(context.relationExtension(0).joinRelation().relationPrimary());
                Expression expression = visitPredicated((SqlBaseParser.PredicatedContext) context.relationExtension(0).joinRelation().joinCriteria().booleanExpression());
                return new LogicalJoin(expression, joinType, left, right);
            } else {
                throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
            }
        }
    }


    public LogicalPlan visitRelationPrimary(SqlBaseParser.RelationPrimaryContext context) {
        if (context instanceof SqlBaseParser.TableNameContext) {
            return visitTableName((SqlBaseParser.TableNameContext) context);
        } else if (context instanceof SqlBaseParser.AliasedQueryContext) {
            if (((SqlBaseParser.AliasedQueryContext) context).query().queryTerm() instanceof SqlBaseParser.QueryTermDefaultContext) {
                return visitQueryTermDefault((SqlBaseParser.QueryTermDefaultContext) ((SqlBaseParser.AliasedQueryContext) context).query().queryTerm());
            } else {
                throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
            }
        } else if (context instanceof SqlBaseParser.AliasedRelationContext) {
            if (((SqlBaseParser.AliasedRelationContext) context).relation().relationExtension().size() == 0) {
                throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
            } else {
                LogicalJoin logicalJoin = (LogicalJoin) visitRelation(((SqlBaseParser.AliasedRelationContext) context).relation());
                if (((SqlBaseParser.AliasedRelationContext) context).tableAlias().strictIdentifier() != null) {
                    logicalJoin.setAlias(((SqlBaseParser.AliasedRelationContext) context).tableAlias().strictIdentifier().getText());
                }
                return logicalJoin;
            }
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
        }
    }

    // tableName或者db.tableName
    public LogicalPlan visitTableName(SqlBaseParser.TableNameContext context) {
        String tableName = context.multipartIdentifier().errorCapturingIdentifier.identifier().getText();
        if (context.tableAlias().strictIdentifier() != null) {
            String alias = context.tableAlias().strictIdentifier().getText();
            tableNameMap.put(alias, tableName);
            return new LogicalTable(tableName, alias);
        }
        tableNameMap.put(tableName, tableName);
        return new LogicalTable(tableName);
    }

//    public LogicalPlan visitJoinRelation(SqlBaseParser.JoinRelationContext context) {
//        return new NonRelation();
//    }

    @Override
    public NamedExpression visitNamedExpression(SqlBaseParser.NamedExpressionContext context) {
        if (context.identifierList() == null) {
            if (context.errorCapturingIdentifier() == null) {
                Expression expression = visitExpression(context.expression());
                return new NamedExpression(expression);
            } else {
                Identifier identifier = visitIdentifier(context.errorCapturingIdentifier().identifier());
                Expression expression = visitExpression(context.expression());
                return new NamedExpression(expression, identifier);
            }
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.identifierList().getText());
        }
    }

    @Override
    public Identifier visitIdentifier(SqlBaseParser.IdentifierContext context) {
        return new Identifier(context.getChild(0).getText());
    }

    @Override
    public Expression visitExpression(SqlBaseParser.ExpressionContext context) {
        return visitBooleanExpression(context.booleanExpression());
    }

    /***
     * @description 联邦学习解析
     * @param context
     * @return com.chainmaker.jobservice.core.parser.tree.FederatedLearningExpression
     * @author gaokang
     * @date 2022/8/14 22:01
     */
    @Override
    public FederatedLearningExpression visitFederatedLearningExpression(SqlBaseParser.FederatedLearningExpressionContext context) {
        List<FlExpression> fl = new ArrayList<>();
        for (int i=0; i<context.flExpressionSeq().flExpression().size(); i++) {
            FlExpression flExpression = visitFlExpression(context.flExpressionSeq().flExpression(i));
            fl.add(flExpression);
        }

        List<List<FlExpression>> labels = new ArrayList<>();
        for (int i=0; i<context.flLabelSeq(0).flLabel().size(); i++) {
            List<FlExpression> flExpressions = new ArrayList<>();
            for (int j=0; j<context.flLabelSeq(0).flLabel(i).flExpressionSeq().flExpression().size(); j++) {
                FlExpression flExpression = visitFlExpression(context.flLabelSeq(0).flLabel(i).flExpressionSeq().flExpression(j));
                flExpressions.add(flExpression);
            }
            labels.add(flExpressions);
        }

        List<FlExpression> psi = new ArrayList<>();
        for (int i=0; i<context.flPSISeq(0).flPSI(0).flExpressionSeq().flExpression().size(); i++) {
            FlExpression flExpression = visitFlExpression(context.flPSISeq(0).flPSI(0).flExpressionSeq().flExpression(i));
            psi.add(flExpression);
        }

        // feat 暂不处理
        List<List<FlExpression>> feat = new ArrayList<>();

        List<FlExpression> model = new ArrayList<>();
        Identifier key = new Identifier("model_name");
        Identifier value = new Identifier(context.flModelSeq(0).flModel(0).HELR().getText());
        FlExpression modelName = new FlExpression(key, value, FlExpression.Operator.EQUAL);
        model.add(modelName);
        for (int i=0; i<context.flModelSeq(0).flModel(0).flExpressionSeq().flExpression().size(); i++) {
            FlExpression flExpression = visitFlExpression(context.flModelSeq(0).flModel(0).flExpressionSeq().flExpression(i));
            model.add(flExpression);
        }

        List<FlExpression> eval = new ArrayList<>();
        for (int i=0; i<context.flEvalSeq(0).flEval(0).flExpressionSeq().flExpression().size(); i++) {
            FlExpression flExpression = visitFlExpression(context.flEvalSeq(0).flEval(0).flExpressionSeq().flExpression(i));
            eval.add(flExpression);
        }

        return new FederatedLearningExpression(fl, labels, psi, feat, model, eval);
    }

    @Override
    public FlExpression visitFlExpression(SqlBaseParser.FlExpressionContext context) {
        Expression left = visitValueExpressionDefault((SqlBaseParser.ValueExpressionDefaultContext) context.left);
        Expression right = visitValueExpressionDefault((SqlBaseParser.ValueExpressionDefaultContext) context.right);
        FlExpression.Operator operator = FlExpression.Operator.EQUAL;
        return new FlExpression(left, right, operator);
    }

    private static ArithmeticUnaryExpression.Sign getArithmeticUnaryOperator(Token token)
    {
        switch (token.getType()) {
            case SqlBaseLexer.MINUS:
                return ArithmeticUnaryExpression.Sign.MINUS;
            case SqlBaseLexer.PLUS:
                return ArithmeticUnaryExpression.Sign.PLUS;
        }

        throw new ParserException(DEFAULT_ERROR + ": " + token.getText());
    }

    private static LogicalExpression.Operator getLogicalOperator(Token token)
    {
        switch (token.getType()) {
            case SqlBaseLexer.AND:
                return LogicalExpression.Operator.AND;
            case SqlBaseLexer.PLUS:
                return LogicalExpression.Operator.OR;
        }

        throw new ParserException(DEFAULT_ERROR + ": " + token.getText());
    }

    private static ArithmeticBinaryExpression.Operator getArithmeticBinaryOperator(Token operator) {
        switch (operator.getType()) {
            case SqlBaseLexer.PLUS:
                return ArithmeticBinaryExpression.Operator.ADD;
            case SqlBaseLexer.MINUS:
                return ArithmeticBinaryExpression.Operator.SUBTRACT;
            case SqlBaseLexer.ASTERISK:
                return ArithmeticBinaryExpression.Operator.MULTIPLY;
            case SqlBaseLexer.SLASH:
                return ArithmeticBinaryExpression.Operator.DIVIDE;
            case SqlBaseLexer.PERCENT:
                return ArithmeticBinaryExpression.Operator.MODULUS;
        }

        throw new ParserException(DEFAULT_ERROR + ": " + operator.getText());
    }

    private static ComparisonExpression.Operator getComparisonOperator(Token symbol) {
        switch (symbol.getType()) {
            case SqlBaseLexer.EQ:
                return ComparisonExpression.Operator.EQUAL;
            case SqlBaseLexer.NEQ:
                return ComparisonExpression.Operator.NOT_EQUAL;
            case SqlBaseLexer.LT:
                return ComparisonExpression.Operator.LESS_THAN;
            case SqlBaseLexer.LTE:
                return ComparisonExpression.Operator.LESS_THAN_OR_EQUAL;
            case SqlBaseLexer.GT:
                return ComparisonExpression.Operator.GREATER_THAN;
            case SqlBaseLexer.GTE:
                return ComparisonExpression.Operator.GREATER_THAN_OR_EQUAL;
        }

        throw new ParserException(DEFAULT_ERROR + ": " + symbol.getText());
    }
}
