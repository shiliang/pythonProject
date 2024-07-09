package com.chainmaker.sqlrewrite;

import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.Context;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.PrestoSqlDialect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlString;
import org.apache.calcite.tools.Frameworks;

public class TestSqlRewrite {

    public static void main(String[] args) throws SqlParseException {
        String sql = "select * from table_a , table_b where table_a.id=table_b.id";
        SqlParser parser = SqlParser.create(sql, SqlParser.Config.DEFAULT);
        SqlNode sqlNode = parser.parseStmt();
        SqlString sql_str = sqlNode.toSqlString(PrestoSqlDialect.DEFAULT);
        System.out.println(sql_str);


    }
}
