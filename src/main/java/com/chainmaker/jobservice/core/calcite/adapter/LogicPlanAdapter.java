package com.chainmaker.jobservice.core.calcite.adapter;

import com.chainmaker.jobservice.core.calcite.cost.MPCCost;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.MPCMetadata;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.TableInfo;
import com.chainmaker.jobservice.core.parser.plans.*;
import com.chainmaker.jobservice.core.parser.tree.*;
import org.apache.calcite.DataContext;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.plan.*;
import org.apache.calcite.rel.*;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.*;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;
import org.apache.calcite.tools.RelBuilderFactory;
import org.apache.tomcat.util.http.LegacyCookieProcessor;

import java.util.*;

import static com.chainmaker.jobservice.core.calcite.utils.ConstExprJudgement.isInteger;
import static com.chainmaker.jobservice.core.calcite.utils.ConstExprJudgement.isNumeric;

/**
 * 逻辑计划转换器，能够将Antlr4生成的逻辑计划转换成Calcite格式
 */
public class LogicPlanAdapter extends LogicalPlanRelVisitor {

    private String originQuery;                 // sql语句
    private LogicalPlan originLogicalPlan;      // antlr4的逻辑计划

    private MPCMetadata metadata;               // 全局metadata
    private RelNode root;                       // 生成的calcite的逻辑计划
    private SchemaPlus rootSchema;              // 语句涉及的schema信息
    private FrameworkConfig config;             // 创建builder的一些配置
    private RelBuilder builder;                 // RelNode生成器
    private Integer modelType;                      // 需要处理的任务类型
//    private RelBuilderFactory relBuilderFactory;

    List<String> multiTableList = new ArrayList<>(); // 用于存储参加join的属性名

    /**
     * 构造函数，主要处理了元数据相关信息和schema信息
     * @param sql
     * @param plan
     */
    public LogicPlanAdapter(String sql, LogicalPlan plan, HashMap<String, TableInfo> data, Integer type) {
        originQuery = sql;
        originLogicalPlan = plan;
        root = null;
        metadata = MPCMetadata.getInstance();
        metadata.init(originQuery, data);
        modelType = type;
        generateSchema();
    }

