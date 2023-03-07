package com.chainmaker.jobservice.api.response;

/**
 * @author gaokang
 * @date 2022-08-03 18:25
 * @description:解析异常处理
 * @version: 1.0.0
 */
public class ParserException extends RuntimeException {
    private static final String defaultCode = "1005";
    private static final long serialVersionUID = 1L;
    protected String errorCode;
    protected String errorMsg;

    public ParserException() {
        super();
    }

    public ParserException(BaseErrorInfoInterface errorInfoInterface) {
        super(errorInfoInterface.getResultCode());
        this.errorCode = errorInfoInterface.getResultCode();
        this.errorMsg = errorInfoInterface.getResultMsg();
    }

    public ParserException(BaseErrorInfoInterface errorInfoInterface, Throwable cause) {
        super(errorInfoInterface.getResultCode(), cause);
        this.errorCode = errorInfoInterface.getResultCode();
        this.errorMsg = errorInfoInterface.getResultMsg();
    }

    public ParserException(String errorMsg) {
        super(errorMsg);
        this.errorCode = defaultCode;
        this.errorMsg = errorMsg;
    }

    public ParserException(String errorCode, String errorMsg) {
        super(errorCode);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public ParserException(String errorCode, String errorMsg, Throwable cause) {
        super(errorCode, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }


    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}