package com.chainmaker.jobservice.api.model.bo;

import com.chainmaker.jobservice.api.model.bo.job.service.ReferEndpoint;
import com.chainmaker.jobservice.api.model.bo.job.service.ReferValue;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class ServiceRunner {
    /** 不可为空，service id */
    private String id;
    /** service格式版本 */
    private String version;
    /** Job ID */
    private String jobID;
    /**
     * true人工部署
     * false自动部署K8S接管
     */
    private Boolean manual;
    /** service状态 */
    private String status;
    /** service创建日期时间戳 */
    private String createTime;
    /** service更新日期时间戳 */
    private String updateTime;
    /** 不可为空，服务方的DID */
    private String orgDID;
    /**
     * 不可为空，服务类型：
     * DataSourceAllInOne4RISC 输入服务
     * PrivacyCompute4RISC 计算服务
     * SuccessDownstream4RISC 输出服务
     * FailureDownstream4RISC 重试服务
     */
    private String serviceClass;
    /** 服务名称 */
    private String serviceName;
    @JsonProperty(value = "NodePort")
    private Integer nodePort;
    /** 配置参数 */
    private HashMap<String, HashMap<String, String>> exposeEndpoints;
    /** 引用的服务 */
    private HashMap<String, ReferEndpointRunner> referEndpoints;
    /** 自定义配置参数 */
    private HashMap<String, String> values;
    private HashMap<String, ReferValue> referValues;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public Boolean getManual() {
        return manual;
    }

    public void setManual(Boolean manual) {
        this.manual = manual;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getOrgDID() {
        return orgDID;
    }

    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    @JsonProperty(value = "NodePort")
    public Integer getNodePort() {
        return nodePort;
    }

    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }

    public HashMap<String, HashMap<String, String>> getExposeEndpoints() {
        return exposeEndpoints;
    }

    public void setExposeEndpoints(HashMap<String, HashMap<String, String>> exposeEndpoints) {
        this.exposeEndpoints = exposeEndpoints;
    }

    public HashMap<String, ReferEndpointRunner> getReferEndpoints() {
        return referEndpoints;
    }

    public void setReferEndpoints(HashMap<String, ReferEndpointRunner> referEndpoints) {
        this.referEndpoints = referEndpoints;
    }

    public HashMap<String, String> getValues() {
        return values;
    }

    public void setValues(HashMap<String, String> values) {
        this.values = values;
    }

    public HashMap<String, ReferValue> getReferValues() {
        return referValues;
    }

    public void setReferValues(HashMap<String, ReferValue> referValues) {
        this.referValues = referValues;
    }

    @Override
    public String toString() {
        return "ServiceRunner{" +
                "id='" + id + '\'' +
                ", version='" + version + '\'' +
                ", jobID='" + jobID + '\'' +
                ", manual=" + manual +
                ", status='" + status + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", orgDID='" + orgDID + '\'' +
                ", serviceClass='" + serviceClass + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", nodePort='" + nodePort + '\'' +
                ", exposeEndpoints=" + exposeEndpoints +
                ", referEndpoints=" + referEndpoints +
                ", values=" + values +
                ", referValues=" + referValues +
                '}';
    }
}
