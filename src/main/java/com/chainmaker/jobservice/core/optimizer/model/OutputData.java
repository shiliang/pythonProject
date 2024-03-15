package com.chainmaker.jobservice.core.optimizer.model;

import lombok.Data;

/**
 * @author gaokang
 * @date 2022-08-22 20:14
 * @description:计算结果输出
 * @version: 1.0.0
 */
@Data
public class OutputData {
    private String tableName;
    private String domainID;
    private String outputSymbol;
    private String domainName;
}
