package com.chainmaker.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.ParserApplication;
import com.chainmaker.jobservice.api.controller.ParserController;
import com.chainmaker.jobservice.api.model.job.Job;
import com.chainmaker.jobservice.api.model.vo.SqlVo;
import com.chainmaker.jobservice.api.response.Result;
import com.chainmaker.jobservice.api.service.JobParserService;
import com.chainmaker.jobservice.api.service.impl.JobParserServiceImpl;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

public class TestAllPqlV2 {
//    public static final List<String> pqls = Lists.newArrayList(
//            "SELECT ATEST.ID FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT /*+ JOIN(TEE) */ ATEST.ID FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT ATEST.K FROM ATEST WHERE ATEST.ID=?",
//            "SELECT /*+ FILTER(TEE) */ATEST.K FROM ATEST WHERE ATEST.ID=?",
//            "SELECT ATEST.K*BTEST.K FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT SUM(ATEST.K*BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT AVG(ATEST.K*BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT MAX(ATEST.K*BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT MIN(ATEST.K*BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT COUNT(ATEST.ID) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT /*+ FUNC(TEE) */ MUL(ATEST.K,BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT /*+ FUNC(TEE) */ MULSUM(ATEST.K,BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT /*+ FUNC(TEE) */ MULAVG(ATEST.K,BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT /*+ FUNC(TEE) */ MULMAX(ATEST.K,BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT /*+ FUNC(TEE) */ MULMIN(ATEST.K,BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT ID, (0.1 * ATEST.A1) + (0.2 * BTEST.B1) + (0.1 * ATEST.A2) + (0.4 * BTEST.B2) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
//            "SELECT /*+ FUNC(TEE) */ SCORE(0.1, ATEST.A1, 0.2, BTEST.B2, 0.1, ATEST.A2, 0.4, BTEST.B2) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID"
//    );

    public static final List<String> pqls = Lists.newArrayList(
            "select atest.id from atest, btest where atest.id=btest.id",
            "select /*+ JOIN(TEE) */ atest.id from atest, btest where atest.id=btest.id",
            "select atest.k from atest where atest.id=?",
            "select /*+ FILTER(TEE) */atest.k from atest where atest.id=?",
            "select atest.k*btest.k from atest, btest where atest.id=btest.id",
            "select SUM(atest.k*btest.k) from atest, btest where atest.id=btest.id",
            "select AVG(atest.k*btest.k) from atest, btest where atest.id=btest.id",
            "select MAX(atest.k*btest.k) from atest, btest where atest.id=btest.id",
            "select MIN(atest.k*btest.k) from atest, btest where atest.id=btest.id",
            "select COUNT(atest.id) from atest, btest where atest.id=btest.id",
            "select /*+ FUNC(TEE) */ MUL(atest.k,btest.k) from atest, btest where atest.id=btest.id",
            "select /*+ FUNC(TEE) */ MULSUM(atest.k,btest.k) from atest, btest where atest.id=btest.id",
            "select /*+ FUNC(TEE) */ MULAVG(atest.k,btest.k) from atest, btest where atest.id=btest.id",
            "select /*+ FUNC(TEE) */ MULMAX(atest.k,btest.k) from atest, btest where atest.id=btest.id",
            "select /*+ FUNC(TEE) */ MULMIN(atest.k,btest.k) from atest, btest where atest.id=btest.id",
            "select atest.id, (0.1 * atest.a1) + (0.2 * btest.b1) + (0.1 * atest.a2) + (0.4 * btest.b2) from atest, btest where atest.id=btest.id",
            "select /*+ FUNC(TEE) */ SCORE(0.1, atest.a1, 0.2, btest.b2, 0.1, atest.a2, 0.4, btest.b2) from atest, btest where atest.id=btest.id",
            "select atest.a1, tmp_table.id from atest, btest,(select id, cnt, tot_val from (select id, count(a1) as cnt, sum(a1) as tot_val from atest group by id ) tmp_inner ) tmp_table where atest.id= btest.id and tmp_table.id= btest.id",
    "select atest.a1, tmp_table1.id, tmp_table2.id from atest,( select id, count(b2) as cnt, sum(b2) as tot_val from btest group by id) tmp_table1, ( select id, count(a1) as cnt, sum(a1) as tot_val from atest group by id ) tmp_table2 where atest.id= tmp_table1.id and tmp_table1.id= tmp_table2.id",
    "select atest.a1, tmp_table.id*2 + 1 from atest, btest,( select id, cnt, tot_val from ( select id, count(a1) as cnt, sum(a1) as tot_val from atest group by id) tmp_inner ) tmp_table where atest.id= btest.id and tmp_table.id= btest.id",
    "select atest.a1, tmp_table1.id*2 + tmp_table2.id*8 from atest,( select id, count(b2) as cnt, sum(b2) as tot_val from btest group by id) tmp_table1, ( select id, count(a1) as cnt, sum(a1) as tot_val from atest group by id ) tmp_table2 where atest.id= tmp_table1.id and tmp_table1.id= tmp_table2.id"
    );
    @Test
    public void pql() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger calciteLogger = context.getLogger("org.apache.calcite");
        calciteLogger.setLevel(Level.INFO);

