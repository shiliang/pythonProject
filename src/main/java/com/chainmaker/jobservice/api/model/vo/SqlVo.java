package com.chainmaker.jobservice.api.model.vo;

/**
 * @author gaokang
 * @date 2022-09-22 21:34
 * @description:用户提交SQL
 * @version: 1.0.0
 */

public class SqlVo {
    private String sqltext;
    private Integer modelType;
    private Integer isStream;

    public String getSqltext() {
        return sqltext;
    }

    public void setSqltext(String sqltext) {
        this.sqltext = sqltext;
    }

    public Integer getModelType() {
        return modelType;
    }

    public void setModelType(Integer modelType) {
        this.modelType = modelType;
    }

    public Integer getIsStream() {
        return isStream;
    }

    public void setIsStream(Integer isStream) {
        this.isStream = isStream;
    }
}
