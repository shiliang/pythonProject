package com.chainmaker.jobservice.api.model.bo.job.task;

import com.alibaba.fastjson.JSONObject;

/**
 * @author gaokang
 * @date 2022-08-13 10:17
 * @description:任务的模型参数
 * @version: 1.0.0
 */

public class Module {
    private String moduleName;
    private JSONObject params;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public JSONObject getParams() {
        return params;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "Module{" +
                "moduleName='" + moduleName + '\'' +
                ", params=" + params +
                '}';
    }
}
