package com.chainmaker.jobservice.api.sqlrewrite;


import com.chainmaker.jobservice.api.builder.CalciteUtil;
import com.chainmaker.jobservice.core.calcite.relnode.MPCTableScan;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.calcite.adapter.csv.CsvSchema;
import org.apache.calcite.adapter.csv.CsvTable;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.plan.volcano.RelSubset;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.pretty.SqlPrettyWriter;
import org.apache.calcite.sql.util.SqlString;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.calcite.tools.Frameworks;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class CalciteRelOps {

    public static Map<String, String> replaceMap = Maps.newHashMap();
    public static void main( String[] args ) throws Exception {

        String sql = "SELECT o.id, o.goods, o.price, o.amount, c.firstname, c.lastname FROM orders AS o LEFT OUTER JOIN consumers c ON o.user_id = c.id WHERE o.amount > 30 ORDER BY o.id LIMIT 5";
        SqlParser.Config config = SqlParser.configBuilder().setCaseSensitive(false).build();
        SqlParser parser = SqlParser.create(sql, config);
        SqlNode sqlNodeParsed = parser.parseQuery();
        System.out.println("[parsed sqlNode]");
        System.out.println(sqlNodeParsed);


        SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        String csvPath = "D:\\chainweaver\\mira-job-service\\src\\main\\resources\\csv\\";
        CsvSchema csvSchema = new CsvSchema(new File(csvPath), CsvTable.Flavor.SCANNABLE);


//        rootSchema.add("csv", csvSchema);
        rootSchema.add("orders", csvSchema.getTable("orders"));
        rootSchema.add("consumers", csvSchema.getTable("consumers"));

        JavaTypeFactoryImpl sqlTypeFactory = new JavaTypeFactoryImpl();
        Properties properties = new Properties();
        properties.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "false");
        // reader 接收 schema，用于检测字段名、字段类型、表名等是否存在和一致
        CalciteCatalogReader catalogReader = new CalciteCatalogReader(
                CalciteSchema.from(rootSchema),
                CalciteSchema.from(rootSchema).path(null),
                sqlTypeFactory,
                new CalciteConnectionConfigImpl(properties));
        // 简单示例，大部分参数采用默认值即可
        SqlValidator validator = SqlValidatorUtil.newValidator(
                SqlStdOperatorTable.instance(),
                catalogReader,
                sqlTypeFactory,
                SqlValidator.Config.DEFAULT);
        // validate: SqlNode -> SqlNode
        SqlNode sqlNodeValidated = validator.validate(sqlNodeParsed);
        System.out.println();
        System.out.println("[validated sqlNode]");
        System.out.println(sqlNodeValidated);

        SqlDialect sqlDialect = SqlDialect.DatabaseProduct.SPARK.getDialect();

        SqlString sqlString = sqlNodeValidated.toSqlString(sqlDialect);
        System.out.println("convert to " + sqlString);




        RexBuilder rexBuilder = new RexBuilder(sqlTypeFactory);
        HepProgramBuilder hepProgramBuilder = new HepProgramBuilder();
        hepProgramBuilder.addRuleInstance(CoreRules.FILTER_INTO_JOIN);

        HepPlanner hepPlanner = new HepPlanner(hepProgramBuilder.build());
        hepPlanner.addRelTraitDef(ConventionTraitDef.INSTANCE);

        RelOptCluster relOptCluster = RelOptCluster.create(hepPlanner, rexBuilder);
        SqlToRelConverter sqlToRelConverter = new SqlToRelConverter(
                // 没有使用 view
                new RelOptTable.ViewExpander() {
                    @Override
                    public RelRoot expandView(RelDataType rowType, String queryString, List<String> schemaPath,  List<String> viewPath) {
                        return null;
                    }
                },
                validator,
                catalogReader,
                relOptCluster,
                // 均使用标准定义即可
                StandardConvertletTable.INSTANCE,
                SqlToRelConverter.config());
        RelRoot logicalPlan = sqlToRelConverter.convertQuery(sqlNodeValidated, false, true);

        System.out.println(RelOptUtil.dumpPlan("[Logical plan]", logicalPlan.rel, SqlExplainFormat.TEXT, SqlExplainLevel.NON_COST_ATTRIBUTES));

        hepPlanner.setRoot(logicalPlan.rel);
        RelNode phyPlan = hepPlanner.findBestExp();
        System.out.println(RelOptUtil.dumpPlan("[Physical plan]", phyPlan, SqlExplainFormat.TEXT, SqlExplainLevel.NON_COST_ATTRIBUTES));


        SqlWriterConfig writerConfig = SqlWriterConfig.of()
                .withDialect(sqlDialect)
                .withAlwaysUseParentheses(true) // 可选配置，控制括号的使用
                .withSelectListItemsOnSeparateLines(false) // 控制 SELECT 列表的格式
                ;
        // 使用配置创建 SqlWriter

        SqlPrettyWriter prettyWriter = new SqlPrettyWriter(writerConfig, new StringBuilder());
        RelToSqlConverter relToSqlConverter = new RelToSqlConverter(sqlDialect);
        SqlNode sqlNode = relToSqlConverter.visitRoot(phyPlan).asStatement();

        sqlNode.unparse(prettyWriter, 0, 0);
        String sqlStr = prettyWriter.toString();
        System.out.println(sqlStr);
    }

    public static String rel2sql(RelNode phyPlan){
        SqlDialect sqlDialect = SqlDialect.DatabaseProduct.SPARK.getDialect();

        SqlWriterConfig writerConfig = SqlWriterConfig.of()
                .withDialect(sqlDialect)
                .withQuoteAllIdentifiers(true)
                .withAlwaysUseParentheses(true) // 可选配置，控制括号的使用
                .withSelectListItemsOnSeparateLines(false) // 控制 SELECT 列表的格式
                ;
        SqlPrettyWriter prettyWriter = new SqlPrettyWriter(writerConfig, new StringBuilder());
        RelToSqlConverter relToSqlConverter = new RelToSqlConverter(sqlDialect);
        SqlNode sqlNode = relToSqlConverter.visitRoot(phyPlan).asStatement();
        sqlNode = correctField((SqlSelect) sqlNode);
        sqlNode.unparse(prettyWriter, 0, 0);

        return prettyWriter.toString();
    }

    public static SqlNode correctField(SqlSelect node){
        SqlNode from = node.getFrom();
        if(from instanceof SqlJoin){
           dealJoin((SqlJoin) from);
        }else if(from instanceof SqlBasicCall){
            dealSubQuery((SqlBasicCall) from);
        }else{

        }
        for(int i = 0; i < node.getSelectList().size(); i++){
            SqlNode ele = node.getSelectList().get(i);
            if(ele instanceof SqlIdentifier){
                SqlIdentifier field = (SqlIdentifier) ele;
                if(field.names.size() > 1) {
                    String name1 = field.names.get(0);
                    String name2 = field.names.get(1);
                    name2 = CalciteUtil.getColumnName(name2);
                    field.names = ImmutableList.<String>builder().addAll(Lists.newArrayList(name1, name2)).build();
                }
            }else if(ele instanceof SqlBasicCall){
                SqlBasicCall call = (SqlBasicCall)ele;
                if(call.getOperator() instanceof SqlAsOperator){
                    SqlNode operand = call.getOperandList().get(0);
                    if(operand instanceof SqlBasicCall){
                        node.getSelectList().set(i, operand);
                    }
                }
            }
        }
        return node;
    }

    public static void dealJoin(SqlJoin join){
        SqlNode left =  join.getLeft();
        SqlNode right = join.getRight();
        SqlBasicCall cond = (SqlBasicCall) join.getCondition();
        if(left instanceof SqlBasicCall) {
            SqlBasicCall leftCall = (SqlBasicCall)left;
            if (leftCall.getOperandList().stream().anyMatch(x -> x instanceof SqlSelect)) {
                dealSubQuery(leftCall);
            }
        }
        if(right instanceof SqlBasicCall){
            SqlBasicCall rightCall = (SqlBasicCall)right;
            if(rightCall.getOperandList().stream().anyMatch(x -> x instanceof SqlSelect)){
                dealSubQuery(rightCall);
            }
        }

        List<SqlNode> nodes = cond.getOperandList();
        for(int i=0; i < nodes.size(); i++){
            SqlIdentifier field = (SqlIdentifier)(nodes.get(i));
            String tableAlias = field.names.get(0);
            String fieldName =field.names.get(1);
            if(fieldName.contains(".")){
                fieldName = CalciteUtil.getColumnName(fieldName);
            }
            SqlIdentifier newField = field.setName(1, CalciteUtil.getColumnName(fieldName));
            if(replaceMap.containsKey(tableAlias)) {
                newField = newField.setName(0, replaceMap.get(tableAlias));
            }
            cond.setOperand(i, newField);
        }
    }

    public static String dealSelectAlias(SqlSelect select){
        String tableAlias = null;
        SqlNodeList selectList = select.getSelectList();
        for(int i = 0;  i < selectList.size(); i++ ){
            SqlNode node = selectList.get(i);
            if(node instanceof SqlBasicCall){
                SqlBasicCall exprCall = (SqlBasicCall)node;
                SqlNode sqlNode = exprCall.getOperandList().get(0);
                SqlIdentifier fieldIdentifier = (SqlIdentifier) exprCall.getOperandList().get(1);
                String fieldAlias = fieldIdentifier.toString();
                if(fieldAlias.contains(".")){
                    tableAlias = fieldAlias.split("\\.")[0];
                    fieldAlias = fieldAlias.split("\\.")[1];
                    if(sqlNode instanceof SqlCharStringLiteral) {
                        String val = ((SqlCharStringLiteral) sqlNode).toValue();
                        if (fieldAlias.equals(val)) {
                            selectList.set(i, fieldIdentifier.setName(0, fieldAlias));
                        }
                    }else {
                        exprCall.setOperand(1, fieldIdentifier.setName(0, fieldAlias));
                    }
                }
            }else if(node instanceof SqlIdentifier){
                SqlIdentifier identifier = (SqlIdentifier) node;
                String fieldName = identifier.names.get(0);
                selectList.set(i, identifier.setName(0, CalciteUtil.getColumnName(fieldName)));
            }
        }
        return tableAlias;
    }


    public static void dealSubQuery(SqlBasicCall call){
        SqlOperator op = call.getOperator();
        if(op instanceof SqlAsOperator){ //有内部子查询
            SqlSelect select =(SqlSelect) call.getOperandList().get(0);
            SqlIdentifier tableIdentifier = (SqlIdentifier) call.getOperandList().get(1);
            String tableAlias = tableIdentifier.toString();
            if(select.getFrom() instanceof SqlJoin){
                dealJoin((SqlJoin) select.getFrom());
            }
            if(select.getFrom() instanceof SqlBasicCall){ //多层嵌套子查询
                dealSubQuery((SqlBasicCall) select.getFrom());
            }
            String newTableAlias = dealSelectAlias(select);
            replaceMap.put(tableAlias, newTableAlias);
            call.setOperand(1, tableIdentifier.setName(0, newTableAlias));

        }
    }


    public static RelNode cleanRelSubSet(RelNode phyPlan){
        if(phyPlan instanceof MPCTableScan){
            return phyPlan;
        }
        if(phyPlan instanceof RelSubset){
            RelSubset subset = (RelSubset)phyPlan;
            return subset.getBest();
        }
        List<RelNode> inputs = phyPlan.getInputs();
        List<RelNode> newInputs = Lists.newArrayList();
        for(RelNode input : inputs){
            RelNode newInput = cleanRelSubSet(input);
            newInputs.add(newInput);
        }
        return phyPlan.copy(phyPlan.getTraitSet(), newInputs);
    }



}
