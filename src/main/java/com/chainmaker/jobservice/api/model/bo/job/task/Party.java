package com.chainmaker.jobservice.api.model.bo.job.task;

/**
 * @author gaokang
 * @date 2022-08-13 11:28
 * @description:任务参与方信息
 * @version: 1.0.0
 */

public class Party {
    private String partyID;
    private ServerInfo serverInfo;
    private String status;
    private String timestamp;

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPartyID() {
        return partyID;
    }

    public void setPartyID(String partyID) {
        this.partyID = partyID;
    }

    @Override
    public String toString() {
        return "Party{" +
                "partyID='" + partyID + '\'' +
                ", serverInfo=" + serverInfo +
                ", status='" + status + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
