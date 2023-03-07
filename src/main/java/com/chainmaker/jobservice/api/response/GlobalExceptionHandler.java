package com.chainmaker.jobservice.api.response;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ParserException.class)
    @ResponseBody
    public Result parserExceptionHandler(HttpServletRequest req, ParserException e){
        return Result.failure(Integer.valueOf(e.getErrorCode()), e.getErrorMsg());
    }

    /**
     * 处理空指针的异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value =NullPointerException.class)
    @ResponseBody
    public Result exceptionHandler(HttpServletRequest req, NullPointerException e){
        return Result.failure(ResultCode.NULL_POINTER_EXCEPTION,e.getMessage());
    }
    /**
     * 处理调用合约异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value =ContractException.class)
    @ResponseBody
    public Result exceptionHandler(HttpServletRequest req, ContractException e){

        return Result.failure(ResultCode.CONTRACT_FAILED,e.getMessage());
    }
    /**
     * 处理调用外部接口异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value =RestException.class)
    @ResponseBody
    public Result exceptionHandler(HttpServletRequest req, RestException e){

        return Result.failure(ResultCode.REST_FAILED,e.getMessage());
    }

    @ExceptionHandler(value = RestClientException.class)
    @ResponseBody
    public Result exceptionHandler(HttpServletRequest req, RestClientException e){

        return Result.failure(ResultCode.REST_FAILED,e.getMessage());
    }
    /**
     * 处理其他异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value =Exception.class)
    @ResponseBody
    public Result exceptionHandler(HttpServletRequest req, Exception e){

        return Result.failure(ResultCode.OTHER_ERRORS,e.getMessage());
    }

}