package com.chainmaker.jobservice.core.parser;

import com.chainmaker.jobservice.api.response.ParserException;
import com.chainmaker.jobservice.core.antlr4.gen.SqlBaseLexer;
import com.chainmaker.jobservice.core.antlr4.gen.SqlBaseParser;
import com.chainmaker.jobservice.core.antlr4.gen.SqlBaseParser.*;
import com.chainmaker.jobservice.core.antlr4.gen.SqlBaseParserBaseVisitor;
import com.chainmaker.jobservice.core.parser.plans.*;
import com.chainmaker.jobservice.core.parser.tree.*;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gaokang
 * @date 2023-04-01 15:06
 * @description:
 * @version:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LogicalPlanBuilderV2 extends SqlBaseParserBaseVisitor {
    private HashMap<String, String> tableNameMap = new HashMap<>();
    public List<String> modelNameList = new ArrayList<>();
    private static final String DEFAULT_ERROR = "SQL语法暂不支持";
    private LogicalPlan logicalPlan;
    private HashMap<String, LogicalPlan> joinCache = new HashMap<>();
    private LogicalJoin rootJoin;
    private Expression rootFilterExpression;
    private LogicalExpression.Operator currentOp = LogicalExpression.Operator.AND;

    private Set<String> assetAndColumnList = new HashSet<>();

    private HashMap<String,LogicalPlan> subQueryTables = Maps.newHashMap();

    public  List<String> getColumnList() {
        final String regexStr = "\\d+.\\d*";
        return assetAndColumnList.stream().filter(e -> {
         if (StringUtils.contains(e, ".") && !e.matches(regexStr)){
             return true;
         }
         return false;
        }).collect(Collectors.toList());
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
        ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
//        parser.singleStatement()
//        if (aggCallList != null) {
//            backFillGroupBy(logicalPlan);
//        }
    }

    /**
     * 回填group by中空缺的需要聚合的列信息
     */
    public void backFillGroupBy(LogicalPlan root) {
        if (root instanceof LogicalAggregate) {
//            ((LogicalAggregate) root).addAggCallAll(aggCallList);
            return;
        } else {
            for (Node child : root.getChildren()) {
                backFillGroupBy((LogicalPlan) child);
            }
        }
    }

    @Override
    public LogicalPlan visitSingleStatement(SingleStatementContext context) {
        if (context.statement() instanceof StatementDefaultContext) {
            return visitStatementDefault((StatementDefaultContext) context.statement());
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.statement().getChild(0).getText());
        }
    }

    @Override
    public LogicalPlan visitStatementDefault(StatementDefaultContext context) {
        return visitQuery(context.query());
    }

    @Override
    public LogicalPlan visitQuery(QueryContext context) {
        if (context.queryTerm() instanceof QueryTermDefaultContext) {
            return visitQueryTermDefault((QueryTermDefaultContext) context.queryTerm());
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.queryTerm().getChild(0).getText());
        }
    }

    @Override
    public LogicalPlan visitQueryTermDefault(QueryTermDefaultContext context) {
        if (context.queryPrimary() instanceof QueryPrimaryDefaultContext) {
            return visitQueryPrimaryDefault((QueryPrimaryDefaultContext)context.queryPrimary());
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.queryPrimary().getText());
        }
    }
    @Override
    public LogicalPlan visitQueryPrimaryDefault(QueryPrimaryDefaultContext context) {
        if (context.querySpecification() instanceof RegularQuerySpecificationContext) {
            if (((RegularQuerySpecificationContext) context.querySpecification()).selectClause().hints.isEmpty()) {
                return visitRegularQuerySpecification((RegularQuerySpecificationContext) context.querySpecification());
            } else {
                LogicalPlan child = visitRegularQuerySpecification((RegularQuerySpecificationContext) context.querySpecification());
                List<HintExpression> hintExpressions = new ArrayList<>();
                for (HintStatementContext hintStatementContext: ((RegularQuerySpecificationContext) context.querySpecification()).selectClause().hint(0).hintStatement()) {
                    List<String> values = new ArrayList<>();
                    String key = hintStatementContext.hintName.getText();
                    for (PrimaryExpressionContext primaryExpressionContext: hintStatementContext.parameters) {
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
    public LogicalPlan visitRegularQuerySpecification(RegularQuerySpecificationContext context) {

        List<LogicalPlan> children = new ArrayList<>();

        if (context.fromClause() == null) {
            throw new ParserException(DEFAULT_ERROR + ": " + "缺少From");
        } else if (context.whereClause() == null) {
            List<LogicalPlan> fromList = visitFrom(context.fromClause());
            children.addAll(fromList);
        } else {
            visitFrom(context.fromClause());
            children.add(visitWhere(context));
        }

        if (context.selectClause().namedExpressionSeq() instanceof FederatedLearningExpressionContext) {
            FederatedLearningExpression expression = visitFederatedLearningExpression((FederatedLearningExpressionContext) context.selectClause().namedExpressionSeq());
            return new FederatedLearning(expression, children);
        } else {
            FaderatedQueryExpression projectList = visitFederatedQueryExpression((FederatedQueryExpressionContext) context.selectClause().namedExpressionSeq());

            if (context.aggregationClause() == null) {
                if (projectList.getValues().get(0).toString().equals("*")) {
                    return children.get(0);
                } else {
                    return new LogicalProject(projectList, children);
                }
            } else {
                LogicalProject child = new LogicalProject(projectList, children);
                if (context.havingClause() == null) {
                    return visitAggregate(context, List.of(child));
                } else {
                    return visitHaving(context, List.of(child));
                }
            }
        }
    }

    private LogicalPlan visitHaving(RegularQuerySpecificationContext context, List<LogicalPlan> children) {
        Expression havingCond = visitBooleanExpression(context.havingClause().booleanExpression());
//        System.out.println(havingCond);
//        String[] sp = havingCond.toString().split(" |=|\\(|\\)|>|<|\\+|-|\\*|/|!=|,|\\[|]");
//        for (String s : sp) {
//            if (s.contains(".")) {
//                System.out.println(s);
//            }
//        }
        LogicalPlan child = visitAggregate(context, children);
        return new LogicalFilter(havingCond, List.of(child));
    }

    public LogicalPlan visitAggregate(RegularQuerySpecificationContext context, List<LogicalPlan> children) {

        List<Expression> groupKeys = new ArrayList<>();
//        System.out.println(context.aggregationClause().groupByClause());
        for (GroupByClauseContext ctx : context.aggregationClause().groupByClause()) {
            groupKeys.add(visitExpression(ctx.expression()));
        }
        List<Expression> aggCallList = new ArrayList<>();
        return new LogicalAggregate(groupKeys, aggCallList, children);
    }
//    public LogicalPlan visitWhere(RegularQuerySpecificationContext context) {
//        Expression expression = visitBooleanExpression(context.whereClause().booleanExpression());
//        List<LogicalPlan> children = visitFrom(context.fromClause());
//        return new LogicalFilter(expression, children);
//    }

    public LogicalPlan visitWhere(RegularQuerySpecificationContext context) {
        Expression expression = visitBooleanExpression(context.whereClause().booleanExpression());
        if (expression instanceof ComparisonExpression) {
            dealWithComparisonExpression((ComparisonExpression) expression);
        } else if (expression instanceof LogicalExpression) {
            dealWithLogicalExpression((LogicalExpression) expression);
        } else {
            throw new ParserException("暂不支持的where语法");
        }
        if (rootJoin == null) {
            List<LogicalPlan> children = visitFrom(context.fromClause());
            return new LogicalFilter(rootFilterExpression, children);
        } else if (rootFilterExpression == null) {
            visitFrom(context.fromClause());
            return rootJoin;
        } else {
            visitFrom(context.fromClause());
            List<LogicalPlan> children = new ArrayList<>();
            children.add(rootJoin);
            return new LogicalFilter(rootFilterExpression, children);
        }

    }
    private void dealWithLogicalExpression(LogicalExpression logicalExpression) {
        if (logicalExpression.getLeft() instanceof ComparisonExpression) {
            dealWithComparisonExpression((ComparisonExpression) logicalExpression.getLeft());
        } else if (logicalExpression.getLeft() instanceof LogicalExpression) {
            dealWithLogicalExpression((LogicalExpression) logicalExpression.getLeft());
        } else {
            throw new ParserException("暂不支持的语法");
        }

        if (logicalExpression.getRight() instanceof ComparisonExpression) {
            dealWithComparisonExpression((ComparisonExpression) logicalExpression.getRight());
        } else if (logicalExpression.getRight() instanceof LogicalExpression) {
            dealWithLogicalExpression((LogicalExpression) logicalExpression.getRight());
        } else {
            throw new ParserException("暂不支持的语法");
        }
    }
    private void dealWithComparisonExpression(ComparisonExpression expression) {
        if (expression.getLeft() instanceof DereferenceExpression && expression.getRight() instanceof DereferenceExpression) {
            String leftTable = ((DereferenceExpression) expression.getLeft()).getBase().toString();
            String rightTable = ((DereferenceExpression) expression.getRight()).getBase().toString();
            LogicalPlan left, right;
            if (joinCache.containsKey(leftTable)) {
                left = joinCache.get(leftTable);
            } else if(subQueryTables.containsKey(leftTable)) {
                left = subQueryTables.get(leftTable);
            } else{
                left = new LogicalTable(((DereferenceExpression) expression.getLeft()).getBase().toString());
            }
            if (joinCache.containsKey(rightTable)) {
                right = joinCache.get(rightTable);
            } else if(subQueryTables.containsKey(rightTable)) {
                right = subQueryTables.get(rightTable);
            }else {
                right = new LogicalTable(((DereferenceExpression) expression.getRight()).getBase().toString());
            }
            LogicalJoin.Type joinType = LogicalJoin.Type.INNER;
            LogicalJoin node = new LogicalJoin(expression, joinType, left, right);
            rootJoin = node;
            joinCache.put(((DereferenceExpression) expression.getLeft()).getBase().toString(), node);
            joinCache.put(((DereferenceExpression) expression.getRight()).getBase().toString(), node);
        } else {
            if (rootFilterExpression == null) {
                rootFilterExpression = expression;
            } else {
                LogicalExpression currentExpression = new LogicalExpression(rootFilterExpression, expression, currentOp);
                rootFilterExpression = currentExpression;
            }
        }
    }

    private void dealWithSubQueryExpression(SubQueryExpression expression) {
        //todo
    }

    public Expression visitBooleanExpression(BooleanExpressionContext context) {
        if (context instanceof LogicalBinaryContext) {
            return visitLogicalBinary((LogicalBinaryContext) context);
        } else if (context instanceof LogicalNotContext) {
            return visitLogicalNot((LogicalNotContext) context);
        } else if (context instanceof PredicatedContext) {
            return visitPredicated((PredicatedContext) context);
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
        }
    }

    @Override
    public LogicalExpression visitLogicalBinary(LogicalBinaryContext context) {
        Expression left = visitBooleanExpression(context.left);
        LogicalExpression.Operator operator = getLogicalOperator(context.operator);
        Expression right = visitBooleanExpression(context.right);
        return new LogicalExpression(left, right, operator);
    }
    @Override
    public NotExpression visitLogicalNot(LogicalNotContext context) {
        Expression expression = visitBooleanExpression(context.booleanExpression());
        return new NotExpression(expression);
    }

    @Override
    public Expression visitPredicated(PredicatedContext context) {
        if (context.predicate() == null) {
            return visitValueExpression(context.valueExpression());
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
        }
    }
    public Expression visitValueExpression(ValueExpressionContext context) {
        if (context instanceof ValueExpressionDefaultContext) {
            return visitValueExpressionDefault((ValueExpressionDefaultContext) context);
        } else if (context instanceof ArithmeticBinaryContext) {
            return visitArithmeticBinary((ArithmeticBinaryContext) context);
        } else if (context instanceof ArithmeticUnaryContext) {
            return visitArithmeticUnary((ArithmeticUnaryContext) context);
        } else if (context instanceof ComparisonContext) {
            return visitComparison((ComparisonContext) context);
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
        }
    }
    @Override
    public ArithmeticBinaryExpression visitArithmeticBinary(ArithmeticBinaryContext context) {
        Expression left = visitValueExpression(context.left);
        ArithmeticBinaryExpression.Operator operator = getArithmeticBinaryOperator(context.operator);
        Expression right = visitValueExpression(context.right);
        return new ArithmeticBinaryExpression(left, right, operator);
    }
    @Override
    public ArithmeticUnaryExpression visitArithmeticUnary(ArithmeticUnaryContext context) {
        ArithmeticUnaryExpression.Sign sign = getArithmeticUnaryOperator(context.operator);
        Expression expression = visitValueExpression(context.valueExpression());
        return new ArithmeticUnaryExpression(expression, sign);
    }
    @Override
    public ComparisonExpression visitComparison(ComparisonContext context) {
        Expression left = visitValueExpression(context.left);
        ComparisonExpression.Operator operator = getComparisonOperator(((TerminalNode) context.comparisonOperator().getChild(0)).getSymbol());
        Expression right = visitValueExpression(context.right);
        return new ComparisonExpression(left, right, operator);
    }
    @Override
    public Expression visitValueExpressionDefault(ValueExpressionDefaultContext context) {
        return visitPrimaryExpression(context.primaryExpression());
    }

    public Expression visitPrimaryExpression(PrimaryExpressionContext context) {
        if (context instanceof ColumnReferenceContext) {
            assetAndColumnList.add(context.getText());
            return visitColumnReference((ColumnReferenceContext) context);
        } else if (context instanceof DereferenceContext) {
            assetAndColumnList.add(context.getText());
            return visitDereference((DereferenceContext) context);
        } else if (context instanceof ConstantDefaultContext) {
            return visitConstantDefault((ConstantDefaultContext) context);
        } else if (context instanceof ParenthesizedExpressionContext) {
            return visitParenthesizedExpression((ParenthesizedExpressionContext) context);
        } else if (context instanceof FunctionCallContext) {
            return visitFunctionCall((FunctionCallContext) context);
        } else if (context instanceof FeatureReferenceContext) {
            return visitFeatureReference((FeatureReferenceContext) context);
        } else if (context instanceof PirCaseContext) {
            return visitPirCase((PirCaseContext) context);
        } else if (context instanceof StarContext) {
            return visitStar((StarContext) context);
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
        }
    }
    @Override
    public Identifier visitPirCase(PirCaseContext context) {
        String identifier = context.getText();
        return new Identifier(identifier);
    }
    @Override
    public Identifier visitStar(StarContext context) {
        String identifier = context.getText();
        return new Identifier(identifier);
    }
    @Override
    public Identifier visitColumnReference(ColumnReferenceContext context) {
        String identifier = context.identifier().getChild(0).getText();
        return new Identifier(identifier);
    }
    @Override
    public Identifier visitFeatureReference(FeatureReferenceContext context) {
        String identifier = context.getText();
        return new Identifier(identifier);
    }
    @Override
    public DereferenceExpression visitDereference(DereferenceContext context) {
        Expression base = visitPrimaryExpression(context.base);
        String fieldName = context.fieldName.getChild(0).getText();
        return new DereferenceExpression(base, fieldName);
    }
    @Override
    public ConstantExpression visitConstantDefault(ConstantDefaultContext context) {
        String identifier = context.constant().getChild(0).getText();
        return new ConstantExpression(identifier);
    }
    @Override
    public ParenthesizedExpression visitParenthesizedExpression(ParenthesizedExpressionContext context) {
        Expression expression = visitExpression(context.expression());
        return new ParenthesizedExpression(expression);
    }
    @Override
    public FunctionCallExpression visitFunctionCall(FunctionCallContext context) {
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
    public FaderatedQueryExpression visitFederatedQueryExpression(FederatedQueryExpressionContext context) {
        List<NamedExpression> namedExpressions = new ArrayList<>();
        for (int i=0; i<context.namedExpression().size(); i++) {
            NamedExpression namedExpression = visitNamedExpression(context.namedExpression(i));
            namedExpressions.add(namedExpression);
        }
        return new FaderatedQueryExpression(namedExpressions);
    }

    public List<LogicalPlan> visitFrom(FromClauseContext context) {
        List<LogicalPlan> logicalPlans = new ArrayList<>();
        for (int i=0; i<context.relation().size(); i++) {
            logicalPlans.add(visitRelation(context.relation(i)));
        }
        return logicalPlans;
    }

    @Override
    public LogicalPlan visitRelation(RelationContext context) {
        if (context.relationExtension().isEmpty()) {
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
                Expression expression = visitPredicated((PredicatedContext) context.relationExtension(0).joinRelation().joinCriteria().booleanExpression());
                return new LogicalJoin(expression, joinType, left, right);
            } else {
                throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
            }
        }
    }


    public LogicalPlan visitRelationPrimary(RelationPrimaryContext context) {
        if (context instanceof TableNameContext) {
            return visitTableName((TableNameContext) context);
        } else if (context instanceof AliasedQueryContext) {
            AliasedQueryContext aliaseCtx = (AliasedQueryContext) context;
            QueryTermContext queryTermContext = aliaseCtx.query().queryTerm();
            if (queryTermContext instanceof QueryTermDefaultContext) {
                QueryTermDefaultContext termCtx = (QueryTermDefaultContext) queryTermContext;
//                LogicalPlan child = visitQueryTermDefault(termCtx);
                LogicalPlan child = visitAliasedQuery(aliaseCtx);
                String tableAlias = aliaseCtx.tableAlias().getText();
                SubQuery subQuery = new SubQuery(tableAlias, child);
                subQueryTables.put(tableAlias, subQuery);
                return new SubQuery(tableAlias, child);
            } else {
                throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
            }
        } else if (context instanceof AliasedRelationContext) {
            if (((AliasedRelationContext) context).relation().relationExtension().isEmpty()) {
                throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
            } else {
                LogicalJoin logicalJoin = (LogicalJoin) visitRelation(((AliasedRelationContext) context).relation());
                if (((AliasedRelationContext) context).tableAlias().strictIdentifier() != null) {
                    logicalJoin.setAlias(((AliasedRelationContext) context).tableAlias().strictIdentifier().getText());
                }
                return logicalJoin;
            }
        } else {
            throw new ParserException(DEFAULT_ERROR + ": " + context.getText());
        }
    }

    // tableName或者db.tableName
    public LogicalPlan visitTableName(TableNameContext context) {
        String tableName = context.multipartIdentifier().errorCapturingIdentifier.identifier().getText();
        if (context.tableAlias().strictIdentifier() != null) {
            String alias = context.tableAlias().strictIdentifier().getText();
            tableNameMap.put(alias, tableName);
            return new LogicalTable(tableName, alias);
        }
        tableNameMap.put(tableName, tableName);
        return new LogicalTable(tableName);
    }

//    public LogicalPlan visitJoinRelation(JoinRelationContext context) {
//        return new NonRelation();
//    }

    @Override
    public NamedExpression visitNamedExpression(NamedExpressionContext context) {
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
    public Identifier visitIdentifier(IdentifierContext context) {
        return new Identifier(context.getChild(0).getText());
    }

    @Override
    public Expression visitExpression(ExpressionContext context) {
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
    public FederatedLearningExpression visitFederatedLearningExpression(FederatedLearningExpressionContext context) {
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
        if (!context.flPSISeq().isEmpty()) {
            for (int i = 0; i < context.flPSISeq(0).flPSI(0).flExpressionSeq().flExpression().size(); i++) {
                FlExpression flExpression = visitFlExpression(context.flPSISeq(0).flPSI(0).flExpressionSeq().flExpression(i));
                psi.add(flExpression);
            }
        }

        List<FlExpression> feat = new ArrayList<>();
        if (!context.flFeatSeq().isEmpty()) {
            Identifier feature_key = new Identifier("feature_name");
            Identifier feature_value = new Identifier(context.flFeatSeq(0).flFeat(0).feat.getText());
            FlExpression featName = new FlExpression(feature_key, feature_value, FlExpression.Operator.EQUAL);
            feat.add(featName);
            if (!context.flFeatSeq().isEmpty()) {
                for (int i = 0; i < context.flFeatSeq(0).flFeat(0).flExpressionSeq().flExpression().size(); i++) {
                    FlExpression flExpression = visitFlExpression(context.flFeatSeq(0).flFeat(0).flExpressionSeq().flExpression(i));
                    feat.add(flExpression);
                }
            }
        }
        List<FlExpression> model = new ArrayList<>();
        if (!context.flModelSeq().isEmpty()) {
            Identifier key = new Identifier("model_name");
            Identifier value = new Identifier(context.flModelSeq(0).flModel(0).model.getText());
            FlExpression modelName = new FlExpression(key, value, FlExpression.Operator.EQUAL);
            model.add(modelName);
            for (int i = 0; i < context.flModelSeq(0).flModel(0).flExpressionSeq().flExpression().size(); i++) {
                FlExpression flExpression = visitFlExpression(context.flModelSeq(0).flModel(0).flExpressionSeq().flExpression(i));
                model.add(flExpression);
            }
        }

        List<FlExpression> eval = new ArrayList<>();
        if (!context.flModelSeq().isEmpty()) {
            for (int i = 0; i < context.flEvalSeq(0).flEval(0).flExpressionSeq().flExpression().size(); i++) {
                FlExpression flExpression = visitFlExpression(context.flEvalSeq(0).flEval(0).flExpressionSeq().flExpression(i));
                eval.add(flExpression);
            }
        }

        return new FederatedLearningExpression(fl, labels, psi, feat, model, eval);
    }

    @Override
    public FlExpression visitFlExpression(FlExpressionContext context) {
        Expression left = visitValueExpressionDefault((ValueExpressionDefaultContext) context.left);
        Expression right = visitValueExpressionDefault((ValueExpressionDefaultContext) context.right);
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

    @Override
    public LogicalPlan visitAliasedQuery(AliasedQueryContext ctx) {
//        QueryTermContext queryTermContext = ctx.query().queryTerm();
//        if (queryTermContext instanceof QueryTermDefaultContext) {
//            QueryTermDefaultContext termCtx = (QueryTermDefaultContext) queryTermContext;
//            return visitQuery(ctx.query());
//        }
        subQueryTables.put(ctx.tableAlias().getText(), visitQuery(ctx.query()));
        return visitQuery(ctx.query());
    }
}
