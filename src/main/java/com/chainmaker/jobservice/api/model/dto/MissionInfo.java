/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-06-2022/6/29 16:15
 * @Description: 任务 数据传输对象
 * @Version: 1.0
 */

package com.chainmaker.backendservice.model.dto;

import java.util.ArrayList;

public class MissionInfo {
    private String id;// Mission id
    private String name;
    private String description;
    private int modelType;
    private int isStream; //机密计算模型是否是连续执行的任务：0 不是连续执行的;1 是连续执行的
    private String sqltext;
    private String jobID; //job ID
    private int status;
    private String createTime;
    private String createUserDID;
    private String createOrgDID;
    private ArrayList<MissionServiceParamInfo> serviceParams;
    private ArrayList<MissionDetailInfo> missionDetailInfos;

    public MissionInfo() {
        this.serviceParams = new ArrayList<>();
        this.missionDetailInfos = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getModelType() {
        return modelType;
    }

    public void setModelType(int modelType) {
        this.modelType = modelType;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSqltext() {
        return sqltext;
    }

    public void setSqltext(String sqltext) {
        this.sqltext = sqltext;
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

    public ArrayList<MissionDetailInfo> getMissionDetailInfos() {
        return missionDetailInfos;
    }

    public void setMissionDetailInfos(ArrayList<MissionDetailInfo> missionDetailInfos) {
        this.missionDetailInfos = missionDetailInfos;
    }

    public int getIsStream() {
        return isStream;
    }

    public void setIsStream(int isStream) {
        this.isStream = isStream;
    }

    public ArrayList<MissionServiceParamInfo> getServiceParams() {
        return serviceParams;
    }

    public void setServiceParams(ArrayList<MissionServiceParamInfo> serviceParams) {
        this.serviceParams = serviceParams;
    }

    public boolean addMissionDetailInfo(MissionDetailInfo missionDetailInfo) {
        return this.missionDetailInfos.add(missionDetailInfo);
    }

    public boolean addMissionServiceParam(MissionServiceParamInfo serviceParam){
        return this.serviceParams.add(serviceParam);
    }

    @Override
    public String toString() {
        return "MissionInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", sqltext='" + sqltext + '\'' +
                ", status=" + status +
                ", createTime='" + createTime + '\'' +
                ", createUserDID='" + createUserDID + '\'' +
                ", createOrgDID='" + createOrgDID + '\'' +
                ", missionDetailInfos=" + missionDetailInfos +
                '}';
    }
}
