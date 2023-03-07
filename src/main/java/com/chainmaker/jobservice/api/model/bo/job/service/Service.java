package com.chainmaker.jobservice.api.model.bo.job.service;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.model.po.contract.job.ServicePo;
import com.chainmaker.jobservice.api.model.vo.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-17 20:00
 * @description:服务类结构
 * @version: 1.0.0
 */

public class Service {
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
    private Integer nodePort;
    /** 配置参数 */
    private HashMap<String, HashMap<String, String>> exposeEndpoints;
    /** 引用的服务 */
    private HashMap<String, ReferEndpoint> referEndpoints;
    /** 自定义配置参数 */
    private HashMap<String, String> values;
    private HashMap<String, ReferValue> referValues;

    public static Service servicePoToService(ServicePo servicePo) {
        Service service = new Service();
        service.setId(servicePo.getId());
        service.setVersion(servicePo.getVersion());
        service.setJobID(servicePo.getJobID());
        service.setManual(servicePo.getManual());
        service.setStatus(servicePo.getStatus());
        service.setCreateTime(servicePo.getCreateTime());
        service.setUpdateTime(servicePo.getUpdateTime());
        service.setOrgDID(servicePo.getOrgDID());

        service.setServiceClass(servicePo.getServiceClass());
        service.setServiceName(servicePo.getServiceName());

        service.setExposeEndpoints(servicePo.getExposeEndpoints());
        service.setReferEndpoints(servicePo.getReferEndpoints());
        if (!servicePo.getValues().equals("{}")) {
            service.setValues(servicePo.getValues());
        }

        if (!servicePo.getReferValues().equals("{}")) {
            service.setReferValues(servicePo.getReferValues());
        }
        return service;
    }

    public static Service serviceVoToService(ServiceVo serviceVo, String jobID) {
        Service service = new Service();
        service.setId(serviceVo.getId());
        service.setVersion(serviceVo.getVersion());
        service.setJobID(jobID);
        service.setManual(serviceVo.getManual());
        service.setStatus(serviceVo.getStatus());
        service.setCreateTime(serviceVo.getCreateTime());
        service.setUpdateTime(serviceVo.getUpdateTime());
        service.setOrgDID(serviceVo.getOrgDID());
        service.setServiceClass(serviceVo.getServiceClass());
        service.setServiceName(serviceVo.getServiceName());
        service.setNodePort(serviceVo.getNodePort());

        HashMap<String, HashMap<String, String>> exposeEndpoints = new HashMap<>();
        for (ExposeEndpointVo exposeEndpointVo : serviceVo.getExposeEndpoints()) {
            HashMap<String, String> exposeEndpoint = new HashMap<>();
            for (ExposeFormVo exposeFormVo : exposeEndpointVo.getForm()) {
                exposeEndpoint.put(exposeFormVo.getKey(), exposeFormVo.getValues());
            }
            exposeEndpoints.put(exposeEndpointVo.getName(), exposeEndpoint);
        }
        service.setExposeEndpoints(exposeEndpoints);

        HashMap<String, ReferEndpoint> referEndpoints = new HashMap<>();
        for (ReferEndpoint referEndpoint : serviceVo.getReferEndpoints()) {
            referEndpoints.put(referEndpoint.getName(), referEndpoint);
        }
        service.setReferEndpoints(referEndpoints);

        HashMap<String, String> values = new HashMap<>();
        for (ValueVo valueVo : serviceVo.getValues()) {
            values.put(valueVo.getKey(), valueVo.getValue());
        }
        service.setValues(values);
        HashMap<String, ReferValue> referValues = new HashMap<>();
        for (ReferValueVo referValueVo : serviceVo.getReferValues()) {
            ReferValue referValue = new ReferValue();
            referValue.setKey(referValueVo.getReferKey());
            referValue.setReferServiceID(referValueVo.getReferServiceID());
            referValues.put(referValueVo.getKey(), referValue);
        }
        service.setReferValues(referValues);
        return service;
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

    public HashMap<String, HashMap<String, String>> getExposeEndpoints() {
        return exposeEndpoints;
    }

    public Integer getNodePort() {
        return nodePort;
    }

    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }

    public void setExposeEndpoints(HashMap<String, HashMap<String, String>> exposeEndpoints) {
        this.exposeEndpoints = exposeEndpoints;
    }
    public void setExposeEndpoints(String exposeEndpointsString) {
        HashMap<String, HashMap<String, String>> exposeEndpoints = new HashMap<>();
        JSONObject data = JSONObject.parseObject(exposeEndpointsString);
        for (String key : data.keySet()) {
            HashMap<String, String> value = JSONObject.parseObject(data.getString(key), HashMap.class);
            exposeEndpoints.put(key, value);
        }
        this.exposeEndpoints = exposeEndpoints;
    }

    public HashMap<String, ReferEndpoint> getReferEndpoints() {
        return referEndpoints;
    }

    public void setReferEndpoints(HashMap<String, ReferEndpoint> referEndpoints) {
        this.referEndpoints = referEndpoints;
    }
    public void setReferEndpoints(String referEndpointsString) {
        HashMap<String, ReferEndpoint> referEndpoints = new HashMap<>();
        JSONObject data = JSONObject.parseObject(referEndpointsString);
        for (String key : data.keySet()) {
            ReferEndpoint value = JSONObject.parseObject(data.getString(key), ReferEndpoint.class);
            referEndpoints.put(key, value);
        }
        this.referEndpoints = referEndpoints;
    }

    public HashMap<String, String> getValues() {
        return values;
    }

    public void setValues(HashMap<String, String> values) {
        this.values = values;
    }
    public void setValues(String valuesString) {
        HashMap<String, String> values = new HashMap<>();
        JSONObject data = JSONObject.parseObject(valuesString);
        for (String key : data.keySet()) {
            values.put(key, data.getString(key));
        }
        this.values = values;
    }

    public HashMap<String, ReferValue> getReferValues() {
        return referValues;
    }

    public void setReferValues(HashMap<String, ReferValue> referValues) {
        this.referValues = referValues;
    }
    public void setReferValues(String referValuesString) {
        HashMap<String, ReferValue> referValues = new HashMap<>();
        JSONObject data = JSONObject.parseObject(referValuesString);
        for (String key : data.keySet()) {
            ReferValue value = JSONObject.parseObject(data.getString(key), ReferValue.class);
            referValues.put(key, value);
        }
        this.referValues = referValues;
    }

    @Override
    public String toString() {
        return "Service{" +
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
                ", exposeEndpoints=" + exposeEndpoints +
                ", referEndpoints=" + referEndpoints +
                ", values=" + values +
                ", referValues=" + referValues +
                '}';
    }
}
