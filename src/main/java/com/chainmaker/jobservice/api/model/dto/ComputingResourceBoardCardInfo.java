/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-08-2022/8/9 21:44
 * @Description: TODO
 * @Version: 1.0
 */

package com.chainmaker.backendservice.model.dto;

public class ComputingResourceBoardCardInfo {
    private String fid;
    private String sequence;
    private String type;
    private String configuration;
    private String version;

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
