package com.chainmaker.jobservice.api.model.po.contract.mission;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashMap;

/**
 * @author gaokang
 * @date 2022-09-23 03:14
 * @description:根据jobID查询
 * @version: 1.0.0
 */

public class MissionGetReq {
    @JSONField(name = "partyID")
    private String partyID;
    @JSONField(name = "missionID")
    private String missionID;
    @JSONField(name = "status")
    private String status;
    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("partyID", this.getPartyId().getBytes());
        contractParams.put("status", this.getStatus().getBytes());
        return contractParams;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMissionID(String missionID) {
        this.missionID = missionID;
    }

    public String getMissionID() {
        return missionID;
    }

    public void setPartyID(String partyID){
        this.partyID = partyID;
    }

    public String getPartyId(){
        return partyID;
    }
}