        String req = "{\"assetInfoList\":[{\"holderCompany\":\"ida2\",\"dataInfo\":{\"dbName\":\"asset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"k\",\"description\":\"\",\"isPrimaryKey\":1,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"int\",\"name\":\"a1\",\"description\":\"名称\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"int\",\"name\":\"a2\",\"description\":\"地址\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"name\":\"id\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"privacyQuery\":1}],\"tableName\":\"atest\"},\"scale\":\"2000条\",\"txId\":\"17bbaf383ac09a28ca60731891c3be39ef04b3fc414648eeab71fb4e9d6bf080\",\"cycle\":\"1分\",\"assetType\":1,\"assetNumber\":\"120240311000014376\",\"assetId\":\"37\",\"intro\":\"atest\",\"assetName\":\"atest\",\"uploadedAt\":\"2024-03-11 18:31:56\",\"assetEnName\":\"atest\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\"},{\"holderCompany\":\"ida1\",\"dataInfo\":{\"dbName\":\"asset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"k\",\"description\":\"\",\"isPrimaryKey\":1,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"int\",\"name\":\"b1\",\"description\":\"名称\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"int\",\"name\":\"b2\",\"description\":\"地址\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"name\":\"id\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"privacyQuery\":1}],\"tableName\":\"btest\"},\"scale\":\"2000条\",\"txId\":\"17bbaf122cd2116cca2135fa7ee19dd565cb6f8aabe84a658c14997bde054372\",\"cycle\":\"1分\",\"assetType\":1,\"assetNumber\":\"320240311000015941\",\"assetId\":\"36\",\"intro\":\"btest\",\"assetName\":\"btest\",\"uploadedAt\":\"2024-03-11 18:29:37\",\"assetEnName\":\"btest\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\"}],\"sqltext\":\" select whh_enterprise_3.balance, tmp_table.socialid from whh_enterprise_3, whh_security_1,(select tmp_inner.* from (select * from whh_security_1 ) tmp_inner ) tmp_table where whh_enterprise_3.socialid\\u003d whh_security_1.socialid and tmp_table.socialid\\u003d whh_security_1.socialid\",\"isStream\":0,\"modelType\":0,\"orgInfo\":{\"orgId\":\"1\",\"orgName\":\"1-c\"}}";
        JobParserServiceImpl jobParser = new JobParserServiceImpl();
        for(String pql: pqls){
            SqlVo sqlVo = JSONObject.parseObject(req, SqlVo.class, Feature.OrderedField);
            sqlVo.setSqltext(pql);
            System.out.println(sqlVo);
            Job job = jobParser.jobPreview(sqlVo);
            System.out.println(JSON.toJSONString(job));
        }

    }
}
