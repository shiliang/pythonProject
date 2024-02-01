/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-08-2022/8/9 16:49
 * @Description: 机密计算 返回参数持久化对象
 * @Version: 1.0
 */

package com.chainmaker.backendservice.model.dto;

public class ConfidentialComputingModelParameterInfo {
    private String fid;
    private String name;
    private String dataSource;
    private int dataType;
    private int isEncrypt;
    private String description;

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getIsEncrypt() {
        return isEncrypt;
    }

    public void setIsEncrypt(int isEncrypt) {
        this.isEncrypt = isEncrypt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
