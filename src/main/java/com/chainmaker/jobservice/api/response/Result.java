package com.chainmaker.jobservice.api.response;

import lombok.Data;
import java.io.Serializable;

public class Result implements Serializable {
    //    private ResultCode resultCode;
    private Integer code;
    private String message;
    /*返回对象*/
    private Object data;

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setResultCode(ResultCode resultCode) {
        this.code = resultCode.code();
        this.message = resultCode.message();
    }

    /**
     * 返回成功，不含对象
     *
     * @return
     */
    public static Result success() {
        Result result = new Result();
        result.setResultCode(ResultCode.SUCCESS);
        return result;
    }

    /**
     * 返回成功，含对象
     *
     * @return
     */
    public static Result success(Object data) {
        Result result = new Result();
        result.setResultCode(ResultCode.SUCCESS);
        result.setData(data);
        return result;
    }

    /**
     * 返回失败，不含对象
     *
     * @param resultCode
     * @return
     */
    public static Result failure(ResultCode resultCode) {
        Result result = new Result();
        result.setResultCode(resultCode);
        return result;
    }

    /**
     * 返回失败，含对象
     *
     * @param resultCode
     * @param
     * @return
     */
    public static Result failure(ResultCode resultCode, String message) {
        Result result = new Result();
        result.setResultCode(resultCode);
        result.setMessage(message);
        return result;
    }

    public static Result failure(Integer errCode, String errMessage) {
        Result result = new Result();
        result.setCode(errCode);
        result.setMessage(errMessage);
        return result;
    }

}