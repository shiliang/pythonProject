package com.chainmaker.jobservice.api;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chainmaker.jobservice.api.builder.JobBuilder;
import com.chainmaker.jobservice.api.builder.JobBuilderWithOptimizer;
import com.chainmaker.jobservice.api.model.OrgInfo;
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
        catalogConfig.setAddress("192.168.40.246");
        catalogConfig.setPort("31008");

//        String query = "SELECT FL(a.b.c.is_train=true,a.b.is_on=false,is_test=false,FLLABEL(SOURCE_DATA=ADATA,with_label=true,label_type=int,output_format=dense,namespace=experiment),FLLABEL(SOURCE_DATA=BDATA,with_label=false,output_format=dense,namespace=experiment),INTERSECTION(intersect_method=rsa),HESB(penalty=L2,tol=0.0001,alpha=0.01,optimizer=rmsprop,batch_size=-1,learning_rate=0.15,init_param.init_method=zeros,init_param.fit_intercept=true,max_iter=1,early_stop=diff,encrypt_param.key_length=1024,reveal_strategy=respectively,reveal_every_iter=true),EVAL(eval_type=binary)) FROM ADATA,BDATA";
//        String query = "select /*+ TEEJOIN(1,2) */ TESTA(adata.a1, bdata.b1), adata.a2 from adata join bdata on adata.id=bdata.id";
//        String query = "SELECT AVG(ADATA.A1+BDATA.B1), ADATA.A2+ADATA.A1, CDATA.C1+CDATA.C1, ADATA.A1, BDATA.B1 FROM (ADATA JOIN BDATA ON ADATA.A1=BDATA.B1) " +
//                "JOIN CDATA ON ADATA.A2=CDATA.C2 WHERE BDATA.B1>3 AND ADATA.A2<10";
//        String query = "select adata.a1 from adata";
//        String query = "SELECT CDATA.ID, BDATA.B1, ADATA.A2, TABLEC.C1 FROM ((ADATA JOIN BDATA ON ADATA.A1=BDATA.B1) JOIN TABLEC ON ADATA.A2=TABLEC.C2) JOIN CDATA ON ADATA.ID=CDATA.ID";

//        String query = "SELECT ADATA.A1, SUM(BDATA.B1) FROM (ADATA JOIN BDATA ON ADATA.A1=BDATA.B1) WHERE ADATA.A2<10 GROUP BY ADATA.A1 HAVING SUM(BDATA.B1) > 5 AND COUNT(BDATA.B1) > 1";
//        String query = "select adata.a1 from (adata join cdata on adata.a1=cdata.c1) join bdata on bdata.b2=cdata.c2";

//        String query = "SELECT FL(a.b.c.is_train=true,a.b.is_on=false,is_test=false,FLLABEL(SOURCE_DATA=ADATA,with_label=true,label_type=int,output_format=dense,namespace=experiment),FLLABEL(SOURCE_DATA=BDATA,with_label=true,output_format=dense,namespace=experiment),HOLR(penalty=L2,tol=0.0001,alpha=0.01,optimizer=rmsprop,batch_size=-1,learning_rate=0.15,init_param.init_method=zeros,init_param.fit_intercept=true,max_iter=1,early_stop=diff,encrypt_param.key_length=1024,reveal_strategy=respectively,reveal_every_iter=true),EVAL(eval_type=binary)) FROM ADATA,BDATA";
//        String query = "select /*+ TEEJOIN */ adata.a2 from adata join bdata on adata.id=bdata.id";

