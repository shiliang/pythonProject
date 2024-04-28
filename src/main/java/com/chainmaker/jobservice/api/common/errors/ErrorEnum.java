package com.chainmaker.jobservice.api.common.errors;

public enum ErrorEnum {
    // 缺省错误码定义

    ErrCodeProtoToBean(1008, "Proto转Bean失败"),

    ErrCodeLocalResourcePlatformInformationNotFound(1009, "平台信息获取失败"),



    ;

    private Integer errorCode;
    private String errorMsg;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    ErrorEnum(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

}