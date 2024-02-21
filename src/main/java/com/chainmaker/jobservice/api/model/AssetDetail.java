package com.chainmaker.jobservice.api.model;


import lombok.Data;

@Data
public class AssetDetail {
    private String assetId;
    private String orgId;
    private String assetName;
    private String dBName;
    private String tableName;
    private String columnName;
    private String type;
    private int length;
    private String comments;

}

