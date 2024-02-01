package com.chainmaker.backendservice.model.dto;

import java.util.List;
import java.util.ArrayList;

public class DataCatalogInfo {
    private String id;
    private String code;
    private String name;
    private String remark;
    private int version;
    private String dataVersion;
    private long publishTime;
    private String orgId;
    private String orgDID;
    private int status;
    private String createTime;
    private String assetType;
    private List<DataCatalogDetailInfo> itemVOList;
    private String permissionLabel;
    private int encrypt;
    private String cipher;

    public DataCatalogInfo() {this.itemVOList = new ArrayList<>();}

    public String getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgDID() {
        return orgDID;
    }

    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }

    public ArrayList<DataCatalogDetailInfo> getItemVOList() {
        return (ArrayList<DataCatalogDetailInfo>) itemVOList;
    }

    public void setItemVOList(ArrayList<DataCatalogDetailInfo> itemVOList) {
        this.itemVOList = itemVOList;
    }

    public boolean addCatalogDetailTuple(DataCatalogDetailInfo tuple){
        return this.itemVOList.add(tuple);
    }

    @Override
    public String toString() {
        return "DataCatalogInfo{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", remark='" + remark + '\'' +
                ", version=" + version +
                ", dataVersion='" + dataVersion + '\'' +
                ", publishTime=" + publishTime +
                ", orgId='" + orgId + '\'' +
                ", orgDID='" + orgDID + '\'' +
                ", status=" + status +
                ", createTime='" + createTime + '\'' +
                ", assetType='" + assetType + '\'' +
                ", itemVOList=" + itemVOList +
                '}';
    }

    public String getPermissionLabel() {
        return permissionLabel;
    }

    public void setPermissionLabel(String permissionLabel) {
        this.permissionLabel = permissionLabel;
    }

    public int getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

}
