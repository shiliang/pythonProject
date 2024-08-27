package com.chainmaker.jobservice.api.serdeser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class Obj2StrSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) {
        SerializeWriter out = serializer.getWriter();
        if (object == null) {
            out.writeNull();
        } else if(object instanceof String) {
            out.writeString((String) object);
        } else {
            out.writeString(JSON.toJSONString(object));
        }
    }
}
