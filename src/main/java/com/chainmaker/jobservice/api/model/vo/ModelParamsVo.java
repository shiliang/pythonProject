package com.chainmaker.jobservice.api.model.vo;

/**
 * @author gaokang
 * @date 2022-10-20 18:57
 * @description:
 * @version:
 */
public class ModelParamsVo {
    private String name;
    private String type;
    private String version;
    private String createOrgDID;
    private String methodName;

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

    public String getCreateOrgDID() {
        return createOrgDID;
    }

    public void setCreateOrgDID(String createOrgDID) {
        this.createOrgDID = createOrgDID;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "ModelParamsVo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", version='" + version + '\'' +
                ", createOrgDID='" + createOrgDID + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
