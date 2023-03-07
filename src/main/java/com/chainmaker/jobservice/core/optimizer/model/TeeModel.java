package com.chainmaker.jobservice.core.optimizer.model;

/**
 * @author gaokang
 * @date 2022-08-21 18:28
 * @description:TEE模型信息
 * @version: 1.0.0
 */

public class TeeModel {
    private String methodName;
    private String domainID;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDomainID() {
        return domainID;
    }

    public void setDomainID(String domainID) {
        this.domainID = domainID;
    }

    @Override
    public String toString() {
        return "TeeModel{" +
                "methodName='" + methodName + '\'' +
                ", domainID='" + domainID + '\'' +
                '}';
    }
}
