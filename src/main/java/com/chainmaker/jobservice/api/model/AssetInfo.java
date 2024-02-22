package com.chainmaker.jobservice.api.model;

import lombok.Data;

@Data
public class AssetInfo {
    /**
     * 主键ID
     */
    private String assetId;
    /**
     * 数据资产编号
     */
    private String assetNumber;
    /**
     * 数据资产名称
     */
    private String assetName;

    /**
     * 资产英文简称
     */
    private String assetEnName;
    /**
     * 数据源类型：1数据库，2API
     */
    private Integer assetType;
    /**
     * 数据规模
     */
    private String scale;
    /**
     * 更新周期
     */
    private String cycle;

    /**
     * 时间跨度
     */
    private String timeSpan;

    /**
     * 资产所有者
     */
    private String holderCompany;

    /**
     * 资产描述
     */
    private String intro;
    /**
     * 交易ID
     */
    private String txId;
    /**
     * 登记时间（也是上链时间）
     */
    private String uploadedAt;

    /**
     * 数据集信息
     */
    private DataInfo dataInfo;
}
