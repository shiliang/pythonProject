package com.chainmaker.utils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.serdeser.Str2JsonDeSerializer;

import java.lang.reflect.Type;

public class Main {

    public static void main(String[] args) {
        // JSON 字符串
        String jsonString = "{\"name\":\"John Doe\",\"age\":30,\"address\":\"{\\\"street\\\":\\\"My Street\\\",\\\"city\\\":\\\"New York\\\"}\",\"details\":\"{\\\"phone\\\":\\\"1234567890\\\",\\\"email\\\":\\\"john.doe@example.com\\\"}\"}";
        ParserConfig config = new ParserConfig();
        // 注册自定义反序列化器
        config.putDeserializer(JSONObject.class, new Str2JsonDeSerializer());

        // 转换 JSON 字符串为 Map 对象
        JSONObject jsonObject = JSON.parseObject(jsonString, JSONObject.class, config);

        // 输出结果
        System.out.println(jsonObject);
    }
}

