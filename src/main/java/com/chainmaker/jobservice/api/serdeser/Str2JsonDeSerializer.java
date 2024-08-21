package com.chainmaker.jobservice.api.serdeser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;
import com.alibaba.fastjson.JSONObject;

public class Str2JsonDeSerializer implements ObjectDeserializer {

    @Override
    public Object deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        // 反序列化 JSONObject 对象
        parser.accept(JSONToken.LBRACE);
        String jsonString = null;
        return JSON.parseObject(jsonString, JSONObject.class);
    }

    @Override
    public int getFastMatchToken() {
        // 返回匹配的 JSONToken，这里我们匹配的是 LITERAL_STRING
        return JSONToken.LITERAL_STRING;
    }
}

