package com.chainmaker.jobservice.core.optimizer.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.chainmaker.jobservice.api.serdeser.Obj2StrSerializer;
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

    private String index;

    private String domainID;

    private String domainName;

    private String assetName;
}