//        String query = "SELECT TEST_C_1.C2+TEST_B_1.B2 FROM TEST_C_1 JOIN TEST_B_1 ON TEST_C_1.ID=TEST_B_1.ID WHERE TEST_B_1.ID<5";
//        String query = "SELECT FL(a.b.c.is_train=true,a.b.is_on=false,is_test=false,FLLABEL(SOURCE_DATA=ADATA,with_label=true,label_type=int,output_format=dense,namespace=experiment),FLLABEL(SOURCE_DATA=BDATA,with_label=true,output_format=dense,namespace=experiment),HEPR(a=1),EVAL(eval_type=binary)) FROM ADATA,BDATA";
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
//        String query = "SELECT FL(is_train=true,is_test=false,FLLABEL(SOURCE_DATA=BREAST_HETERO_GGUEST,with_label=true,label_type=int,output_format=dense,namespace=experiment),FLLABEL(SOURCE_DATA=BREAST_HETERO_HHOST,with_label=false,output_format=dense,namespace=experiment),INTERSECTION(intersect_method=rsa),HENN(bottom_nn_define.a.in_features=10,bottom_nn_define.a.out_features=4,bottom_nn_define.a.layer=Linear,bottom_nn_define.b.layer=ReLU,top_nn_define.a.in_features=10,top_nn_define.a.out_features=4,top_nn_define.b.layer=Linear,top_nn_define.b.layer=Sigmoid,interactive_layer_define.a.out_dim=4,interactive_layer_define.a.need_guest=true,interactive_layer_define.a.host_num=1,interactive_layer_define.a.guest_dim=4,interactive_layer_define.a.host_dim=4,interactive_layer_define.a.layer=InteractiveLayer,epochs=2,loss.reduction=mean,loss.loss_fn=BCELoss),EVAL(eval_type=binary)) FROM BREAST_HETERO_GGUEST,BREAST_HETERO_HHOST";
//        String query = "SELECT TESTA(ADATA.ID, BDATA.ID, TABLEC.ID) FROM ADATA, BDATA, TABLEC";
//        String query = "SELECT T_DT_USER.ID FROM T_DT_USER,T_JTW_USER WHERE T_DT_USER.ID=T_JTW_USER.ID JOIN ";
//        String query = "SELECT DT_DATA.DATA, WYC_DATA.ID, JTW_DATA.DATA+5, DT_DATA.DATA*0.2+WYC_DATA.DATA*0.1+JTW_DATA.DATA*0.5 FROM WYC_DATA,DT_DATA,JTW_DATA WHERE WYC_DATA.ID=DT_DATA.ID AND WYC_DATA.ID=JTW_DATA.ID";
//        String query = "SELECT SUM(WYC_DATA.DATA+JTW_DATA.DATA) FROM WYC_DATA,DT_DATA,JTW_DATA WHERE WYC_DATA.ID=DT_DATA.ID AND WYC_DATA.ID=JTW_DATA.ID";

//        String query = "SELECT ATEST1.ID FROM ATEST1,BTEST1,CTEST1 WHERE ATEST1.ID=BTEST1.ID AND ATEST1.ID=CTEST1.ID";
//        String query = "SELECT T_MULXX_1.VAL1+T_MULXX_2.VAL1 FROM T_MULXX_1, T_MULXX_2 WHERE T_MULXX_1.ID=T_MULXX_2.ID";
        String query = "SELECT /*+ FUNC(TEE) JOIN(TEE) */ MUL(CTEST3.ID,BTEST3.ID) FROM CTEST3, BTEST3 WHERE CTEST3.ID = BTEST3.ID";

        // modelType:
        // 0： 联邦查询
        // 1： 联邦学习
        // 2： 机密计算
        // isStream：
        // 0: 任务类型
        // 1: 服务类型

        Integer isStream = 0, modelType = 0;

        String sql = query.replace("\"", "");
        SqlParser sqlParser = new SqlParser(sql, modelType, isStream, null,null);
        sqlParser.setCatalogConfig(catalogConfig);
        if (isStream == 1) {    // 2590ms
            long start = System.currentTimeMillis();
            OrgInfo orgInfo = new OrgInfo("wx-org3.chainmaker.orgDID", "wx-org3.chainmaker.orgDID");
            JobBuilder jobBuilder = new JobBuilder(modelType, isStream, sqlParser.parser(), orgInfo, sqlParser.getSql());
            jobBuilder.build();
            System.out.println(JSONObject.toJSONString(jobBuilder.getJobTemplate(), SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat));
            long end = System.currentTimeMillis();
            System.out.println("Origin Plan: " + String.valueOf(end-start) + "ms");
        } else {    // 4758ms
            long start = System.currentTimeMillis();
            JobBuilderWithOptimizer jobBuilder = new JobBuilderWithOptimizer(modelType, isStream, sqlParser.parserWithOptimizer(), sqlParser.getColumnInfoMap(), "wx-org3.chainmaker.orgDID", sqlParser.getSql());     // 4000+ ms
            jobBuilder.build(); // <50 ms
            long end = System.currentTimeMillis();
            System.out.println("Optimizer Plan: " + String.valueOf(end-start) + "ms");
            System.out.println(JSONObject.toJSONString(jobBuilder.getJobTemplate(), SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat));
        }
    }
}

/*



 */