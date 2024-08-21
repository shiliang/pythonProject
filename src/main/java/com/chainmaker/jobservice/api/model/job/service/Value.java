package com.chainmaker.jobservice.api.model.job.service;

import com.alibaba.fastjson.annotation.JSONField;
import com.chainmaker.jobservice.api.serdeser.Obj2StrSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author gaokang
 * @date 2022-10-10 20:00
 * @description:用户自定义配置
 * @version: 1.0
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Value {
    private String key;

    @JSONField(serializeUsing = Obj2StrSerializer.class)
    private Object value;


}
