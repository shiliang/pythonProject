package com.chainmaker.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.ParserApplication;
import com.chainmaker.jobservice.api.controller.ParserController;
import com.chainmaker.jobservice.api.response.Result;
import com.chainmaker.jobservice.api.service.JobParserService;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest(classes = ParserApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class TestAllPql {

    @Autowired
    private JobParserService jobParserService;

    @Autowired
    ParserController parserController;

    @Autowired
    TestRestTemplate testTemplate;

//    @BeforeEach
//    public void setup(){
//        PlatformInfo platformInfo = new PlatformInfo();
//        platformInfo.setRegisterId(1);
//        platformInfo.setAccountName("1-c");
//        this.jobParserService.(platformInfo);
//    }

    public static final List<String> pqls = Lists.newArrayList(
            "SELECT ATEST.ID FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT /*+ JOIN(TEE) */ ATEST.ID FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT ATEST.K FROM ATEST WHERE ATEST.ID=?",
            "SELECT /*+ FILTER(TEE) */ATEST.K FROM ATEST WHERE ATEST.ID=?",
            "SELECT ATEST.K*BTEST.K FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT SUM(ATEST.K*BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT AVG(ATEST.K*BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT MAX(ATEST.K*BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT MIN(ATEST.K*BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT COUNT(ATEST.ID) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT /*+ FUNC(TEE) */ MUL(ATEST.K,BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT /*+ FUNC(TEE) */ MULSUM(ATEST.K,BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT /*+ FUNC(TEE) */ MULAVG(ATEST.K,BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT /*+ FUNC(TEE) */ MULMAX(ATEST.K,BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT /*+ FUNC(TEE) */ MULMIN(ATEST.K,BTEST.K) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT ID, (0.1 * ATEST.A1) + (0.2 * BTEST.B1) + (0.1 * ATEST.A2) + (0.4 * BTEST.B2) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID",
            "SELECT /*+ FUNC(TEE) */ SCORE(0.1, ATEST.A1, 0.2, BTEST.B2, 0.1, ATEST.A2, 0.4, BTEST.B2) FROM ATEST, BTEST WHERE ATEST.ID=BTEST.ID"
    );
    @Test
    public void pql() {
        String req = "{\"assetInfoList\":[{\"holderCompany\":\"ida2\",\"dataInfo\":{\"dbName\":\"asset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"k\",\"description\":\"\",\"isPrimaryKey\":1,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"int\",\"name\":\"a1\",\"description\":\"名称\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"int\",\"name\":\"a2\",\"description\":\"地址\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"name\":\"id\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"privacyQuery\":1}],\"tableName\":\"atest\"},\"scale\":\"2000条\",\"txId\":\"17bbaf383ac09a28ca60731891c3be39ef04b3fc414648eeab71fb4e9d6bf080\",\"cycle\":\"1分\",\"assetType\":1,\"assetNumber\":\"120240311000014376\",\"assetId\":\"37\",\"intro\":\"atest\",\"assetName\":\"atest\",\"uploadedAt\":\"2024-03-11 18:31:56\",\"assetEnName\":\"atest\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\"},{\"holderCompany\":\"ida1\",\"dataInfo\":{\"dbName\":\"asset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"name\":\"k\",\"description\":\"\",\"isPrimaryKey\":1,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"int\",\"name\":\"b1\",\"description\":\"名称\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"int\",\"name\":\"b2\",\"description\":\"地址\",\"isPrimaryKey\":0,\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"name\":\"id\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"privacyQuery\":1}],\"tableName\":\"btest\"},\"scale\":\"2000条\",\"txId\":\"17bbaf122cd2116cca2135fa7ee19dd565cb6f8aabe84a658c14997bde054372\",\"cycle\":\"1分\",\"assetType\":1,\"assetNumber\":\"320240311000015941\",\"assetId\":\"36\",\"intro\":\"btest\",\"assetName\":\"btest\",\"uploadedAt\":\"2024-03-11 18:29:37\",\"assetEnName\":\"btest\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\"}],\"sqltext\":\" select whh_enterprise_3.balance, tmp_table.socialid from whh_enterprise_3, whh_security_1,(select tmp_inner.* from (select * from whh_security_1 ) tmp_inner ) tmp_table where whh_enterprise_3.socialid\\u003d whh_security_1.socialid and tmp_table.socialid\\u003d whh_security_1.socialid\",\"isStream\":0,\"modelType\":0,\"orgInfo\":{\"orgId\":\"1\",\"orgName\":\"1-c\"}}";
        for(String pql: pqls){
            if(pqls.indexOf(pql) == 100){
                continue;
            }
            JSONObject json = JSON.parseObject(req);
            json.put("sqltext", pql);
            Result result = testTemplate.postForObject("/v1/commit/dag", json.toString().toUpperCase(), Result.class);
            System.out.println(result);
        }

    }
}
