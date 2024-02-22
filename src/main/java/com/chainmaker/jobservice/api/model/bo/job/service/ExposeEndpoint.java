package com.chainmaker.jobservice.api.model.bo.job.service;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-10-10 19:58
 * @description:服务声明
 * @version: 1.0
 */

@Data
public class ExposeEndpoint {

    private String orgId;

    private String orgName;
    private String serviceClass;
    private String name;

    /**
     * 是否使用加密通信
     */
    private String tlsEnabled;
    /**
     * 暴露服务方法描述
     */
    private String description;

    /**
     * TLS证书
     */
    private String serviceCa;
    private String serviceCert;
    private String serviceKey;
    /**
     * 通信协议类型：
     * HTTP
     * GRPC
     */
    private String protocol;
    /**
     * hostname:port形式
     */
    private String address;
    /**
     * 路径
     * 仅当protocol:HTTP有效
     */
    private String path;
    /**
     * GET/POST/PUT等
     * 仅当protocol:HTTP有效
     */
    private String method;


    private List<Value> valueList;

    public String toJSONString() {
        String jsonString = JSONObject.toJSONString(this);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        HashMap<String, Object> valueMap = new HashMap<>();
        for (Value value : valueList) {
            valueMap.put(value.getKey(), value.getValue());
        }
        jsonObject.put("valueMap", valueMap);
        return jsonString.toString();
    }


}
