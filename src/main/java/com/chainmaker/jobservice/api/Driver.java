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

public class Driver {
    public static void main(String[] args) {
        CatalogConfig catalogConfig = new CatalogConfig();
        catalogConfig.setAddress("192.168.0.112");
        catalogConfig.setPort("31008");
//        String query = "SELECT NUCLEIC_ACID_TESTING(TRAFFIC_NUCLEIC_ACID_TESTING.JSONDATA) FROM TRAFFIC_NUCLEIC_ACID_TESTING";
//        String query = "SELECT FL(a.b.c.is_train=true,a.b.is_on=false,is_test=false,FLLABEL(SOURCE_DATA=ADATA,with_label=true,label_type=int,output_format=dense,namespace=experiment),FLLABEL(SOURCE_DATA=BDATA,with_label=false,output_format=dense,namespace=experiment),INTERSECTION(intersect_method=rsa),HELR(penalty=L2,tol=0.0001,alpha=0.01,optimizer=rmsprop,batch_size=-1,learning_rate=0.15,init_param.init_method=zeros,init_param.fit_intercept=true,max_iter=1,early_stop=diff,encrypt_param.key_length=1024,reveal_strategy=respectively,reveal_every_iter=true),EVAL(eval_type=binary)) FROM ADATA,BDATA";
//        String query = "select adata.a1, testt(adata.a1, bdata.b1) from adata, bdata";
        String query = "select /*+ BRAODCASTJOIN(B), TEEJOIN(A) */ adata.a1 from adata join bdata on adata.id=bdata.id";
//        String query = "SELECT ADATA.A1 FROM ADATA JOIN BDATA ON ADATA.A2=BDATA.B2 WHERE BDATA.B1>3";
//        String query = "SELECT SUM(ADATA.A1+BDATA.B2) FROM ADATA JOIN BDATA ON ADATA.A2=BDATA.B2 WHERE BDATA.B1>3";
//        String query = "select bdata.b2 from adata join bdata on adata.a1=bdata.b1 where adata.a1 > 4 and adata.a2 > 50";
//        String query = "SELECT TA.A AS A FROM TA";
        //        String query = "SELECT dept.deptno FROM dept where dept.name=1";
//        String query = "select ta.id from ta join tb on ta.id = tb.id where ta.id < 10 and ta.ta > 50";
//        String query = "select ta.ta from (ta join tb on 1=1) join tc on 1=1 where tb.id < 40 and tc.id > 50";
//        String query = "select ta.id, sum(ta.id-ta.age) " +
//                "from (ta join tb on ta.id=tb.id) " +
//                "join tc on ta.id=tc.id " +
//                "where ta.id > 10 and ta.age < 50";
//        String query = "select ta.ta, ta.id from ta where ta.id > 10";
//        String query = "select optestb.id+30 from optestc join optestb on optestc.c = optestb.b where optestc.id>5";
//        String query = "SELECT TA.A FROM TA AS X JOIN TB AS Y ON TA.ID=TB.ID where TA.A > 2 and TA.ID > 10";
//        String query = "select ta.a, tb.id from ta join tb on 1=1 where ta.a > 5 and ta.id = 1";
//        String query = "select 10 + 30, users.name, users.age " +
//                "from users join jobs on users.id=jobs.id " +
//                "where users.age > 30 and jobs.id>10 ";
        // 示例一
//        String query = "SELECT V_ENTERPRISEINSPECTION.NAME AS NAME, " +
//                "SUM(-V_ENTERPRISETAX.SOCIALSECURITY*(V_ENTERPRISEINSPECTION.DISCLOSEINFORMATION-V_ENTERPRISEOWNERCREDIT.FAILURETOACT)) SUM " +
//                "FROM (V_ENTERPRISEINSPECTION JOIN V_ENTERPRISEOWNERCREDIT ON V_ENTERPRISEINSPECTION.ID=V_ENTERPRISEOWNERCREDIT.ID) " +
//                "JOIN V_ENTERPRISETAX ON V_ENTERPRISEINSPECTION.ID=V_ENTERPRISETAX.ID " +
//                "WHERE V_ENTERPRISEINSPECTION.ABONORMAL>=(5*2)";

        // 示例二
//        String query = "select ta.id, sum(ta.age+tc.id+te.age) " +
//                "from (ta join tc on ta.id=tc.id) " +
//                "join te on ta.id=te.id" +
//                "where ta.id > 10";

        // 示例三
//        String query = "SELECT MAX(ENTERPRISE_TAX.social_security-ENTERPRISE_INSPECTION.charitable_donation) balance " +
//                "FROM (ENTERPRISE_INSPECTION JOIN ENTERPRISE_OWNER_CREDIT " +
//                "ON ENTERPRISE_INSPECTION.id=ENTERPRISE_OWNER_CREDIT.id) " +
//                "JOIN ENTERPRISE_TAX " +
//                "ON ENTERPRISE_INSPECTION.id=ENTERPRISE_TAX.id " +
//                "WHERE NOT(ENTERPRISE_INSPECTION.professional_activity > 5)";

        // modelType:
        // 0： 联邦查询
        // 1： 联邦学习
        // 2： 机密计算
        // isStream：
        // 0: 任务类型
        // 1: 服务类型
        Integer isStream = 0, modelType = 0;
        String sql = query.toUpperCase().replace("\"", "");
        SqlParser sqlParser = new SqlParser(sql, modelType, isStream);
        sqlParser.setCatalogConfig(catalogConfig);
        if (isStream == 1) {
            JobBuilder jobBuilder = new JobBuilder(modelType, isStream, sqlParser.parser());
            jobBuilder.build();
            System.out.println(JSONObject.toJSONString(jobBuilder.getJobTemplate(), SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat));
        } else {
            JobBuilderWithOptimizer jobBuilder = new JobBuilderWithOptimizer(modelType, isStream, sqlParser.parserWithOptimizer());
            jobBuilder.build();
            System.out.println(JSONObject.toJSONString(jobBuilder.getJobTemplate(), SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat));
        }
    }
}

/*



 */