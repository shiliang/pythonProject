/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-06-2022/6/29 15:14
 * @Description: 任务明细 数据传输对象
 * @Version: 1.0
 */

package com.chainmaker.backendservice.model.dto;

public class MissionDetailInfo {
    private String missionId;
    private String datacatalogId;
    private String name;
    private String remark;
    private int version;
    private String orgId;

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public String getDatacatalogId() {
        return datacatalogId;
    }

    public void setDatacatalogId(String datacatalogId) {
        this.datacatalogId = datacatalogId;
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
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
