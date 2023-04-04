package com.chainmaker.jobservice.api.model.po.contract.job;

/**
 * @author gaokang
 * @date 2022-09-19 19:27
 * @description:
 * @version:
 */

public class ModulePo {
    private String moduleName;
    private String params;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "ModulePo{" +
                "moduleName='" + moduleName + '\'' +
                ", params='" + params + '\'' +
                '}';
    }
}
