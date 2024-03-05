package com.chainmaker.jobservice.api.model.bo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.model.ExposeEndpoint;
import com.chainmaker.jobservice.api.model.ReferExposeEndpoint;
import com.chainmaker.jobservice.api.model.Service;
import com.chainmaker.jobservice.api.model.bo.job.task.Input;
import com.chainmaker.jobservice.api.model.bo.job.task.TaskInputData;
import com.chainmaker.jobservice.api.model.po.contract.job.ServicePo;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskInputDataPo;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskPo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Data
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
    @JsonProperty(value = "NodePort")
    private Integer nodePort;
    /** 配置参数 */
    private List<ExposeEndpoint> exposeEndpoints;
    /** 引用的服务 */
    private List<ReferExposeEndpoint> referEndpoints;
    /** 自定义配置参数 */
//    private HashMap<String, String> values;
//    private HashMap<String, ReferValue> referValues;


    public static ServiceRunner converterToServiceRunner(ServicePo servicePo, String orgDID) {
        ServiceRunner serviceRunner = null;
        if (servicePo != null) {
            if (StringUtils.equals(servicePo.getOrgId(), orgDID)) {
                serviceRunner = new ServiceRunner();
                serviceRunner.setId(servicePo.getId());
                serviceRunner.setServiceName(servicePo.getServiceName());
                serviceRunner.setServiceClass(servicePo.getServiceClass());
                serviceRunner.setOrgId(servicePo.getOrgId());
                serviceRunner.setStatus(servicePo.getStatus());
                serviceRunner.setExposeEndpoints(servicePo.getExposeEndpointList());
                serviceRunner.setReferEndpoints(servicePo.getReferEndpointList());
                String path = servicePo.getExposeEndpointList().get(0).getPath();
                String[] split = path.split(":");
                if (split.length == 2){
                    Integer port = Integer.valueOf(split[1]);
                    serviceRunner.setNodePort((port));
                }

            }
        }
        return serviceRunner;
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
                ", orgDID='" + orgId + '\'' +
                ", serviceClass='" + serviceClass + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", nodePort='" + nodePort + '\'' +
                ", exposeEndpoints=" + exposeEndpoints +
                ", referEndpoints=" + referEndpoints +
                '}';
    }
}
