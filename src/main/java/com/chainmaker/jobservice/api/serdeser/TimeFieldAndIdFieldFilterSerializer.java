package com.chainmaker.jobservice.api.serdeser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializeWriter;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.UUID;

public class TimeFieldAndIdFieldFilterSerializer implements PropertyFilter {

    public static boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // 检查时间戳字段
    public static boolean isValidTimestamp(String timestamp) {
        // 检查时间戳是否为有效的时间
        Instant specificTime = Instant.parse("2024-01-01T00:00:01.820575100Z");
        try {
            long ts = Long.parseLong(timestamp);
            Instant instant = Instant.ofEpochMilli(ts);
            return instant.isAfter(specificTime);
        }catch (Exception e){
            return false;
        }
    }

    public static boolean isValidMD5Hash(String hash) {
        String regex = "^[0-9a-fA-F]{32}$";
        return hash.matches(regex);
    }

    public static void main(String[] args) {
        String hash = "ffbdaee6da5647aebd6ba82044f55b2f";

        if (isValidMD5Hash(hash)) {
            System.out.println(hash + " 是有效的 MD5 哈希值。");
        } else {
            System.out.println(hash + " 不是有效的 MD5 哈希值。");
        }
    }

    @Override
    public boolean apply(Object object, String name, Object value) {
        String val = String.valueOf(value);
        if(isValidUUID(val) || isValidTimestamp(val) || isValidMD5Hash(val)){
            return false;
        }
        return true;
    }
}
