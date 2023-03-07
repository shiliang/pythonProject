package com.chainmaker.jobservice.api;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;
import com.chainmaker.jobservice.core.SqlParser;
import com.chainmaker.jobservice.core.optimizer.nodes.DAG;
import com.chainmaker.jobservice.core.optimizer.plans.PhysicalPlan;

public class Driver {
    public static void main(String[] args) {
//        String query = "SELECT NUCLEIC_ACID_TESTING(TRAFFIC_NUCLEIC_ACID_TESTING.JSONDATA) FROM TRAFFIC_NUCLEIC_ACID_TESTING";
//        String query = "SELECT FL(a.b.c.is_train=true,is_test=false,FLLABEL(SOURCE_DATA=TRAFFIC_NUCLEIC_ACID_TESTING,with_label=true,label_type=int,output_format=dense,namespace=experiment),FLLABEL(SOURCE_DATA=TRAFFIC_NUCLEIC_ACID,with_label=false,output_format=dense,namespace=experiment),INTERSECTION(intersect_method=rsa),HELR(penalty=L2,tol=0.0001,alpha=0.01,optimizer=rmsprop,batch_size=-1,learning_rate=0.15,init_param.init_method=zeros,init_param.fit_intercept=true,max_iter=1,early_stop=diff,encrypt_param.key_length=1024,reveal_strategy=respectively,reveal_every_iter=true),EVAL(eval_type=binary)) FROM TRAFFIC_NUCLEIC_ACID_TESTING,TRAFFIC_NUCLEIC_ACID";
        String query = "SELECT ACID(TRFFIC-A.jsondata) FROM TRFFIC-A";
        Integer isStream = 1, modelType = 2;
        String sql = query.toUpperCase().replace("\"", "");
        SqlParser sqlParser = new SqlParser(sql, modelType, isStream);
        DAG<PhysicalPlan> dag = sqlParser.parser();
    }
}
