package com.chainmaker.jobservice.core.analyzer.catalog;

public class MissionDetailVO {
    private String name;
    private String remark;
    private int version;
    private String OrgId;
    private String datacatalogId;

    public String getName() {
        return name;
    }

    public String getRemark() {
        return remark;
    }

    public int getVersion() {
        return version;
    }

    public String getOrgId() {
        return OrgId;
    }

    public String getDatacatalogId() {
        return datacatalogId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setOrgId(String orgId) {
        OrgId = orgId;
    }

    public void setDatacatalogId(String datacatalogId) {
        this.datacatalogId = datacatalogId;
    }

    @Override
    public String toString() {
        return "MissionDetailVO{" +
                "name='" + name + '\'' +
                ", remark='" + remark + '\'' +
                ", version=" + version +
                ", OrgId='" + OrgId + '\'' +
                ", datacatalogId='" + datacatalogId + '\'' +
                '}';
    }
}
