package com.chainmaker.jobservice.api.model.po.contract.job;

import com.chainmaker.jobservice.api.model.ExposeEndpoint;
import com.chainmaker.jobservice.api.model.ReferExposeEndpoint;
import com.chainmaker.jobservice.api.model.Service;
import lombok.Data;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-19 19:09
 * @description:持久化的Service结构
 * @version: 1.0.0
 */

@Data
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
    /** 配置参数 */
    private List<ExposeEndpoint> exposeEndpointList;
    /** 引用的服务 */
    private List<ReferExposeEndpoint> referEndpointList;
    /** 自定义配置参数 */



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
                ", orgDID='" + orgId + '\'' +
                ", serviceClass='" + serviceClass + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", exposeEndpoints='" + exposeEndpointList + '\'' +
                ", referEndpoints='" + referEndpointList + '\'' +
                '}';
    }
}
