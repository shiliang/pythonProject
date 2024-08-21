package com.chainmaker.jobservice.api.model.job.task;

import com.alibaba.fastjson.annotation.JSONField;
import com.chainmaker.jobservice.api.serdeser.Obj2StrSerializer;
import lombok.Data;

@Data
public class ModuleParam {

    private String partyId;

    private String Key;

    @JSONField(serializeUsing = Obj2StrSerializer.class)
    private Object Value;

//    public ModuleParam(String key, String value) {
//        Key = key;
//        Value = value;
//    }

    public ModuleParam(String key, Object value) {
        Key = key;
        Value = value;
    }

    public ModuleParam(){

    }
}
