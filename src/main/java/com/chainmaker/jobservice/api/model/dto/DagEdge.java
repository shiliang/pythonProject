/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-07-2022/7/16 18:19
 * @Description: TODO
 * @Version: 1.0
 */

package com.chainmaker.backendservice.model.dto;

import com.alibaba.fastjson.JSONArray;

public class DagEdge {
    private String from;
    private String to;
    private JSONArray label;

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setLabel(JSONArray label) {
        this.label = label;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public JSONArray getLabel() {
        return label;
    }

}