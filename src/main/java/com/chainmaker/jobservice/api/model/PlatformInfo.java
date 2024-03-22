package com.chainmaker.jobservice.api.model;

import lombok.Data;

import java.util.Map;

@Data
public class PlatformInfo {
    private Integer keyId;
    private String keyMode;
    private String chainId;
    private String subChainId;
    private String chainName;
    private String accountAddress;
    private String accountName;
    private String accountType;
    private String socialCreditCode;
    private Map<Integer, String> authUsers;
    private int registerId;
    private Long createdAt;
    private Long updatedAt;

    private String assetServiceAddr;
    private String keyServiceAddr;
}
