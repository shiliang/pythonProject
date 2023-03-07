package com.chainmaker.jobservice.api.model.po.data;

public class UserInfo {
    private String id;
    private String userName;
    private String password;
    private String orgName;
    private String orgId;
    private String orgDID;
    private String orgPk;
    private String caCert;
    private String tlsCert;
    private String tlsKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
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

    public String getOrgPk() {
        return orgPk;
    }

    public void setOrgPk(String orgPk) {
        this.orgPk = orgPk;
    }

    public String getCaCert() {
        return caCert;
    }

    public void setCaCert(String caCert) {
        this.caCert = caCert;
    }

    public String getTlsCert() {
        return tlsCert;
    }

    public void setTlsCert(String tlsCert) {
        this.tlsCert = tlsCert;
    }

    public String getTlsKey() {
        return tlsKey;
    }

    public void setTlsKey(String tlsKey) {
        this.tlsKey = tlsKey;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id='" + id + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", orgName='" + orgName + '\'' +
                ", orgId='" + orgId + '\'' +
                ", orgDID='" + orgDID + '\'' +
                ", orgPk='" + orgPk + '\'' +
                ", caCert='" + caCert + '\'' +
                ", tlsCert='" + tlsCert + '\'' +
                ", tlsKey='" + tlsKey + '\'' +
                '}';
    }
}
