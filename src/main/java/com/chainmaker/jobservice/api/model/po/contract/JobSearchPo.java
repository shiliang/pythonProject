package com.chainmaker.jobservice.api.model.po.contract;

import java.util.HashMap;

/**
 * @author gaokang
 * @date 2022-09-23 04:11
 * @description:查询job
 * @version: 1.0.0
 */

public class JobSearchPo {
    private String partyID;
    private String sts;

    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("partyID", this.getPartyID().getBytes());
        contractParams.put("sts", this.getSts().getBytes());
        return contractParams;
    }

    public String getPartyID() {
        return partyID;
    }

    public void setPartyID(String partyID) {
        this.partyID = partyID;
    }

    public String getSts() {
        return sts;
    }

    public void setSts(String sts) {
        this.sts = sts;
    }
}
