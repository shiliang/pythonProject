package com.chainmaker.jobservice.api.response;

public enum ResultCode {

    SUCCESS(200, "成功"),
    CONTRACT_FAILED(1001, "链请求失败"),
    REST_FAILED(1007, "请求后台数据失败"),
    NO_MATCHING_DATA_FOUND(404,"未查询到符合条件的数据"),
    NULL_POINTER_EXCEPTION(401,"空指针异常"),
    PARAM_IS_INVALID(1002, "参数无效"),
    PARAM_IS_BLANK(1003, "参数错误"),
    PARAM_TYPE_BING_ERROR(1004, "参数类型错误"),
    OTHER_ERRORS(1005,"其他错误"),
    CUSTOM_EXCEPTION(1006,"自定义错误");

    private Integer code;

    private String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }

}