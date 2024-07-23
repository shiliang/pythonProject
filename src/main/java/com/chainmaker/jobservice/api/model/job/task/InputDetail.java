package com.chainmaker.jobservice.api.model.job.task;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.chainmaker.jobservice.api.serdeser.Obj2StrSerializer;
import lombok.Data;

/**
 * @author gaokang
 * @date 2022-08-13 11:01
 * @description:任务输入详细信息
 * @version: 1.0.0
 */
@Data
public class InputDetail {
    private String dataName;
    private String taskSrc = "";
    private String dataId = "";
    private String domainName;
    private String domainId;
    private String databaseName;
    private String tableName;
    private String columnName;
    private String type;
    private Integer length;
    private String comments;
    private String role = "client";

    //go 的json解析库
    @JSONField(serializeUsing = Obj2StrSerializer.class)
    private JSONObject params;

    private String assetName;
}
