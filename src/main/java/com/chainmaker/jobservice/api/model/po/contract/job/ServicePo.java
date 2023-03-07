package com.chainmaker.jobservice.api.model.po.contract.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.model.bo.job.service.Service;

import java.util.HashMap;

/**
 * @author gaokang
 * @date 2022-09-19 19:09
 * @description:持久化的Service结构
 * @version: 1.0.0
 */

public class ServicePo {
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
    /** 配置参数 */
    private String exposeEndpoints;
    /** 引用的服务 */
    private String referEndpoints;
    /** 自定义配置参数 */
    private String values;
    private String referValues;

    public static ServicePo converterToServicePo(Service service) {
        ServicePo servicePo = new ServicePo();
        servicePo.setId(service.getId());
        servicePo.setVersion(service.getVersion());
        servicePo.setJobID(service.getJobID());
        servicePo.setManual(service.getManual());
        servicePo.setStatus(service.getStatus());
        servicePo.setCreateTime(service.getCreateTime());
        servicePo.setUpdateTime(service.getUpdateTime());
        servicePo.setOrgDID(service.getOrgDID());
        servicePo.setServiceClass(service.getServiceClass());
        servicePo.setServiceName(service.getServiceName());

        for (HashMap<String, String> exposeEndpoint : service.getExposeEndpoints().values()) {
            exposeEndpoint.remove("serviceKey");
        }
        servicePo.setExposeEndpoints(JSON.toJSONString(service.getExposeEndpoints()));
        servicePo.setReferEndpoints(JSON.toJSONString(service.getReferEndpoints()));
        servicePo.setValues(JSON.toJSONString(service.getValues()));
        servicePo.setReferValues(JSON.toJSONString(service.getReferValues()));
        return servicePo;
    }

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

    public String getExposeEndpoints() {
        return exposeEndpoints;
    }

    public void setExposeEndpoints(String exposeEndpoints) {
        this.exposeEndpoints = exposeEndpoints;
    }

    public String getReferEndpoints() {
        return referEndpoints;
    }

    public void setReferEndpoints(String referEndpoints) {
        this.referEndpoints = referEndpoints;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getReferValues() {
        return referValues;
    }

    public void setReferValues(String referValues) {
        this.referValues = referValues;
    }

    @Override
    public String toString() {
        return "ServicePo{" +
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
                ", exposeEndpoints='" + exposeEndpoints + '\'' +
                ", referEndpoints='" + referEndpoints + '\'' +
                ", values='" + values + '\'' +
                ", referValues='" + referValues + '\'' +
                '}';
    }
}
