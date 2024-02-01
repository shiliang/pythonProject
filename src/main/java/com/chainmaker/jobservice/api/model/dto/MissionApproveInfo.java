/**
 * @BelongsProject:chainmaker-mpc-service
 * @BelongsPackage:chainmaker-mpc-service
 * @Author: 王森
 * @CreateTime:2022-07-2022/7/8 17:02
 * @Description: 任务审批 数据传输对象
 * @Version: 1.0
 */

package com.chainmaker.backendservice.model.dto;

public class MissionApproveInfo {
    private String id;  // Mission id
    private String jobID; //job ID
    private String outputServiceAddress;//可为空，输出服务调用地址：如果是主动获取方式，则是计算服务提供的服务地址；如果是回调方式，则是结果接收方提供的服务地址
    private String name;
    private int isStream; //机密计算模型是否是连续执行的任务：0 不是连续执行的;1 是连续执行的
    private String description;
    private String remark;
    private String approveTime;
    private String approveUserDID;
    private String approveOrgDID;
    private int status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getIsStream() {
        return isStream;
    }

    public void setIsStream(int isStream) {
        this.isStream = isStream;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(String approveTime) {
        this.approveTime = approveTime;
    }

    public String getApproveUserDID() {
        return approveUserDID;
    }

    public void setApproveUserDID(String approveUserDID) {
        this.approveUserDID = approveUserDID;
    }

    public String getApproveOrgDID() {
        return approveOrgDID;
    }

    public void setApproveOrgDID(String approveOrgDID) {
        this.approveOrgDID = approveOrgDID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOutputServiceAddress() {
        return outputServiceAddress;
    }

    public void setOutputServiceAddress(String outputServiceAddress) {
        this.outputServiceAddress = outputServiceAddress;
    }

}
