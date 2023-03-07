package com.chainmaker.jobservice.api.model.po.contract;

import java.util.HashMap;

/**
 * @author gaokang
 * @date 2022-10-31 19:51
 * @description:
 * @version:
 */
public class ServiceMonitorPo {
    private String id;
    private String orgDID;
    private String instance;
    private Record current;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgDID() {
        return orgDID;
    }

    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public Record getCurrent() {
        return current;
    }

    public void setCurrent(Record current) {
        this.current = current;
    }

    @Override
    public String toString() {
        return "ServiceMonitorPo{" +
                "id='" + id + '\'' +
                ", orgDID='" + orgDID + '\'' +
                ", instance='" + instance + '\'' +
                ", current=" + current +
                '}';
    }
}
