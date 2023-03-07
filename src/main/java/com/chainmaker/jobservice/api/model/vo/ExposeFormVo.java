package com.chainmaker.jobservice.api.model.vo;

import java.util.List;

public class ExposeFormVo {
    private String key;
    private String values;
    private String labels;
    private String types;
    private List<SelectParam> options;
    private String desc;

    public List<SelectParam> getOptions() {
        return options;
    }

    public void setOptions(List<SelectParam> options) {
        this.options = options;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    @Override
    public String toString() {
        return "ExposeFormVo{" +
                "key='" + key + '\'' +
                ", values='" + values + '\'' +
                ", labels='" + labels + '\'' +
                ", types='" + types + '\'' +
                ", options=" + options +
                ", desc='" + desc + '\'' +
                '}';
    }
}
