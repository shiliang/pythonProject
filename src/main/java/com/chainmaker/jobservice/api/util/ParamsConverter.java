package com.chainmaker.jobservice.api.util;

import com.alibaba.fastjson.JSONObject;

public class ParamsConverter {
    public static String convertToString(JSONObject j){
        String var = j.toJSONString();
        String var1 = var.replaceAll("\"","\\\"");
        return var1;
    }
}
