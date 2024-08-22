package com.chainmaker.jobservice.api.model.vo;


import com.alibaba.fastjson.JSONObject;
import lombok.Data;
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

@Data
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
    private Integer status;
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

    public static ServiceVo fromTemplateFile(String templateID, String templateType) {
        String jsonFilePath = "templates/" + templateID + "/" + templateType + ".json";
        ClassPathResource resource = new ClassPathResource(jsonFilePath);
        if(!resource.exists()){
            return null;
        }
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

    public static void main(String[] args) {
        ServiceVo serviceVo1 = ServiceVo.fromTemplateFile("FL", "HELR");
        ServiceVo serviceVo2 = ServiceVo.fromTemplateFile("FL", "HESB");
        System.out.println(JSONObject.toJSONString(serviceVo1));
        System.out.println(JSONObject.toJSONString(serviceVo2));
    }



//    @Override
//    public String toString() {
//        return "ServiceVo{" +
//                "orgDID='" + orgDID + '\'' +
//                ", serviceName='" + serviceName + '\'' +
//                ", serviceClass='" + serviceClass + '\'' +
//                ", id='" + serviceId + '\'' +
//                ", nodePort='" + nodePort + '\'' +
//                ", version='" + version + '\'' +
//                ", manual=" + manual +
//                ", exposeEndpoints=" + exposeEndpoints +
//                ", referEndpoints=" + referEndpoints +
//                ", values=" + values +
//                ", referValues=" + referValues +
//                '}';
//    }
}
