package com.chainmaker.jobservice.api.model.job.task;

import lombok.Data;

@Data
public class ModuleParam {
    private String Key;
    private Object Value;

    public ModuleParam(String key, Object value) {
        Key = key;
        Value = value;
    }
}
