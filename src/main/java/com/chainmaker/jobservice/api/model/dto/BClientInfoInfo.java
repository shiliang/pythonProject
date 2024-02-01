package com.chainmaker.backendservice.model.dto;

public class BClientInfoInfo {
    private int cid;
    private int unitId;
    private String cname;
    private String channel;
    private String tel;
    private String IDCode;
    private String isMember;
    private String bday;
    private String sex;
    private String email;
    private int areaId;
    private String address;
    private String IM;
    private String ctime;
    private String ipTime;
    private String operator;
    private String remark;

    public int getCid() {
        return cid;
    }

    public int getUnitId() {
        return unitId;
    }

    public String getCname() {
        return cname;
    }

    public String getChannel() {
        return channel;
    }

    public String getTel() {
        return tel;
    }

    public String getIDCode() {
        return IDCode;
    }

    public String getIsMember() {
        return isMember;
    }

    public String getBday() {
        return bday;
    }

    public String getSex() {
        return sex;
    }

    public String getEmail() {
        return email;
    }

    public int getAreaId() {
        return areaId;
    }

    public String getAddress() {
        return address;
    }

    public String getIM() {
        return IM;
    }

    public String getCtime() {
        return ctime;
    }

    public String getIpTime() {
        return ipTime;
    }

    public String getOperator() {
        return operator;
    }

    public String getRemark() {
        return remark;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setIDCode(String IDCode) {
        this.IDCode = IDCode;
    }

    public void setIsMember(String isMember) {
        this.isMember = isMember;
    }

    public void setBday(String bday) {
        this.bday = bday;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setIM(String IM) {
        this.IM = IM;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public void setIpTime(String ipTime) {
        this.ipTime = ipTime;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "BClientInfoInfo{" +
                "cid=" + cid +
                ", unitId=" + unitId +
                ", cname='" + cname + '\'' +
                ", channel='" + channel + '\'' +
                ", tel='" + tel + '\'' +
                ", IDCode='" + IDCode + '\'' +
                ", isMember='" + isMember + '\'' +
                ", bday='" + bday + '\'' +
                ", sex='" + sex + '\'' +
                ", email='" + email + '\'' +
                ", areaId=" + areaId +
                ", address='" + address + '\'' +
                ", IM='" + IM + '\'' +
                ", ctime='" + ctime + '\'' +
                ", ipTime='" + ipTime + '\'' +
                ", operator='" + operator + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
