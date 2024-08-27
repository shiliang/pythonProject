package com.chainmaker.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chainmaker.jobservice.api.model.job.Job;
import com.chainmaker.jobservice.api.model.vo.SqlVo;
import com.chainmaker.jobservice.api.service.impl.JobParserServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.util.List;

@Slf4j
public class TestMPCPql {

    /**
     * MPCProject不能支持别名，因为表达式都存储rowtype中，一旦允许别名，rowtype中的信息就变为别名字段了。
     * 分析了calcite的relNode, 没有找到存放表达式的字段。应该是保留在SqlNode中了。
     * 在RelNode中，表达式经过转化，变成了RexNode，结构过于繁杂，处理麻烦。
     */
    public static final List<String> pqls = Lists.newArrayList(
//            "select count(atest_1.k) as field1 from atest_1",
            "select count(atest_1.k), max(atest_1.k), avg(atest_1.k) from atest_1 ",

            "select counnt(atest_1.k), max(atest_1.k), avg(atest_1.k) from atest_1 ",

            "select /*+ FUNC(TEE) */ JSB01(atest_1.k,btest_2.k) from atest_1, btest_2",

            "select temp.a1 from (select atest_1.a1 from atest_1) temp ",
            "select atest_1.id from atest_1, btest_2 where atest_1.id=btest_2.id",
            "select /*+ JOIN(TEE) */ atest_1.id from atest_1, btest_2 where atest_1.id=btest_2.id",
            "select atest_1.k from atest_1 where atest_1.id=?",
            "select /*+ FILTER(TEE) */atest_1.k from atest_1 where atest_1.id=?",
            "select 2 * atest_1.k*btest_2.k + 3 * atest_1.a1*btest_2.b1 from atest_1, btest_2 where atest_1.id=btest_2.id"
            ,
            "select SUM(atest_1.k*btest_2.k) from atest_1, btest_2 where atest_1.id=btest_2.id",
            "select AVG(atest_1.k*btest_2.k) from atest_1, btest_2 where atest_1.id=btest_2.id",
            "select MAX(atest_1.k*btest_2.k) from atest_1, btest_2 where atest_1.id=btest_2.id",
            "select MIN(atest_1.k*btest_2.k) from atest_1, btest_2 where atest_1.id=btest_2.id",
            "select COUNT(atest_1.id) from atest_1, btest_2 where atest_1.id=btest_2.id",
            "select /*+ FUNC(TEE) */ MUL(atest_1.k,btest_2.k) from atest_1, btest_2 where atest_1.id=btest_2.id",
            "select /*+ FUNC(TEE) */ MULSUM(atest_1.k,btest_2.k) from atest_1, btest_2 where atest_1.id=btest_2.id",
            "select /*+ FUNC(TEE) */ MULAVG(atest_1.k,btest_2.k) from atest_1, btest_2 where atest_1.id=btest_2.id",
            "select /*+ FUNC(TEE) */ MULMAX(atest_1.k,btest_2.k) from atest_1, btest_2 where atest_1.id=btest_2.id",
            "select /*+ FUNC(TEE) */ MULMIN(atest_1.k,btest_2.k) from atest_1, btest_2 where atest_1.id=btest_2.id",

            "select atest_1.id, (0.1 * atest_1.a1) + (0.2 * btest_2.b1) + (0.1 * atest_1.a2) + (0.4 * btest_2.b2) from atest_1, btest_2 where atest_1.id=btest_2.id",
            "select /*+ FUNC(TEE) */ SCORE(0.1, atest_1.a1, 0.2, btest_2.b2, 0.1, atest_1.a2, 0.4, btest_2.b2) from atest_1, btest_2 where atest_1.id=btest_2.id",

            "select atest_1.a1, tmp_table.id from atest_1, btest_2,(select id, cnt, tot_val from (select id, count(a1) as cnt, sum(a1) as tot_val from atest_1 group by id ) tmp_inner ) tmp_table where atest_1.id= btest_2.id and tmp_table.id= btest_2.id"
            ,
            "select atest_1.a1, tmp_table1.id, tmp_table2.id from atest_1,( select id, count(b2) as cnt, sum(b2) as tot_val from btest_2 group by id) tmp_table1, ( select id, count(a1) as cnt, sum(a1) as tot_val from atest_1 group by id ) tmp_table2 where atest_1.id= tmp_table1.id and tmp_table1.id= tmp_table2.id",
            "select atest_1.a1, tmp_table.id * 2 + btest_2.b2 from atest_1, btest_2,( select id, cnt, tot_val from ( select id, count(a1) as cnt, sum(a1) as tot_val from atest_1 group by id) tmp_inner ) tmp_table where atest_1.id= btest_2.id and tmp_table.id= btest_2.id",
            "select atest_1.k + tmp_table1.id*2 + tmp_table2.id*8 from atest_1,( select id, count(b2) as cnt, sum(b2) as tot_val from btest_2 group by id) tmp_table1, ( select id, count(a1) as cnt, sum(a1) as tot_val from atest_1 group by id ) tmp_table2 where atest_1.id= tmp_table1.id and tmp_table1.id= tmp_table2.id",

//            "select fl(is_train = true, is_test = false, fllabel( source_data = atest_1, with_label = true, label_type = int, output_format = dense, namespace = experiment), fllabel( source_data = btest_2, with_label = false, output_format = dense, namespace = experiment ), intersection( intersect_method = rsa ), helr( penalty = l2, tol = 0.0001, alpha = 0.01, optimizer = rmsprop, batch_size =-1, learning_rate = 0.15, init_param.init_method = zeros, init_param.fit_intercept = true, max_iter = 15, early_stop = diff, encrypt_param.key_length = 1024, reveal_strategy = respectively, reveal_every_iter = true ), eval(eval_type = binary) ) from atest_1, btest_2"
//            ,
//            "select fl(is_train = true, is_test = false,fllabel(source_data = atest_1, with_label = true, namespace = experiment),fllabel(source_data = btest_2, with_label = false, namespace = experiment),intersection(intersect_method = rsa),hesb(task_type = classification, num_trees = 3, tree_param.max_depth = 3), eval(eval_type = binary)) from atest_1, btest_2",
//            "select fl(is_train = false, is_test = true,model_id=\"9f6c60546fba0c42072373277b0\",fllabel(source_data = atest_1, with_label = true, namespace = experiment),fllabel(source_data = btest_2, with_label = false, namespace = experiment),intersection(intersect_method = rsa),hesb(task_type = classification, num_trees = 3, tree_param.max_depth = 3), eval(eval_type = binary)) from atest_1,btest_2"
//            ,
            "SELECT /*+ JOIN(FL) */ SEQUENCE(FE(model_name=HESCALE, features=[atest_1.*,btest_2.*]),TRAIN(model_name='HELR',label=btest_2.k, features= [atest_1.*,btest_2.*])) From atest_1, btest_2 WHERE atest_1.id = btest_2.id",
            "SELECT SEQUENCE(FE(model_name=HESCALE, features=[atest_1.*,btest_2.*]),TRAIN(model_name='HELR',label=btest_2.k, features= [atest_1.*,btest_2.*])) From atest_1, btest_2 WHERE atest_1.id = btest_2.id",
            "SELECT FE(model_name=HESCALE, features=[atest_1.*,btest_2.*]),TRAIN(model_name='HELR',label=btest_2.k, features= [atest_1.*,btest_2.*]) From atest_1, btest_2 WHERE atest_1.id = btest_2.id",
            "SELECT /*+ JOIN(FL) */ TRAIN(model_name='HELR',label=btest_2.k, features= [atest_1.*,btest_2.*]) From atest_1, btest_2 WHERE atest_1.id = btest_2.id",
            "SELECT TRAIN(model_name='HELR',label=btest_2.k, features= [atest_1.*,btest_2.*]) From atest_1, btest_2 WHERE atest_1.id = btest_2.id"

    );


