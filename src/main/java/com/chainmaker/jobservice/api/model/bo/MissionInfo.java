package com.chainmaker.jobservice.api.model.bo;

import com.chainmaker.jobservice.api.model.vo.ServiceParamsVo;
import com.chainmaker.jobservice.core.analyzer.catalog.MissionDetailVO;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-23 16:33
 * @description:
 * @version:
 */

public class MissionInfo {
    private String id;
    private String name;
    private String description;
    private String sqltext;
    private String version;
    private String createUserDID;
    private String createOrgDID;
    private int modelType;
    private int isStream;
    private String jobID;
    private int status;
    private List<MissionDetailInfo> missionDetailInfos;
    private List<ServiceParamsVo> serviceParams;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public int getModelType() {
        return modelType;
    }

    public void setModelType(int modelType) {
        this.modelType = modelType;
    }

    public int getIsStream() {
        return isStream;
    }

    public void setIsStream(int isStream) {
        this.isStream = isStream;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<MissionDetailInfo> getMissionDetailInfos() {
        return missionDetailInfos;
    }

    public void setMissionDetailInfos(List<MissionDetailInfo> missionDetailInfos) {
        this.missionDetailInfos = missionDetailInfos;
    }

    public List<ServiceParamsVo> getServiceParams() {
        return serviceParams;
    }

    public void setServiceParams(List<ServiceParamsVo> serviceParams) {
        this.serviceParams = serviceParams;
    }
}
