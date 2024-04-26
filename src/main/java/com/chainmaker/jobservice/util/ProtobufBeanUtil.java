package com.chainmaker.jobservice.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.Type;


public class ProtobufBeanUtil {

    private ProtobufBeanUtil() {
    }

    /**
     * 将ProtoBean对象转化为POJO对象
     *
     * @param destPojoClass 目标POJO对象的类类型
     * @param sourceMessage 含有数据的ProtoBean对象实例
     * @param <PojoType>    目标POJO对象的类类型范型
     * @return
     * @throws IOException
     */
    public static <PojoType> PojoType toPojoBean(@NotNull Class<PojoType> destPojoClass, @NotNull Message sourceMessage)
            throws IOException {
        String json = JsonFormat.printer().print(sourceMessage);
        return JSON.parseObject(json, destPojoClass);
    }

    public static <PojoType> PojoType toPojoBean(@NotNull Type destPojoType, @NotNull Message sourceMessage) throws InvalidProtocolBufferException {
        String json = JsonFormat.printer().includingDefaultValueFields().print(sourceMessage);
        return JSON.parseObject(json, destPojoType);
    }


    public static <PojoType> PojoType toPojoBean(@NotNull TypeReference<PojoType> typeReference, @NotNull Message sourceMessage)
            throws IOException {
        String json = JsonFormat.printer().includingDefaultValueFields().print(sourceMessage);
        return JSON.parseObject(json, typeReference.getType());
    }


    /**
     * 将POJO对象转化为ProtoBean对象
     *
     * @param destBuilder    目标Message对象的Builder类
     * @param sourcePojoBean 含有数据的POJO对象
     * @return
     * @throws IOException
     */
    public static void toProtoBean(@NotNull Message.Builder destBuilder, @NotNull Object sourcePojoBean) throws IOException {
        String json = JSON.toJSONString(sourcePojoBean);
        JsonFormat.parser().merge(json, destBuilder);
    }
}