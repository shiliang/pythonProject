package com.chainmaker.jobservice.api.enums;

public enum JobType{

    FL(1),

    TEE(2),

    MPC(0);
    private final Integer value;
    private JobType(Integer value) {
        this.value = value;
    }

    public Integer getValue(){
        return value;
    }
}
