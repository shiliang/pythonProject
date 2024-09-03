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
import java.util.Properties;

@Slf4j
public class TestStreamPql {

    public static final List<String> pqls = Lists.newArrayList(
            //非法写法
//            "select atest_1.k, atest_1.a1 from atest_1 where atest_1.id= ? ",

            //软件方案支持的流式计算如下：
            "set t1 = ?;\n" +
            "set t2 = ?;\n" +
            "set t3 = ?;\n" +
            "set atest_1.a1.noise = {\"algo\": \"\", \"epsilon\": \"\", \"sensitivity\": \"\", \"delta\": \"\"};" +
            "SELECT  (2 * t1 * (btest_2.b2 + atest_1.a1) + 2 * (atest_1.a1 +atest_1.a1)) * btest_2.b2 FROM atest_1, btest_2 WHERE atest_1.id = t2;\n",

            "set t1 = ?; select atest_1.k, atest_1.a1 from atest_1 where atest_1.id= t1 ",

            //硬件目前支持的流式计算如下：
            "set t1 = ?; select /*+ FILTER(TEE) */ atest_1.k, atest_1.a1 from atest_1 where atest_1.id= t1",

//            "set t1 = ?; select /*+ FUNC(TEE) */ SCORE(atest_1.a1, btest_2.b2) from atest_1, btest_2 where atest_1.id = t1 ",

            "set t2 = ?; SELECT /*+ FULLY_COV(TEE) */ SCORE(atest_1.a1, t2, btest_2.b1) FROM atest_1, btest_2 WHERE atest_1.id= t2 ",
""
    );


    public static void doSomething4log(){
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger calciteLogger = context.getLogger("org.apache.calcite");
        calciteLogger.setLevel(Level.INFO);
    }

    public static void main(String[] args) {
        doSomething4log();
        String req = "{\"assetInfoList\":[{\"holderCompany\":\"ida1\",\"dataInfo\":{\"dbName\":\"asset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"k\",\"description\":\"\",\"isPrimaryKey\":1,\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"a1\",\"description\":\"名称\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"a2\",\"description\":\"地址\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"name\":\"id\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"privacyQuery\":1}],\"tableName\":\"atest\"},\"scale\":\"2000条\",\"txId\":\"17bbaf383ac09a28ca60731891c3be39ef04b3fc414648eeab71fb4e9d6bf080\",\"cycle\":\"1分\",\"assetType\":1,\"assetNumber\":\"120240311000014376\",\"assetId\":\"37\",\"intro\":\"atest\",\"assetName\":\"atest_1\",\"uploadedAt\":\"2024-03-11 18:31:56\",\"assetEnName\":\"atest_1\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\"},{\"holderCompany\":\"ida2\",\"dataInfo\":{\"dbName\":\"asset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"k\",\"description\":\"\",\"isPrimaryKey\":1,\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"b1\",\"description\":\"名称\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"b2\",\"description\":\"地址\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"name\":\"id\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"privacyQuery\":1}],\"tableName\":\"btest\"},\"scale\":\"2000条\",\"txId\":\"17bbaf122cd2116cca2135fa7ee19dd565cb6f8aabe84a658c14997bde054372\",\"cycle\":\"1分\",\"assetType\":1,\"assetNumber\":\"320240311000015941\",\"assetId\":\"36\",\"intro\":\"btest\",\"assetName\":\"btest_2\",\"uploadedAt\":\"2024-03-11 18:29:37\",\"assetEnName\":\"btest_2\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\"},{\"holderCompany\":\"ida3\",\"dataInfo\":{\"dbName\":\"csset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"c3\",\"description\":\"地址\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"name\":\"id\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"privacyQuery\":1}],\"tableName\":\"ctest\"},\"scale\":\"2000条\",\"txId\":\"17bbaf383ac09a28ca60731891c3be39ef04b3fc414648eeab71fb4e9d6bf080\",\"cycle\":\"1分\",\"assetType\":1,\"assetNumber\":\"120240311000014376\",\"assetId\":\"78\",\"intro\":\"ctest\",\"assetName\":\"ctest_3\",\"uploadedAt\":\"2024-03-11 18:31:56\",\"assetEnName\":\"ctest_3\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\"}],\"sqltext\":\" select whh_enterprise_3.balance, tmp_table.socialid from whh_enterprise_3, whh_security_1,(select tmp_inner.* from (select * from whh_security_1 ) tmp_inner ) tmp_table where whh_enterprise_3.socialid = whh_security_1.socialid and tmp_table.socialid = whh_security_1.socialid\",\"isStream\":1,\"modelType\":0,\"orgInfo\":{\"orgId\":\"1\",\"orgName\":\"1-c\"}}";

        JobParserServiceImpl jobParser = new JobParserServiceImpl();
        for(String pql: pqls){
            SqlVo sqlVo = JSONObject.parseObject(req, SqlVo.class, Feature.OrderedField);
            sqlVo.setSqltext(pql);
            Job job = new Job();
            try {
                job = jobParser.jobPreview(sqlVo);
                log.info("result: " + JSON.toJSONString(job, SerializerFeature.PrettyFormat,SerializerFeature.DisableCircularReferenceDetect));
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }

            String storeKey = pql.replace("\n","");
            CustomDelimiterKVStore.put(storeKey, JSON.toJSONString(job.getServiceList()));
        }
        CustomDelimiterKVStore.saveToFile(getStoreInstantFile());
    }

    public static String getStoreInstantFile(){
        String time = CustomDelimiterKVStore.formatTimestamp(System.currentTimeMillis());
        String localFile = "store_stream";
        return localFile + "_" + time + ".txt";
    }

    public static String getStoreBaseFile(){
        return "store_stream.txt";
    }
}
