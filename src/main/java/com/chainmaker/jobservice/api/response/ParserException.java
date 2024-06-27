package com.chainmaker.jobservice.api.response;

import lombok.Data;

/**
 * @author gaokang
 * @date 2022-08-03 18:25
 * @description:解析异常处理
 * @version: 1.0.0
 */
@Data
public class ParserException extends RuntimeException {
    private static final String defaultCode = "1005";
    private static final long serialVersionUID = 1L;
    protected String errorCode;
    protected String errorMsg;


    public ParserException(String errorMsg) {
        super(errorMsg);
        this.errorCode = defaultCode;
        this.errorMsg = errorMsg;
    }


    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}