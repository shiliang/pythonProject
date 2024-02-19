package com.chainmaker.jobservice.api.model.po.contract;

import java.util.HashMap;

public class QueryChainByKeyRequest {
   private String key; 

   // Getter method for key
    public String getKey() {
        return key;
    }

    // Setter method for key
    public void setKey(String key) {
        this.key = key;
    }

    // toContractParams method
    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("key", this.getKey().getBytes());
        return contractParams;
    }
}

