/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-10-2022/10/17 17:12
 * @Description: TODO
 * @Version: 1.0
 */

package com.chainmaker.backendservice.model.dto;

public class UserInfo {
    private String id;
    private String userName;
    private String password;
    private String orgName;
    private String orgId;
    private String orgDID;
    private String orgPK;
    private String caCert;
    private String tlsCert;
    private String tlsKey;
    private int status;

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

    public String getOrgPK() {
        return orgPK;
    }

    public void setOrgPK(String orgPK) {
        this.orgPK = orgPK;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
