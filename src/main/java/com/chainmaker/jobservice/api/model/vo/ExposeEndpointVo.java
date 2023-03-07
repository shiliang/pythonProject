package com.chainmaker.jobservice.api.model.vo;

import java.util.List;

public class ExposeEndpointVo {
    private String name;
    private List<ExposeFormVo> form;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ExposeFormVo> getForm() {
        return form;
    }

    public void setForm(List<ExposeFormVo> form) {
        this.form = form;
    }

    @Override
    public String toString() {
        return "ExposeEndpointVo{" +
                "name='" + name + '\'' +
                ", form=" + form +
                '}';
    }
}
