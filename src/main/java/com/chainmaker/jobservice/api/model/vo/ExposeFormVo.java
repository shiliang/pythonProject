package com.chainmaker.jobservice.api.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ExposeFormVo {

    private String labels;

    private String key;

    private String types;

    private String values;

    private String placeholder;

    private boolean disabled;

    private List<SelectParam> options;

    private String rules;

    private boolean required;

//    private String desc;

    @Override
    public String toString() {
        return "ExposeFormVo{" +
                "key='" + key + '\'' +
                ", values='" + values + '\'' +
                ", labels='" + labels + '\'' +
                ", types='" + types + '\'' +
                ", options=" + options +
//                ", desc='" + desc + '\'' +
                '}';
    }
}
