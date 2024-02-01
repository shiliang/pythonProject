/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-08-2022/8/9 21:41
 * @Description: TODO
 * @Version: 1.0
 */

package com.chainmaker.backendservice.model.dto;

import java.util.ArrayList;

public class ComputingResourceInfo {
    private String id;
    private String name;
    private int systemtype;
    private String configuration;
    private String ip;
    private int status;
    private String createTime;
    private String createUserDID;
    private String createOrgDID;
    private ArrayList<ComputingResourceBoardCardInfo> boardCards;

    public ComputingResourceInfo() {
        this.boardCards = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSystemtype() {
        return systemtype;
    }

    public void setSystemtype(int systemtype) {
        this.systemtype = systemtype;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCreateUserDID() {
        return createUserDID;
    }

    public void setCreateUserDID(String createUserDID) {
        this.createUserDID = createUserDID;
    }

    public String getCreateOrgDID() {
        return createOrgDID;
    }

    public void setCreateOrgDID(String createOrgDID) {
        this.createOrgDID = createOrgDID;
    }

    public ArrayList<ComputingResourceBoardCardInfo> getBoardCards() {
        return boardCards;
    }

    public void setBoardCards(ArrayList<ComputingResourceBoardCardInfo> boardCards) {
        this.boardCards = boardCards;
    }

    public boolean addComputingResourceBoardCardInfo(ComputingResourceBoardCardInfo boardCardInfo) {
        return this.boardCards.add(boardCardInfo);
    }
}
