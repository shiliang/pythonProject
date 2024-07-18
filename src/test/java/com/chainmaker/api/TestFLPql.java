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
import org.slf4j.LoggerFactory;

import java.util.List;

public class TestFLPql {

    public static final List<String> pqls = Lists.newArrayList(
            "select fl(is_train = true, is_test = false, fllabel( source_data = atest, with_label = true, label_type = int, output_format = dense, namespace = experiment), fllabel( source_data = btest, with_label = false, output_format = dense, namespace = experiment ), intersection( intersect_method = rsa ), helr( penalty = l2, tol = 0.0001, alpha = 0.01, optimizer = rmsprop, batch_size =-1, learning_rate = 0.15, init_param.init_method = zeros, init_param.fit_intercept = true, max_iter = 15, early_stop = diff, encrypt_param.key_length = 1024, reveal_strategy = respectively, reveal_every_iter = true ), eval(eval_type = binary) ) from atest, btest"

    );


    public static void main(String[] args) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger calciteLogger = context.getLogger("org.apache.calcite");
        calciteLogger.setLevel(Level.INFO);

        String req = "{\"assetInfoList\":[{\"holderCompany\":\"ida2\",\"dataInfo\":{\"dbName\":\"asset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"k\",\"description\":\"\",\"isPrimaryKey\":1,\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"a1\",\"description\":\"名称\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"a2\",\"description\":\"地址\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"name\":\"id\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"privacyQuery\":1}],\"tableName\":\"atest\"},\"scale\":\"2000条\",\"txId\":\"17bbaf383ac09a28ca60731891c3be39ef04b3fc414648eeab71fb4e9d6bf080\",\"cycle\":\"1分\",\"assetType\":1,\"assetNumber\":\"120240311000014376\",\"assetId\":\"37\",\"intro\":\"atest\",\"assetName\":\"atest\",\"uploadedAt\":\"2024-03-11 18:31:56\",\"assetEnName\":\"atest\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\"},{\"holderCompany\":\"ida1\",\"dataInfo\":{\"dbName\":\"asset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"k\",\"description\":\"\",\"isPrimaryKey\":1,\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"b1\",\"description\":\"名称\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"b2\",\"description\":\"地址\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"name\":\"id\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"privacyQuery\":1}],\"tableName\":\"btest\"},\"scale\":\"2000条\",\"txId\":\"17bbaf122cd2116cca2135fa7ee19dd565cb6f8aabe84a658c14997bde054372\",\"cycle\":\"1分\",\"assetType\":1,\"assetNumber\":\"320240311000015941\",\"assetId\":\"36\",\"intro\":\"btest\",\"assetName\":\"btest\",\"uploadedAt\":\"2024-03-11 18:29:37\",\"assetEnName\":\"btest\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\"}],\"sqltext\":\" select whh_enterprise_3.balance, tmp_table.socialid from whh_enterprise_3, whh_security_1,(select tmp_inner.* from (select * from whh_security_1 ) tmp_inner ) tmp_table where whh_enterprise_3.socialid\\u003d whh_security_1.socialid and tmp_table.socialid\\u003d whh_security_1.socialid\",\"isStream\":0,\"modelType\":0,\"orgInfo\":{\"orgId\":\"1\",\"orgName\":\"1-c\"}}";
        System.out.println(JSONObject.parseObject(req).toString(SerializerFeature.PrettyFormat));
        JobParserServiceImpl jobParser = new JobParserServiceImpl();
        for(String pql: pqls){
            SqlVo sqlVo = JSONObject.parseObject(req, SqlVo.class, Feature.OrderedField);
            sqlVo.setSqltext(pql);
            System.out.println(sqlVo);
            Job job = jobParser.jobPreview(sqlVo);
            System.out.println(JSON.toJSONString(job, SerializerFeature.PrettyFormat,SerializerFeature.DisableCircularReferenceDetect));
        }

    }
}
