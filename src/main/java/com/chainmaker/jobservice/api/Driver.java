package com.chainmaker.jobservice.api;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chainmaker.jobservice.api.builder.JobBuilder;
import com.chainmaker.jobservice.api.builder.JobBuilderWithOptimizer;
import com.chainmaker.jobservice.api.model.bo.config.CatalogConfig;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;
import com.chainmaker.jobservice.core.SqlParser;
import com.chainmaker.jobservice.core.optimizer.nodes.DAG;
import com.chainmaker.jobservice.core.optimizer.plans.PhysicalPlan;
import org.apache.commons.logging.LogFactory;

import java.io.PrintStream;
import java.util.logging.Logger;

import static com.chainmaker.jobservice.api.ParserApplication.disableWarning;

public class Driver {
    public static void main(String[] args) {
        CatalogConfig catalogConfig = new CatalogConfig();
        catalogConfig.setAddress("172.16.12.230");
        catalogConfig.setPort("31008");

        String query = "SELECT FL(a.b.c.is_train=true,a.b.is_on=false,is_test=false,FLLABEL(SOURCE_DATA=ADATA,with_label=true,label_type=int,output_format=dense,namespace=experiment),FLLABEL(SOURCE_DATA=BDATA,with_label=true,output_format=dense,namespace=experiment),HEPR(a=1),EVAL(eval_type=binary)) FROM ADATA,BDATA";
//        String query = "SELECT /*+ TEEJOIN */ * FROM (ADATA FULL OUTER JOIN BDATA ON ADATA.ID=BDATA.ID) FULL OUTER JOIN TABLEC ON ADATA.ID=TABLEC.ID";
//        String query = "SELECT * FROM TEST_C_1 WHERE TEST_C_1.ID=?";
//        String query = "SELECT AVG(ADATA.A1), ADATA.A2+ADATA.A1, CDATA.C3+CDATA.C3, ADATA.A1 FROM (ADATA JOIN BDATA ON ADATA.A1=BDATA.B1) JOIN CDATA ON ADATA.A2=CDATA.C2 WHERE BDATA.B1>3 AND ADATA.A2<10";
//        String query = "select avg(adata.a1) from adata join bdata on adata.a1=bdata.b1 where adata.a1>10 and bdata.b1>5";
//        String query = "select adata.a1 from (adata join bdata on adata.a1=bdata.b1) join cdata on adata.a2=cdata.c2 where adata.a1>5";
//        String query = "select adata.a1 from (adata join cdata on adata.a1=cdata.c1) join bdata on adata.a2=bdata.b2 where adata.a1>5";
//        String query = "select adata.a1, adata.a2 from adata join bdata on adata.a1=bdata.b1 where adata.a1>10 and bdata.b1>5";   strange test
//        String query = "select adata.a1 from adata union select bdata.b1 from bdata";
//        String query = "select adata.a1 from (adata join cdata on adata.a1=cdata.c1) join bdata on adata.id=bdata.id";
//        String query = "SELECT TA.A FROM TA WHERE TA.ID<3";
//        String query = "SELECT COUNT(TEST_B_1.B2 + TEST_C_1.C2) FROM TEST_B_1 JOIN TEST_C_1 ON TEST_B_1.ID=TEST_C_1.ID";
//        String query = "SELECT TESTA(ADATA.ID, BDATA.ID, TABLEC.ID) FROM ADATA, BDATA, TABLEC";

        // modelType:
        // 0： 联邦查询
        // 1： 联邦学习
        // 2： 机密计算
        // isStream：
        // 0: 任务类型
        // 1: 服务类型
        Integer isStream = 0, modelType = 1;
        String sql = query.toUpperCase().replace("\"", "");
        SqlParser sqlParser = new SqlParser(sql, modelType, isStream);
        sqlParser.setCatalogConfig(catalogConfig);
        if (isStream == 1) {    // 2590ms
            long start = System.currentTimeMillis();
            JobBuilder jobBuilder = new JobBuilder(modelType, isStream, sqlParser.parser());
            jobBuilder.build();
            System.out.println(JSONObject.toJSONString(jobBuilder.getJobTemplate(), SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat));
            long end = System.currentTimeMillis();
            System.out.println("Origin Plan: " + String.valueOf(end-start) + "ms");
        } else {    // 4758ms
            long start = System.currentTimeMillis();
            JobBuilderWithOptimizer jobBuilder = new JobBuilderWithOptimizer(modelType, isStream, sqlParser.parserWithOptimizer(), sqlParser.getColumnInfoMap());     // 4000+ ms
            jobBuilder.build(); // <50 ms
            long end = System.currentTimeMillis();
            System.out.println("Optimizer Plan: " + String.valueOf(end-start) + "ms");
            System.out.println(JSONObject.toJSONString(jobBuilder.getJobTemplate(), SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat));
        }
    }
}

/*



 */