    /**
     * 通过全局metadata来初始化root的schema信息
     * 此处大多是Calcite的标准写法
     */
    private void generateSchema() {
        HashMap<String, HashMap<String, SqlTypeName>> tableFieldMap = metadata.getTableFieldMap();
        List<Object[]> _DATA = Arrays.asList();
        RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
        rootSchema = Frameworks.createRootSchema(true);
        for (String key : tableFieldMap.keySet()) {
            RelDataTypeFactory.Builder builder = new RelDataTypeFactory.Builder(typeFactory);
            for (String fieldName : tableFieldMap.get(key).keySet()) {
                builder.add(fieldName, tableFieldMap.get(key).get(fieldName));
            }
            rootSchema.add(key, new ListTable(builder.build(), _DATA));
        }

        config = Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.Config.DEFAULT)
                .defaultSchema(rootSchema)
                .traitDefs((List<RelTraitDef>) null)
                .costFactory(MPCCost.FACTORY)           // 此处将cost计算方式换成自行实现的版本
                .build();

//        System.out.println("generate schema success");
    }


    /**
     * 将antlr4逻辑计划转化成calcite逻辑计划
     */
    public void CastToRelNode() {
        builder = RelBuilder.create(config);
        root = visit(originLogicalPlan);

    }

    /**
     * 启动遍历整个逻辑计划树
     * @param plan
     * @return
     */
    @Override
    public RelNode visit(LogicalPlan plan) {
//        System.out.println("enter logical plan relvisit");
        return plan.accept(this);
    }

    @Override
    public RelNode visit(LogicalHint node) {
        RelNode ans = null;
        int childNum = node.getChildren().size();
        RelNode[] childs = new RelNode[childNum];
        for (int i = 0; i < childNum; i++) {
            childs[i] = node.getChildren().get(i).accept(this);
            builder.push(childs[i]);
        }
        ans = builder.build();
//        System.out.println(
//                RelOptUtil.dumpPlan("[Logical plan]", ans, SqlExplainFormat.TEXT,
//                        SqlExplainLevel.EXPPLAN_ATTRIBUTES));
        return ans;
    }

    @Override
    public RelNode visit(FederatedLearning node) {
        RelNode ans = null;
        int childNum = node.getChildren().size();
        RelNode[] childs = new RelNode[childNum];
        for (int i = 0; i < childNum; i++) {
            childs[i] = node.getChildren().get(i).accept(this);
            builder.push(childs[i]);
        }
        ans = builder.build();
//        System.out.println(
//                RelOptUtil.dumpPlan("[Logical plan]", ans, SqlExplainFormat.TEXT,
//                        SqlExplainLevel.EXPPLAN_ATTRIBUTES));
        return ans;
    }

    /**
     * 处理Project节点
     * @param node
     * @return
     */
    @Override
    public RelNode visit(LogicalProject node) {
        RelNode ans = null;
        // 优先处理子节点
        int childNum = node.getChildren().size();
        RelNode[] childs = new RelNode[childNum];
        for (int i = 0; i < childNum; i++) {
            childs[i] = node.getChildren().get(i).accept(this);
            builder.push(childs[i]);
            multiTableList.addAll(childs[i].getRowType().getFieldNames());
            if (i != childNum-1) {
                multiTableList.add("::");
            }
        }
//        System.out.println(multiTableList);

        // 找到所有映射相关表达式
        List<RexNode> projections = new ArrayList<>();
        List<NamedExpression> namedExpressionList = node.getProjectList().getValues();
        List<String> projectionNames = new ArrayList<>();

        // 将每个表达式转换成的RexNode放入需要映射的列表
        for (NamedExpression exp : namedExpressionList) {
            Expression expr = exp.getExpression();
            String alias = exp.getIdentifier().getIdentifier();
//            System.out.println("Project Expression = " + expr.toString());
//            System.out.println("expression class = " + expr.getClass());
            if (modelType == 2 && expr instanceof FunctionCallExpression) {
                continue;
            }
            RexNode proj = dealWithExpression(expr);
            projectionNames.add(expr.toString());
//            System.out.println(expr.toString() + " : " + proj);
            if (alias != null) {
                projections.add(builder.alias(proj, alias));
            } else {
                projections.add(proj);
            }
        }

        // 应用所有映射并返回
//        builder.project(projections);
        builder.project(projections, projectionNames);
        ans = builder.build();
        multiTableList.clear();
        long end = System.currentTimeMillis();
//        System.out.println(
//                RelOptUtil.dumpPlan("[Logical plan]", ans, SqlExplainFormat.TEXT,
//                        SqlExplainLevel.ALL_ATTRIBUTES));
        return ans;
    }

    /**
     * 处理Filter节点
     * @param node
     * @return
     */
    @Override
    public RelNode visit(LogicalFilter node) {
        RelNode ans = null;
        // 优先处理子节点
        int childNum = node.getChildren().size();
        RelNode[] childs = new RelNode[childNum];
        for (int i = 0; i < childNum; i++) {
            childs[i] = node.getChildren().get(i).accept(this);
            builder.push(childs[i]);
            multiTableList.addAll(childs[i].getRowType().getFieldNames());
            if (i != childNum-1) {
                multiTableList.add("::");
            }
        }

        // 处理filter的条件
        RexNode cond = dealWithExpression(node.getCondition());

        builder.filter(cond);

        ans = builder.build();
        multiTableList.clear();
        long end = System.currentTimeMillis();
//        System.out.println(
//                RelOptUtil.dumpPlan("[Logical plan]", ans, SqlExplainFormat.TEXT,
//                        SqlExplainLevel.EXPPLAN_ATTRIBUTES));
        return ans;
    }

    /**
     * 处理Join节点
     * @param node
     * @return
     */
    @Override
    public RelNode visit(LogicalJoin node) {
        RelNode ans = null;
        // 先访问要Join的左右节点，并获取JoinType
        RelNode left = node.getLeft().accept(this);
        RelNode right = node.getRight().accept(this);
        builder.push(left);
        builder.push(right);

        JoinRelType joinType;
        switch (node.getJoinType()) {
            case INNER:
                joinType = JoinRelType.INNER;
                break;
            case LEFT:
                joinType = JoinRelType.LEFT;
                break;
            case RIGHT:
                joinType = JoinRelType.RIGHT;
                break;
            case FULL:
                joinType = JoinRelType.FULL;
                break;
            default:
                joinType = JoinRelType.INNER;
                break;
        }

        // 用于取属性时区分builder中是否存在多个表，执行的操作会不同
        multiTableList.addAll(left.getRowType().getFieldNames());
        multiTableList.add("::");
        multiTableList.addAll(right.getRowType().getFieldNames());

        // 处理join条件并参加join
        RexNode joinCond = dealWithExpression(node.getCondition());

        builder.join(joinType, joinCond);

        ans = builder.build();
        multiTableList.clear();
        long end = System.currentTimeMillis();
//        System.out.println(
//                RelOptUtil.dumpPlan("[Logical plan]", ans, SqlExplainFormat.TEXT,
//                        SqlExplainLevel.EXPPLAN_ATTRIBUTES));
        return ans;
    }

    /**
     * 处理Table节点，必为叶子节点，直接扫描表
     * @param node
     * @return
     */
    @Override
    public RelNode visit(LogicalTable node) {
        String tableName = node.getTableName();
        String alias = node.getAlias();

        builder.scan(tableName);

        if (alias != null) {
            builder.as(alias);      // 别名
        }

        RelNode ans = builder.build();
        long end = System.currentTimeMillis();
//        System.out.println(
//                RelOptUtil.dumpPlan("[Logical plan]", ans, SqlExplainFormat.TEXT,
//                        SqlExplainLevel.EXPPLAN_ATTRIBUTES));

        return ans;
    }

    /**
     * 处理Sort节点
     * 暂时并没有用到，之后会随着解析部分的更新再加入新的处理
     * @param node
     * @return
     */
    @Override
    public RelNode visit(LogicalSort node) {
        RelNode ans = null;
        RelNode child = node.getChild().accept(this);
        builder.push(child);
        RexNode sortCond = dealWithExpression(node.getExpression());
        builder.sort(sortCond);
        ans = builder.build();
        return ans;
    }

    /**
     * 处理各种类型的Expression
     * @param expr
     * @return
     */
    private RexNode dealWithExpression(Expression expr) {
        if (expr instanceof ArithmeticBinaryExpression) {
            // 二元数学运算（+ - * / %）
            return dealWithArithmeticBinaryExpression((ArithmeticBinaryExpression) expr);
        } else if (expr instanceof ArithmeticUnaryExpression) {
            // 一元数学运算（+ -）
            return dealWithArithmeticUnaryExpression((ArithmeticUnaryExpression) expr);
        } else if (expr instanceof ArrayFlExpression) {
            // 联邦学习相关
            return dealWithArrayFlExpression((ArrayFlExpression) expr);
        } else if (expr instanceof ComparisonExpression) {
            // 比较表达式（< <= > >= = !=）
            return dealWithComparisonExpression((ComparisonExpression) expr);
        } else if (expr instanceof ConstantExpression) {
            // 常量表达式（20 “test”等）
            return dealWithConstantExpression((ConstantExpression) expr);
        } else if (expr instanceof DereferenceExpression) {
            // 属性表达式（TA.ID， TB.B等）
            return dealWithDereferenceExpression((DereferenceExpression) expr);
        } else if (expr instanceof FaderatedQueryExpression) {
            // 联邦查询相关
            return dealWithFaderatedQueryExpression((FaderatedQueryExpression) expr);
        } else if (expr instanceof FederatedLearningExpression) {
            // 联邦学习相关
            return dealWithFederatedLearningExpression((FederatedLearningExpression) expr);
        } else if (expr instanceof FlExpression) {
            // 联邦学习相关
            return dealWithFlExpression((FlExpression) expr);
        } else if (expr instanceof FlLabelExpression) {
            // 联邦学习相关
            return dealWithFlLabelExpression((FlLabelExpression) expr);
        } else if (expr instanceof FunctionCallExpression) {
            // 聚合表达式（SUM，COUNT，MAX，MIN）
            return dealWithFunctionCallExpression((FunctionCallExpression) expr);
        } else if (expr instanceof Identifier) {
            return dealWithIdentifier((Identifier) expr);
        } else if (expr instanceof LogicalExpression) {
            // 逻辑表达式（AND，OR）
            return dealWithLogicalExpression((LogicalExpression) expr);
        } else if (expr instanceof NamedExpression) {
            // 暂时没用到
            return dealWithNamedExpression((NamedExpression) expr);
        } else if (expr instanceof NotExpression) {
            // 逻辑非表达式（NOT）
            return dealWithNotExpression((NotExpression) expr);
        } else if (expr instanceof ParenthesizedExpression) {
            // 括号表达式（（））
            return dealWithParenthesizedExpression((ParenthesizedExpression) expr);
        } else if (expr instanceof SubQueryExpression) {
            // 子查询表达式
            return dealWithSubQueryExpression((SubQueryExpression) expr);
        } else {
            ;
        }
        return null;
    }

    // 这些都是与联邦学习和联邦查询相关的表达式，不需要处理
    private RexNode dealWithFlLabelExpression(FlLabelExpression expr) {
        return null;
    }

    private RexNode dealWithFlExpression(FlExpression expr) {
        return null;
    }

    private RexNode dealWithFederatedLearningExpression(FederatedLearningExpression expr) {
        return null;
    }

    private RexNode dealWithFaderatedQueryExpression(FaderatedQueryExpression expr) {
        return null;
    }

    private RexNode dealWithArrayFlExpression(ArrayFlExpression expr) {
        return null;
    }

    private RexNode dealWithNamedExpression(NamedExpression expr) {
        return null;
    }

    /**
     * 处理逻辑表达式（AND OR）
     * @param expr
     * @return
     */
    private RexNode dealWithLogicalExpression(LogicalExpression expr) {
//        System.out.println("enter dealWithLogicalExpression function");
        RexNode ans = null;
        RexNode left = dealWithExpression(expr.getLeft());
        RexNode right = dealWithExpression(expr.getRight());
        SqlBinaryOperator op = SqlStdOperatorTable.AND;
        switch (expr.getOperator()) {
            case AND:
                op = SqlStdOperatorTable.AND;
                break;
            case OR:
                op = SqlStdOperatorTable.OR;
                break;
        }
        ans = builder.call(op, left, right);
        return ans;
    }

    /**
     * 处理子查询表达式（不过目前Parser并没有支持）
     * @param expr
     * @return
     */
    private RexNode dealWithSubQueryExpression(SubQueryExpression expr) {
//        System.out.println("enter dealWithSubQueryExpression function");
        RexNode ans = null;
        RelNode node = visit(expr.getPlan());
//        builder.push(node);
//        ans = builder.call(SqlStdOperatorTable.EQUALS, builder.literal(1), builder.literal(1));
        ans = builder.literal(node);
        return ans;
    }

    /**
     * 处理非运算
     * @param expr
     * @return
     */
    private RexNode dealWithNotExpression(NotExpression expr) {
//        System.out.println("enter dealWithNotExpression function");
        RexNode ans = null;
        RexNode value = dealWithExpression(expr.getValue());
        ans = builder.not(value);
        return ans;
    }

    /**
     * 处理一元运算
     * @param expr
     * @return
     */
    private RexNode dealWithArithmeticUnaryExpression(ArithmeticUnaryExpression expr) {
//        System.out.println("enter dealWithArithmeticUnaryExpression function");
        RexNode ans = null;
        RexNode value = dealWithExpression(expr.getValue());
        if (expr.getSign() == ArithmeticUnaryExpression.Sign.MINUS) {
            ans = builder.call(SqlStdOperatorTable.MINUS, builder.literal(0), value);
        } else {
            ans = value;
        }
        return ans;
    }

    /**
     * 处理标识，暂时应该没有用到
     * @param expr
     * @return
     */
    private RexNode dealWithIdentifier(Identifier expr) {
//        System.out.println("enter dealWithIdentifier function");
        RexNode ans = null;
        ans = builder.literal(expr.getIdentifier());
        return ans;
    }

    /**
     * 处理常量表达式
     * 区分整数、浮点数和字符型三种常量
     * @param expr
     * @return
     */
    private RexNode dealWithConstantExpression(ConstantExpression expr) {
//        System.out.println("enter dealWithConstantExpression function");
        RexNode ans = null;
        if (isInteger(expr.getValue())) {
            ans = builder.literal(Integer.parseInt(expr.getValue()));
        } else if (isNumeric(expr.getValue())) {
            ans = builder.literal(Double.parseDouble(expr.getValue()));
        } else {
            ans = builder.literal(expr.getValue());
        }
        return ans;
    }

    /**
     * 处理括号表达式，去掉括号就行
     * @param expr
     * @return
     */
    private RexNode dealWithParenthesizedExpression(ParenthesizedExpression expr) {
//        System.out.println("enter dealWithParenthesizedExpression function");
        RexNode ans = null;
        ans = dealWithExpression(expr.getExpression());
        return ans;
    }

    /**
     * 处理二元计算表达式（加减乘除模）
     * @param expr
     * @return
     */
    private RexNode dealWithArithmeticBinaryExpression(ArithmeticBinaryExpression expr) {
//        System.out.println("enter dealWithArithmeticBinaryExpression function");
        RexNode ans = null;
        RexNode left = dealWithExpression(expr.getLeft());
        RexNode right = dealWithExpression(expr.getRight());
        SqlOperator op = SqlStdOperatorTable.PLUS;
        boolean flag = true;
        if (left instanceof RexLiteral && right instanceof RexLiteral) {
            flag = false;
        }
        switch (expr.getOperator()) {
            case ADD:
                op = SqlStdOperatorTable.PLUS;
                if (flag) {
                    ans = builder.call(op, left, right);
                } else {
                    ans = builder.literal(Double.parseDouble(left.toString()) + Double.parseDouble(right.toString()));
                }
                break;
            case SUBTRACT:
                op = SqlStdOperatorTable.MINUS;
                if (flag) {
                    ans = builder.call(op, left, right);
                } else {
                    ans = builder.literal(Double.parseDouble(left.toString()) - Double.parseDouble(right.toString()));
                }
                break;
            case MULTIPLY:
                op = SqlStdOperatorTable.MULTIPLY;
                if (flag) {
                    ans = builder.call(op, left, right);
                } else {
                    ans = builder.literal(Double.parseDouble(left.toString()) * Double.parseDouble(right.toString()));
                }
                break;
            case DIVIDE:
                op = SqlStdOperatorTable.DIVIDE;
                if (flag) {
                    ans = builder.call(op, left, right);
                } else {
                    ans = builder.literal(Double.parseDouble(left.toString()) / Double.parseDouble(right.toString()));
                }
                break;
            case MODULUS:
                op = SqlStdOperatorTable.MOD;
                if (flag) {
                    ans = builder.call(op, left, right);
                } else {
                    ans = builder.literal(Integer.parseInt(left.toString()) % Integer.parseInt(right.toString()));
                }
                break;
            default:
                break;
        }
        return ans;
    }

    /**
     * 处理属性表达式
     * @param expr
     * @return
     */
    private RexNode dealWithDereferenceExpression(DereferenceExpression expr) {
//        System.out.println("enter dealDereferenceExpression function");
        RexNode ans = null;
        String tableName = ((Identifier) expr.getBase()).getIdentifier();
        String fieldName = expr.getFieldName();

        // 如果只有一个Table，则直接取属性就行
        if (!multiTableList.contains("::")) {
            ans = builder.field(tableName+"."+fieldName);
        } else {
            // 如果有多个Table，则要写明是第几个表的属性
            int pos = multiTableList.indexOf(tableName+"."+fieldName);
            int inputOrdinal = 0;
            int inputCount = 1;
            for (int i = 0; i < multiTableList.size(); i++) {
                if (multiTableList.get(i) == "::") {
                    inputCount++;
                    if (i < pos) {
                        inputOrdinal++;
                    }
                }
            }

            ans = builder.field(inputCount, inputOrdinal, tableName+"."+fieldName);
        }

//        System.out.println(ans.toString() + " : [" + tableName+"."+fieldName+"]");

//        if (metadata.getMarkIDFieldNameMap().containsKey(ans.toString())) {
//            metadata.getMarkIDFieldNameMap().replace(ans.toString(), tableName+"."+fieldName);
//        } else {
//            metadata.getMarkIDFieldNameMap().put(ans.toString(), tableName+"."+fieldName);
//        }
        return ans;
    }


    /**
     * 处理比较表达式
     * @param expr
     * @return
     */
    private RexNode dealWithComparisonExpression(ComparisonExpression expr) {
//        System.out.println("enter dealWithComparisonExpression function");
        RexNode ans = null;
        RexNode left = dealWithExpression(expr.getLeft());
        RexNode right = dealWithExpression(expr.getRight());
        SqlBinaryOperator op = SqlStdOperatorTable.EQUALS;
        switch (expr.getOperator()) {
            case EQUAL:
                op = SqlStdOperatorTable.EQUALS;
                break;
            case NOT_EQUAL:
                op = SqlStdOperatorTable.NOT_EQUALS;
                break;
            case GREATER_THAN:
                op = SqlStdOperatorTable.GREATER_THAN;
                break;
            case GREATER_THAN_OR_EQUAL:
                op = SqlStdOperatorTable.GREATER_THAN_OR_EQUAL;
                break;
            case LESS_THAN:
                op = SqlStdOperatorTable.LESS_THAN;
                break;
            case LESS_THAN_OR_EQUAL:
                op = SqlStdOperatorTable.LESS_THAN_OR_EQUAL;
                break;
            default:
                break;
        }
        ans = builder.call(op, left, right);
        return ans;
    }

    /**
     * 处理聚合表达式
     * @param expr
     * @return
     */
    private RexNode dealWithFunctionCallExpression(FunctionCallExpression expr) {
//        System.out.println("enter dealWithFunctionCallExpression function");
        RexNode ans = null;
        SqlAggFunction op = SqlStdOperatorTable.SUM;
        String function = expr.getFunction();
        switch (function) {
            case "SUM":
                op = SqlStdOperatorTable.SUM;
                break;
            case "COUNT":
                op = SqlStdOperatorTable.COUNT;
                break;
            case "MAX":
                op = SqlStdOperatorTable.MAX;
                break;
            case "MIN":
                op = SqlStdOperatorTable.MIN;
            default:
                break;
        }
        List<RexNode> childs = new ArrayList<>();
        for (Expression subExpr : expr.getExpressions()) {
            childs.add(dealWithExpression(subExpr));
        }
        ans = builder.call(op, childs);
        return ans;
    }

    /**
     * 返回calcite风格的逻辑计划
     * @return
     */
    public RelNode getRoot() {
        return root;
    }

    /**
     * 配置config和schema的时候用到，没什么意义
     */
    private class ListTable extends AbstractTable implements ScannableTable {
        private RelDataType rowType;
        private List<Object[]> data;

        ListTable(RelDataType rowType, List<Object[]> data) {
            this.rowType = rowType;
            this.data = data;
        }

        @Override public Enumerable<Object[]> scan(final DataContext root) {
            return Linq4j.asEnumerable(data);
        }

        @Override public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
            return rowType;
        }

    }
}
