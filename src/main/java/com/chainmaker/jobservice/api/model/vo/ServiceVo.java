package com.chainmaker.jobservice.api.model.vo;


import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.model.bo.job.service.ReferEndpoint;
import org.antlr.v4.runtime.CharStreams;
import org.apache.commons.codec.Charsets;
import org.springframework.core.io.ClassPathResource;


import java.io.*;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-22 00:30
 * @description:用户填写表单
 * @version: 1.0.0
 */

public class ServiceVo {
    /** 不可为空，service id */
    private String serviceId;
    /** service格式版本 */
    private String version;

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
    private List<ExposeEndpointVo> exposeEndpoints;
    private List<ReferEndpoint> referEndpoints;
    private List<ValueVo> values;
    private List<ReferValueVo> referValues;

    public static ServiceVo templateToServiceVo(String templateID, String templateType) {
        String jsonFilePath = "templates/" + templateID + "/" + templateType + ".json";
        ClassPathResource resource = new ClassPathResource(jsonFilePath);

        // 获取文件流
        InputStream inputStream = null;
        try {
            inputStream = resource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null == inputStream) {
            return null;
        }
        String serviceVoStr = null;
        try {
            serviceVoStr = CharStreams.fromStream(inputStream, Charsets.UTF_8).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSONObject.parseObject(serviceVoStr, ServiceVo.class);
    }

//    public static ServiceVo serviceToServiceVo(Service service, String templateId) {
//        ServiceVo serviceVo = ServiceVo.templateToServiceVo(templateId, service.getServiceClass());
//        serviceVo.setId(service.getId());
//        serviceVo.setVersion(service.getVersion());
//        serviceVo.setManual(service.getManual());
//        serviceVo.setStatus(service.getStatus());
//        serviceVo.setCreateTime(service.getCreateTime());
//        serviceVo.setUpdateTime(service.getUpdateTime());
//        serviceVo.setOrgDID(service.getOrgId());
//        serviceVo.setServiceClass(service.getServiceClass());
//        serviceVo.setServiceName(service.getServiceName());
//        serviceVo.setNodePort(service.getNodePort());
//
//        for (ExposeEndpointVo exposeEndpointVo : serviceVo.getExposeEndpoints()) {
//            for (String key : service.getExposeEndpoints().keySet()) {
//                if (Objects.equals(exposeEndpointVo.getName(), key)) {
//                    for (ExposeFormVo exposeFormVo : exposeEndpointVo.getForm()) {
//                        if (service.getExposeEndpoints().get(key).get(exposeFormVo.getKey()) == null) {
//                            exposeFormVo.setValues("");
//                        } else {
//                            exposeFormVo.setValues(service.getExposeEndpoints().get(key).get(exposeFormVo.getKey()));
//                        }
//                    }
//                }
//            }
//        }
//
//
//
//        List<ReferEndpoint> referEndpoints = new ArrayList<>(service.getReferEndpoints());
//        serviceVo.setReferEndpoints(referEndpoints);
//
//        List<ValueVo> valueVos = new ArrayList<>();
//        for (String key : service.getValues().keySet()) {
//            ValueVo valueVo = new ValueVo();
//            valueVo.setKey(key);
//            valueVo.setValue(service.getValues().get(key));
//            valueVos.add(valueVo);
//        }
//        serviceVo.setValues(valueVos);
//
//        List<ReferValueVo> referValueVos = new ArrayList<>();
//        for (String key : service.getReferValues().keySet()) {
//            ReferValueVo referValueVo = new ReferValueVo();
//            referValueVo.setKey(key);
//            referValueVos.add(referValueVo);
//
//        }
//        serviceVo.setReferValues(referValreueVos);
//        return serviceVo;
//
//    }



    public String getOrgDID() {
        return orgDID;
    }

    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getServiceId() {
        return serviceId;
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

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public List<ExposeEndpointVo> getExposeEndpoints() {
        return exposeEndpoints;
    }

    public void setExposeEndpoints(List<ExposeEndpointVo> exposeEndpoints) {
        this.exposeEndpoints = exposeEndpoints;
    }

    public List<ReferEndpoint> getReferEndpoints() {
        return referEndpoints;
    }

    public void setReferEndpoints(List<ReferEndpoint> referEndpoints) {
        this.referEndpoints = referEndpoints;
    }

    public List<ValueVo> getValues() {
        return values;
    }

    public void setValues(List<ValueVo> values) {
        this.values = values;
    }

    public List<ReferValueVo> getReferValues() {
        return referValues;
    }

    public void setReferValues(List<ReferValueVo> referValues) {
        this.referValues = referValues;
    }

    public Integer getNodePort() {
        return nodePort;
    }

    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getManual() {
        return manual;
    }

    public void setManual(Boolean manual) {
        this.manual = manual;
    }

    @Override
    public String toString() {
        return "ServiceVo{" +
                "orgDID='" + orgDID + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceClass='" + serviceClass + '\'' +
                ", id='" + serviceId + '\'' +
                ", nodePort='" + nodePort + '\'' +
                ", version='" + version + '\'' +
                ", manual=" + manual +
                ", exposeEndpoints=" + exposeEndpoints +
                ", referEndpoints=" + referEndpoints +
                ", values=" + values +
                ", referValues=" + referValues +
                '}';
    }
}
