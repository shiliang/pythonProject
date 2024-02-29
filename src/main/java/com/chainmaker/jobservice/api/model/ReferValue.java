package com.chainmaker.jobservice.api.model;

import lombok.Data;

/**
 * @author gaokang
 * @date 2022-10-10 20:01
 * @description:服务引用配置
 * @version: 1.0
 */
@Data
public class ReferValue {
    private String key;
    private String referServiceID;
    private String value;
}
