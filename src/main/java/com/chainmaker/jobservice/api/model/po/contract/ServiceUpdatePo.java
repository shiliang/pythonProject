package com.chainmaker.jobservice.api.model.po.contract;

import com.alibaba.fastjson.JSON;
import com.chainmaker.jobservice.api.model.bo.job.service.Service;
import com.chainmaker.jobservice.api.model.vo.ServiceVo;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-26 10:43
 * @description:
 * @version:
 */

public class ServiceUpdatePo {
    private List<ServiceParamsPo> serviceParamsPoList;


    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("services", JSON.toJSONString(this.getServiceParamsPoList()).getBytes(StandardCharsets.UTF_8));
        return contractParams;
    }

    public List<ServiceParamsPo> getServiceParamsPoList() {
        return serviceParamsPoList;
    }

    public void setServiceParamsPoList(List<ServiceParamsPo> serviceParamsPoList) {
        this.serviceParamsPoList = serviceParamsPoList;
    }
}
