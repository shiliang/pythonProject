/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-08-2022/8/9 16:26
 * @Description: 机密计算模型 持久化对象
 * @Version: 1.0
 */

package com.chainmaker.backendservice.model.dto;

import java.util.ArrayList;

public class ConfidentialComputingInfo {
    private String id;
    private String name;
    private String type;
    private String version;
    private String category;
    private String description;
    private byte[] modelFile;
    private String modelFileHash;
    private byte[] sourceFile;
    private String sourceFileHash;
    private String methodName;
    private String methodDescription;
    private int status;
    private String createTime;
    private String createUserDID;
    private String createOrgDID;
    private ArrayList<ConfidentialComputingModelParameterInfo> modelParameters;
    private ArrayList<ConfidentialComputingReturnParameterInfo> returnParameters;

    public ConfidentialComputingInfo() {
        this.modelParameters = new ArrayList<>();
        this.returnParameters = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getModelFile() {
        return modelFile;
    }

    public void setModelFile(byte[] modelFile) {
        this.modelFile = modelFile;
    }

    public String getModelFileHash() {
        return modelFileHash;
    }

    public void setModelFileHash(String modelFileHash) {
        this.modelFileHash = modelFileHash;
    }

    public byte[] getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(byte[] sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getSourceFileHash() {
        return sourceFileHash;
    }

    public void setSourceFileHash(String sourceFileHash) {
        this.sourceFileHash = sourceFileHash;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDescription() {
        return methodDescription;
    }

    public void setMethodDescription(String methodDescription) {
        this.methodDescription = methodDescription;
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

    public String getCreateUserDID() {
        return createUserDID;
    }

    public void setCreateUserDID(String createUserDID) {
        this.createUserDID = createUserDID;
    }

    public String getCreateOrgDID() {
        return createOrgDID;
    }

    public void setCreateOrgDID(String createOrgDID) {
        this.createOrgDID = createOrgDID;
    }

    public ArrayList<ConfidentialComputingModelParameterInfo> getModelParameters() {
        return modelParameters;
    }

    public void setModelParameters(ArrayList<ConfidentialComputingModelParameterInfo> modelParameters) {
        this.modelParameters = modelParameters;
    }

    public ArrayList<ConfidentialComputingReturnParameterInfo> getReturnParameters() {
        return returnParameters;
    }

    public void setReturnParameters(ArrayList<ConfidentialComputingReturnParameterInfo> returnParameters) {
        this.returnParameters = returnParameters;
    }

    public boolean addModelParameterInfo(ConfidentialComputingModelParameterInfo modelParameterInfo) {
        return this.modelParameters.add(modelParameterInfo);
    }

    public boolean addReturnParameterInfo(ConfidentialComputingReturnParameterInfo returnParameterInfo){
        return this.returnParameters.add(returnParameterInfo);
    }
}
