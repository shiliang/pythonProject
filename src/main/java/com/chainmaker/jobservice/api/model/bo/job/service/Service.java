package com.chainmaker.jobservice.api.model.bo.job.service;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.model.po.contract.job.ServicePo;
import com.chainmaker.jobservice.api.model.vo.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gaokang
 * @date 2022-09-17 20:00
 * @description:服务类结构
 * @version: 1.0.0
 */
@Data
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
    private String orgId;
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
    private List<ExposeEndpoint> exposeEndpoints;
    /** 引用的服务 */
    private List<ReferExposeEndpoint> referEndpoints;
    /** 自定义配置参数 */
//    private HashMap<String, String> values;
//    private HashMap<String, ReferValue> referValues;

    public static Service servicePoToService(ServicePo servicePo) {
        Service service = new Service();
        service.setId(servicePo.getId());
        service.setVersion(servicePo.getVersion());
        service.setJobID(servicePo.getJobID());
        service.setManual(servicePo.getManual());
        service.setStatus(servicePo.getStatus());
        service.setCreateTime(servicePo.getCreateTime());
        service.setUpdateTime(servicePo.getUpdateTime());
        service.setOrgId(servicePo.getOrgId());

        service.setServiceClass(servicePo.getServiceClass());
        service.setServiceName(servicePo.getServiceName());
        service.setExposeEndpoints(servicePo.getExposeEndpointList());
        service.setReferEndpoints(servicePo.getReferEndpointList());
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
        service.setOrgId(serviceVo.getOrgDID());
        service.setServiceClass(serviceVo.getServiceClass());
        service.setServiceName(serviceVo.getServiceName());
        service.setNodePort(serviceVo.getNodePort());

        List<Value> values = new ArrayList<>();
        for (ValueVo valueVo : serviceVo.getValues()) {
            Value value = new Value();
            value.setValue(valueVo.getValue());
            value.setKey(valueVo.getKey());
            values.add(value);
        }
        List<ExposeEndpoint> exposeEndpoints = new ArrayList<>();
        for (ExposeEndpointVo exposeEndpointVo : serviceVo.getExposeEndpoints()) {
            List<ExposeFormVo> form = exposeEndpointVo.getForm();
            HashMap<String, String> tmpEndPointMap = new HashMap<>();
            for (ExposeFormVo exposeFormVo : form) {
                tmpEndPointMap.put(exposeFormVo.getKey(), exposeFormVo.getValues());
            }
            String jsonString = JSONObject.toJSONString(tmpEndPointMap);
            ExposeEndpoint exposeEndpoint = JSONObject.parseObject(jsonString, ExposeEndpoint.class);
            exposeEndpoint.setValueList(values);
            exposeEndpoints.add(exposeEndpoint);
        }
        service.setExposeEndpoints(exposeEndpoints);

        Map<String, List<ReferValue>> referValueMap = new HashMap<>();
        for (ReferValueVo referValueVo : serviceVo.getReferValues()) {
            ReferValue referValue = new ReferValue();
            referValue.setKey(referValueVo.getReferKey());
            referValue.setReferServiceID(referValueVo.getReferServiceID());
            List<ReferValue> referValueList = referValueMap.getOrDefault(referValue, new ArrayList<>());
            referValueList.add(referValue);
            referValueMap.put(referValueVo.getReferServiceID(), referValueList);
        }

        List<ReferExposeEndpoint> referEndpoints = new ArrayList<>();
        for (ReferEndpoint referEndpoint : serviceVo.getReferEndpoints()) {
            ReferExposeEndpoint referExposeEndpoint = new ReferExposeEndpoint();
            referExposeEndpoint.setReferEndpointName(referEndpoint.getReferEndpointName());
            referExposeEndpoint.setName(referEndpoint.getName());
            referExposeEndpoint.setProtocol(referEndpoint.getProtocol());
            referExposeEndpoint.setReferServiceId(referEndpoint.getReferServiceID());
            List<ReferValue> referValueList = referValueMap.get(referEndpoint.getReferServiceID());
            referExposeEndpoint.setReferValueList(referValueList);
            referEndpoints.add(referExposeEndpoint);
        }
        service.setReferEndpoints(referEndpoints);
        return service;
    }

    public Integer getNodePort() {
        ExposeEndpoint exposeEndpoint = exposeEndpoints.get(0);
        String[] split = exposeEndpoint.getAddress().split(":");
        String portStr = split[1];
        return Integer.parseInt(portStr);
    }

    //    public void setExposeEndpoints( exposeEndpointsString) {
//        HashMap<String, HashMap<String, String>> exposeEndpoints = new HashMap<>();
//        JSONObject data = JSONObject.parseObject(exposeEndpointsString);
//        for (String key : data.keySet()) {
//            HashMap<String, String> value = JSONObject.parseObject(data.getString(key), HashMap.class);
//            exposeEndpoints.put(key, value);
//        }
//        this.exposeEndpoints = exposeEndpoints;
//    }

//    public void setReferEndpoints( referEndpointsString) {
//        HashMap<String, ReferEndpoint> referEndpoints = new HashMap<>();
//        JSONObject data = JSONObject.parseObject(referEndpointsString);
//        for (String key : data.keySet()) {
//            ReferEndpoint value = JSONObject.parseObject(data.getString(key), ReferEndpoint.class);
//            referEndpoints.put(key, value);
//        }
//        this.referEndpoints = referEndpoints;
//    }

//    public HashMap<String, String> getValues() {
//        return values;
//    }

//    public void setValues(HashMap<String, String> values) {
//        this.values = values;
//    }
//    public void setValues(String valuesString) {
//        HashMap<String, String> values = new HashMap<>();
//        JSONObject data = JSONObject.parseObject(valuesString);
//        for (String key : data.keySet()) {
//            values.put(key, data.getString(key));
//        }
//        this.values = values;
//    }
//
//    public HashMap<String, ReferValue> getReferValues() {
//        return referValues;
//    }
//
//    public void setReferValues(HashMap<String, ReferValue> referValues) {
//        this.referValues = referValues;
//    }
//    public void setReferValues(String referValuesString) {
//        HashMap<String, ReferValue> referValues = new HashMap<>();
//        JSONObject data = JSONObject.parseObject(referValuesString);
//        for (String key : data.keySet()) {
//            ReferValue value = JSONObject.parseObject(data.getString(key), ReferValue.class);
//            referValues.put(key, value);
//        }
//        this.referValues = referValues;
//    }

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
                ", orgDID='" + orgId + '\'' +
                ", serviceClass='" + serviceClass + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", exposeEndpoints=" + exposeEndpoints +
                ", referEndpoints=" + referEndpoints +
                '}';
    }
}
