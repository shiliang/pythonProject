package com.chainmaker.jobservice.core.optimizer;

/**
 * @author gaokang
 * @date 2022-08-19 17:58
 * @description:
 * @version:
 */

public class CycleFoundException extends RuntimeException {

    public CycleFoundException(String message) {
        super(message);
    }

}