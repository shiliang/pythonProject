package com.chainmaker.jobservice.api.model.bo;

/**
 * @author gaokang
 * @date 2022-09-24 13:35
 * @description:
 * @version:
 */

public class MissionDetailInfo {
    private String missionId;
    private String name;
    private String remark;
    private int version;
    private String OrgId;
    private String datacatalogId;

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getOrgId() {
        return OrgId;
    }

    public void setOrgId(String orgId) {
        OrgId = orgId;
    }

    public String getDatacatalogId() {
        return datacatalogId;
    }

    public void setDatacatalogId(String datacatalogId) {
        this.datacatalogId = datacatalogId;
    }
}
