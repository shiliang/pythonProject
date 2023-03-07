package com.chainmaker.jobservice.api.model.po.contract;

import com.alibaba.fastjson.JSONObject;

/**
 * @author gaokang
 * @date 2022-09-23 03:48
 * @description:
 * @version:
 */

public class PartyInfoPo {
    private JSONObject serverInfo;
    private String timestamp;

    public JSONObject getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(JSONObject serverInfo) {
        this.serverInfo = serverInfo;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