    public static void main(String[] args) {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger calciteLogger = context.getLogger("org.apache.calcite");
        calciteLogger.setLevel(Level.INFO);

        String req = "{\"assetInfoList\":[{\"holderCompany\":\"ida1\",\"dataInfo\":{\"dbName\":\"asset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"k\",\"description\":\"\",\"isPrimaryKey\":1,\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"a1\",\"description\":\"名称\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"a2\",\"description\":\"地址\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"name\":\"id\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"privacyQuery\":1}],\"tableName\":\"atest\"},\"scale\":\"2000条\",\"txId\":\"17bbaf383ac09a28ca60731891c3be39ef04b3fc414648eeab71fb4e9d6bf080\",\"cycle\":\"1分\",\"assetType\":1,\"assetNumber\":\"120240311000014376\",\"assetId\":\"37\",\"intro\":\"atest\",\"assetName\":\"atest_1\",\"uploadedAt\":\"2024-03-11 18:31:56\",\"assetEnName\":\"atest_1\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\"},{\"holderCompany\":\"ida2\",\"dataInfo\":{\"dbName\":\"asset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"k\",\"description\":\"\",\"isPrimaryKey\":1,\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"b1\",\"description\":\"名称\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"b2\",\"description\":\"地址\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"name\":\"id\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"privacyQuery\":1}],\"tableName\":\"btest\"},\"scale\":\"2000条\",\"txId\":\"17bbaf122cd2116cca2135fa7ee19dd565cb6f8aabe84a658c14997bde054372\",\"cycle\":\"1分\",\"assetType\":1,\"assetNumber\":\"320240311000015941\",\"assetId\":\"36\",\"intro\":\"btest\",\"assetName\":\"btest_2\",\"uploadedAt\":\"2024-03-11 18:29:37\",\"assetEnName\":\"btest_2\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\"}],\"sqltext\":\" select whh_enterprise_3.balance, tmp_table.socialid from whh_enterprise_3, whh_security_1,(select tmp_inner.* from (select * from whh_security_1 ) tmp_inner ) tmp_table where whh_enterprise_3.socialid\\u003d whh_security_1.socialid and tmp_table.socialid\\u003d whh_security_1.socialid\",\"isStream\":0,\"modelType\":0,\"orgInfo\":{\"orgId\":\"1\",\"orgName\":\"1-c\"}}";
//        log.info("request: " + JSONObject.parseObject(req).toString(SerializerFeature.PrettyFormat));
        JobParserServiceImpl jobParser = new JobParserServiceImpl();
        for(String pql: pqls){
            SqlVo sqlVo = JSONObject.parseObject(req, SqlVo.class, Feature.OrderedField);
            sqlVo.setSqltext(pql);
//            log.info("request: " + JSON.toJSONString(sqlVo, SerializerFeature.PrettyFormat));
            try {
                Job job = jobParser.jobPreview(sqlVo);
                log.info("result: " + JSON.toJSONString(job, SerializerFeature.PrettyFormat,SerializerFeature.DisableCircularReferenceDetect));
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }

    }
}
