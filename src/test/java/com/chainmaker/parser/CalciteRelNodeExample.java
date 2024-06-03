//package com.chainmaker.parser;
//
//import org.apache.calcite.config.CalciteConnectionConfig;
//import org.apache.calcite.config.Lex;
//import org.apache.calcite.jdbc.CalciteConnection;
//import org.apache.calcite.jdbc.CalcitePrepare;
//import org.apache.calcite.plan.RelOptCluster;
//import org.apache.calcite.prepare.CalciteCatalogReader;
//import org.apache.calcite.prepare.CalcitePrepareImpl;
//import org.apache.calcite.prepare.CalciteSqlValidator;
//import org.apache.calcite.rel.RelNode;
//import org.apache.calcite.rel.type.*;
//import org.apache.calcite.runtime.CalciteContextException;
//import org.apache.calcite.runtime.CalciteException;
//import org.apache.calcite.runtime.Resources;
//import org.apache.calcite.schema.SchemaPlus;
//import org.apache.calcite.sql.*;
//import org.apache.calcite.sql.fun.SqlStdOperatorTable;
//import org.apache.calcite.sql.parser.SqlParseException;
//import org.apache.calcite.sql.parser.SqlParser;
//import org.apache.calcite.sql.validate.*;
//import org.apache.calcite.sql.validate.implicit.TypeCoercion;
//import org.apache.calcite.tools.FrameworkConfig;
//import org.apache.calcite.tools.Frameworks;
//import org.apache.calcite.tools.Planner;
//import org.apache.calcite.util.Pair;
//import org.checkerframework.checker.nullness.qual.Nullable;
//
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.function.UnaryOperator;
//
//public class CalciteRelNodeExample {
//    public static void main(String[] args) throws SQLException, SqlParseException {
//        Properties info = new Properties();
//        info.setProperty("lex", "JAVA");
//        CalciteConnection connection = (CalciteConnection) DriverManager.getConnection("jdbc:calcite:", info);
//        SchemaPlus rootSchema = connection.getRootSchema();
//
//        // 配置框架
//        FrameworkConfig config = Frameworks.newConfigBuilder()
//                .defaultSchema(rootSchema)
//                .parserConfig(SqlParser.configBuilder().setLex(Lex.JAVA).build())
//                .sqlValidatorConfig(null)
//                .build();
//
//        // 创建Planner
//        Planner planner = Frameworks.getPlanner(config);
//
//        // 解析SQL
//        SqlParser parser = SqlParser
//                .create("SELECT '1' AS socialid, cnt, tot_val FROM (SELECT COUNT(*), SUM(balance) FROM whh_security_1) tmp_inner",
//                SqlParser.config().withLex(Lex.JAVA));
//        SqlNode sqlNode = parser.parseQuery();
//
//        // 验证并准备SQL
//        CalciteConnectionConfig connectionConfig = CalciteConnectionConfig.DEFAULT;
//
//        // 设置SQL方言（这里以标准SQL为例）
//        SqlConformance conformance = SqlConformanceEnum.DEFAULT;
//
//        // 创建SqlValidatorConfig
//        SqlValidator.Config validatorConfig = SqlValidatorImpl.Config.DEFAULT;
//
//        // 使用配置创建SqlValidator
//        SqlValidator validator = SqlValidator;
//
//
////         将SQL转换为RelNode
////        CalcitePrepareImpl.Context context = CalcitePrepare.Dummy.
////                .schema(rootSchema)
////                .typeFactory(connection.getTypeFactory())
////                .parserConfig(config.getParserConfig())
////                .build();
////        Pair<RelOptCluster, RelNode> pair = context.(sqlNode);
////        RelOptCluster cluster = pair.left;
////        RelNode relNode = pair.right;
////
//        // 打印RelNode树
////        printRelNodeTree(relNode, "");
//    }
//
//    private static void printRelNodeTree(RelNode node, String indent) {
//        System.out.println(indent + node.getClass().getSimpleName() + " [" + node.getId() + "]");
//        for (RelNode input : node.getInputs()) {
//            printRelNodeTree(input, indent + "  ");
//        }
//    }
//}
