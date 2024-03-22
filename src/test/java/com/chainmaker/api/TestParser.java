package com.chainmaker.api;

import com.alibaba.fastjson.JSON;
import com.chainmaker.jobservice.api.ParserApplication;
import com.chainmaker.jobservice.api.controller.ParserController;
import com.chainmaker.jobservice.api.model.PlatformInfo;
import com.chainmaker.jobservice.api.model.vo.SqlVo;
import com.chainmaker.jobservice.api.response.Result;
import com.chainmaker.jobservice.api.service.JobParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ParserApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class TestParser {

    @Autowired
    private JobParserService jobParserService;

    @Autowired
    ParserController parserController;

    @Autowired
    TestRestTemplate testTemplate;

    @BeforeEach
    public void setup(){
        PlatformInfo platformInfo = new PlatformInfo();
        platformInfo.setRegisterId(1);
        platformInfo.setAccountName("1-c");
        this.jobParserService.setPlatformInfo(platformInfo);
    }


    @Test
    public void testCommit() {;
        String req = "{\n" +
                "    \"assetInfoList\": [\n" +
                "        {\n" +
                "            \"holderCompany\": \"ida2\",\n" +
                "            \"dataInfo\": {\n" +
                "                \"dbName\": \"asset\",\n" +
                "                \"itemList\": [\n" +
                "                    {\n" +
                "                        \"dataLength\": 0,\n" +
                "                        \"dataType\": \"int\",\n" +
                "                        \"name\": \"id\",\n" +
                "                        \"description\": \"\",\n" +
                "                        \"isPrimaryKey\": 1,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"name\",\n" +
                "                        \"description\": \"名称\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"adress\",\n" +
                "                        \"description\": \"地址\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"phone\",\n" +
                "                        \"description\": \"联系方式\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"purpose\",\n" +
                "                        \"description\": \"用途\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"int\",\n" +
                "                        \"name\": \"balance\",\n" +
                "                        \"description\": \"金额\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 0,\n" +
                "                        \"dataType\": \"date\",\n" +
                "                        \"name\": \"time\",\n" +
                "                        \"description\": \"时间\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"remarks\",\n" +
                "                        \"description\": \"备注\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"socialid\",\n" +
                "                        \"description\": \"社会id\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"tableName\": \"whh_security\"\n" +
                "            },\n" +
                "            \"scale\": \"2000条\",\n" +
                "            \"txId\": \"17bbaf383ac09a28ca60731891c3be39ef04b3fc414648eeab71fb4e9d6bf080\",\n" +
                "            \"cycle\": \"1分\",\n" +
                "            \"assetType\": 1,\n" +
                "            \"assetNumber\": \"120240311000014376\",\n" +
                "            \"assetId\": \"37\",\n" +
                "            \"intro\": \"whh_security\",\n" +
                "            \"assetName\": \"whh_security\",\n" +
                "            \"uploadedAt\": \"2024-03-11 18:31:56\",\n" +
                "            \"assetEnName\": \"whh_security_1\",\n" +
                "            \"timeSpan\": \"2024/03/01 ~ 2024/03/31\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"holderCompany\": \"ida1\",\n" +
                "            \"dataInfo\": {\n" +
                "                \"dbName\": \"asset\",\n" +
                "                \"itemList\": [\n" +
                "                    {\n" +
                "                        \"dataLength\": 0,\n" +
                "                        \"dataType\": \"int\",\n" +
                "                        \"name\": \"id\",\n" +
                "                        \"description\": \"\",\n" +
                "                        \"isPrimaryKey\": 1,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"name\",\n" +
                "                        \"description\": \"名称\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"adress\",\n" +
                "                        \"description\": \"地址\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"phone\",\n" +
                "                        \"description\": \"联系方式\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"purpose\",\n" +
                "                        \"description\": \"用途\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"int\",\n" +
                "                        \"name\": \"balance\",\n" +
                "                        \"description\": \"金额\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 0,\n" +
                "                        \"dataType\": \"date\",\n" +
                "                        \"name\": \"time\",\n" +
                "                        \"description\": \"时间\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"remarks\",\n" +
                "                        \"description\": \"备注\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"socialid\",\n" +
                "                        \"description\": \"社会id\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"tableName\": \"whh_enterprise\"\n" +
                "            },\n" +
                "            \"scale\": \"2000条\",\n" +
                "            \"txId\": \"17bbaf122cd2116cca2135fa7ee19dd565cb6f8aabe84a658c14997bde054372\",\n" +
                "            \"cycle\": \"1分\",\n" +
                "            \"assetType\": 1,\n" +
                "            \"assetNumber\": \"320240311000015941\",\n" +
                "            \"assetId\": \"36\",\n" +
                "            \"intro\": \"whh_enterprise\",\n" +
                "            \"assetName\": \"whh_enterprise\",\n" +
                "            \"uploadedAt\": \"2024-03-11 18:29:37\",\n" +
                "            \"assetEnName\": \"whh_enterprise_3\",\n" +
                "            \"timeSpan\": \"2024/03/01 ~ 2024/03/31\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"sqltext\": \"SELECT SUM(whh_enterprise_3.balance * whh_security_1.balance + 1) FROM whh_enterprise_3,whh_security_1 WHERE whh_enterprise_3.socialid=whh_security_1.socialid \",\n" +
                "    \"modelParams\": null,\n" +
                "    \"isStream\": 0,\n" +
                "    \"modelType\": 0\n" +
                "}";

        Result result = testTemplate.postForObject("/v1/commit/dag", req, Result.class);
        System.out.println(result);
    }

    @Test
    public void testCommitWithStream() {;
        String req = "{\n" +
                "    \"assetInfoList\": [\n" +
                "        {\n" +
                "            \"holderCompany\": \"ida2\",\n" +
                "            \"dataInfo\": {\n" +
                "                \"dbName\": \"asset\",\n" +
                "                \"itemList\": [\n" +
                "                    {\n" +
                "                        \"dataLength\": 0,\n" +
                "                        \"dataType\": \"int\",\n" +
                "                        \"name\": \"id\",\n" +
                "                        \"description\": \"\",\n" +
                "                        \"isPrimaryKey\": 1,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"name\",\n" +
                "                        \"description\": \"名称\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"adress\",\n" +
                "                        \"description\": \"地址\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"phone\",\n" +
                "                        \"description\": \"联系方式\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"purpose\",\n" +
                "                        \"description\": \"用途\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"int\",\n" +
                "                        \"name\": \"balance\",\n" +
                "                        \"description\": \"金额\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 0,\n" +
                "                        \"dataType\": \"date\",\n" +
                "                        \"name\": \"time\",\n" +
                "                        \"description\": \"时间\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"remarks\",\n" +
                "                        \"description\": \"备注\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"socialid\",\n" +
                "                        \"description\": \"社会id\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"tableName\": \"whh_security\"\n" +
                "            },\n" +
                "            \"scale\": \"2000条\",\n" +
                "            \"txId\": \"17bbaf383ac09a28ca60731891c3be39ef04b3fc414648eeab71fb4e9d6bf080\",\n" +
                "            \"cycle\": \"1分\",\n" +
                "            \"assetType\": 1,\n" +
                "            \"assetNumber\": \"120240311000014376\",\n" +
                "            \"assetId\": \"37\",\n" +
                "            \"intro\": \"whh_security\",\n" +
                "            \"assetName\": \"whh_security\",\n" +
                "            \"uploadedAt\": \"2024-03-11 18:31:56\",\n" +
                "            \"assetEnName\": \"whh_security_1\",\n" +
                "            \"timeSpan\": \"2024/03/01 ~ 2024/03/31\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"holderCompany\": \"ida1\",\n" +
                "            \"dataInfo\": {\n" +
                "                \"dbName\": \"asset\",\n" +
                "                \"itemList\": [\n" +
                "                    {\n" +
                "                        \"dataLength\": 0,\n" +
                "                        \"dataType\": \"int\",\n" +
                "                        \"name\": \"id\",\n" +
                "                        \"description\": \"\",\n" +
                "                        \"isPrimaryKey\": 1,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"name\",\n" +
                "                        \"description\": \"名称\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"adress\",\n" +
                "                        \"description\": \"地址\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"phone\",\n" +
                "                        \"description\": \"联系方式\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"purpose\",\n" +
                "                        \"description\": \"用途\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"int\",\n" +
                "                        \"name\": \"balance\",\n" +
                "                        \"description\": \"金额\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 0,\n" +
                "                        \"dataType\": \"date\",\n" +
                "                        \"name\": \"time\",\n" +
                "                        \"description\": \"时间\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"remarks\",\n" +
                "                        \"description\": \"备注\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"dataLength\": 255,\n" +
                "                        \"dataType\": \"varchar\",\n" +
                "                        \"name\": \"socialid\",\n" +
                "                        \"description\": \"社会id\",\n" +
                "                        \"isPrimaryKey\": 0,\n" +
                "                        \"privacyQuery\": 1\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"tableName\": \"whh_enterprise\"\n" +
                "            },\n" +
                "            \"scale\": \"2000条\",\n" +
                "            \"txId\": \"17bbaf122cd2116cca2135fa7ee19dd565cb6f8aabe84a658c14997bde054372\",\n" +
                "            \"cycle\": \"1分\",\n" +
                "            \"assetType\": 1,\n" +
                "            \"assetNumber\": \"320240311000015941\",\n" +
                "            \"assetId\": \"36\",\n" +
                "            \"intro\": \"whh_enterprise\",\n" +
                "            \"assetName\": \"whh_enterprise\",\n" +
                "            \"uploadedAt\": \"2024-03-11 18:29:37\",\n" +
                "            \"assetEnName\": \"whh_enterprise_3\",\n" +
                "            \"timeSpan\": \"2024/03/01 ~ 2024/03/31\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"sqltext\": \"SELECT whh_enterprise_3.balance  FROM whh_enterprise_3 WHERE whh_enterprise_3.socialid=? \",\n" +
                "    \"modelParams\": null,\n" +
                "    \"isStream\": 1,\n" +
                "    \"modelType\": 0\n" +
                "}";

        Result result = testTemplate.postForObject("/v1/commit/dag", req, Result.class);
        System.out.println(result);
    }
}
