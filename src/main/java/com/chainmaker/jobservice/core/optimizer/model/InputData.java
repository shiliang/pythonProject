package com.chainmaker.jobservice.core.optimizer.model;

import lombok.Data;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-21 14:31
 * @description:SPDZ的输入
 * @version:
 */
@Data
public class InputData {
    private Integer nodeSrc;
    private String tableName;
    private String column;
    private String domainID;
    private String domainName;
    private String assetName;
}
