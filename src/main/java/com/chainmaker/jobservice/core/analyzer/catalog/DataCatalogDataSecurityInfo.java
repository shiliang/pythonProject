package com.chainmaker.jobservice.core.analyzer.catalog;

public class DataCatalogDataSecurityInfo {

    private String id;

    /**
     * 目录表id
     */
    private String dataCatalogId;

    private String dataCatalogName;
    /**
     * 数据库表的列名称
     */
    private String columnName;

    /**
     * 分类id
     */
    private Integer categoryId;
    /**
     * 分类编码
     */
    private String categoryCode;
    /**
     * 分类名称
     */
    private String classification;
    /**
     * 分级id
     */
    private Integer gradingId;
    /**
     * 分级名称
     */
    private String grading;
    /**
     * 分级对应的数字
     */
    private Integer gradingLevel;
    /**
     * 分类扩展属性
     */

    private String categoryExtraFields;

    /**
     * 资产扩展属性
     */
    private String assetExtraFields;
    /**
     * 隐私计算算法
     */
    private String algorithmName;
    /**
     * 数据目录根据数据安全级别提供的出的数据形式
     * original=原始数据、
     * encrypted=加密数据、
     * forbiddenOutbound=不可出域。
     * 说明：敏感数据需要加密，最高级别数据不可出域
     */
    private String sensitiveType;

    private Integer isQueryable;
    public Integer getIsQueryable() {
        return isQueryable;
    }

    public void setIsQueryable(Integer isQueryable) {
        this.isQueryable = isQueryable;
    }
    @Override
    public String toString() {
        return "DataCatalogDataSecurityInfo{" +
                "id='" + id + '\'' +
                ", dataCatalogId='" + dataCatalogId + '\'' +
                ", dataCatalogName='" + dataCatalogName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", categoryId=" + categoryId +
                ", categoryCode='" + categoryCode + '\'' +
                ", classification='" + classification + '\'' +
                ", gradingId=" + gradingId +
                ", grading='" + grading + '\'' +
                ", gradingLevel=" + gradingLevel +
                ", categoryExtraFields='" + categoryExtraFields + '\'' +
                ", assetExtraFields='" + assetExtraFields + '\'' +
                ", algorithmName='" + algorithmName + '\'' +
                ", sensitiveType='" + sensitiveType + '\'' +
                ", isQueryable='" + isQueryable +
                '}';
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataCatalogId() {
        return dataCatalogId;
    }

    public void setDataCatalogId(String dataCatalogId) {
        this.dataCatalogId = dataCatalogId;
    }

    public String getDataCatalogName() {
        return dataCatalogName;
    }

    public void setDataCatalogName(String dataCatalogName) {
        this.dataCatalogName = dataCatalogName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public Integer getGradingId() {
        return gradingId;
    }

    public void setGradingId(Integer gradingId) {
        this.gradingId = gradingId;
    }

    public String getGrading() {
        return grading;
    }

    public void setGrading(String grading) {
        this.grading = grading;
    }

    public Integer getGradingLevel() {
        return gradingLevel;
    }

    public void setGradingLevel(Integer gradingLevel) {
        this.gradingLevel = gradingLevel;
    }

    public String getCategoryExtraFields() {
        return categoryExtraFields;
    }

    public void setCategoryExtraFields(String categoryExtraFields) {
        this.categoryExtraFields = categoryExtraFields;
    }

    public String getAssetExtraFields() {
        return assetExtraFields;
    }

    public void setAssetExtraFields(String assetExtraFields) {
        this.assetExtraFields = assetExtraFields;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getSensitiveType() {
        return sensitiveType;
    }

    public void setSensitiveType(String sensitiveType) {
        this.sensitiveType = sensitiveType;
    }

}
