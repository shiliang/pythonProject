package com.chainmaker.jobservice.api.model.po.contract.job;

import lombok.Data;

/**
 * @author gaokang
 * @date 2022-09-19 19:27
 * @description:
 * @version:
 */
@Data
public class ModulePo {
    private String moduleName;
    private String paramList;

    @Override
    public String toString() {
        return "ModulePo{" +
                "moduleName='" + moduleName + '\'' +
                ", params='" + paramList + '\'' +
                '}';
    }
}
