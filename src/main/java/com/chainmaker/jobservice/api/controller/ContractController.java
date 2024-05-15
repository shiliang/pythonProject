package com.chainmaker.jobservice.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.aspect.WebLog;
import com.chainmaker.jobservice.api.model.PlatformInfo;
import com.chainmaker.jobservice.api.model.Service;
import com.chainmaker.jobservice.api.model.po.contract.*;
import com.chainmaker.jobservice.api.model.po.contract.job.ServicePo;
import com.chainmaker.jobservice.api.model.po.contract.mission.JobCreateReq;
import com.chainmaker.jobservice.api.model.po.contract.mission.MissionGetReq;
import com.chainmaker.jobservice.api.response.ContractServiceResponse;
import com.chainmaker.jobservice.api.response.Result;
import com.chainmaker.jobservice.api.response.ResultCode;
import com.chainmaker.jobservice.api.service.BlockchainContractService;

import com.chainmaker.jobservice.api.service.JobParserService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gaokang
 * @date 2022-09-25 21:58
 * @description:与链交互接口
 * @version: 1.0.0
 */

@Slf4j
@RequestMapping("/v1")
@RestController
public class ContractController {
    private static final String CONTRACT_NAME = "mission_manager";

    @Autowired
    BlockchainContractService blockchainContractService;

    @Autowired
    JobParserService jobParserService;





    @WebLog
    @RequestMapping(value = "/jobs/{jobID}/services/{serviceID}", method = RequestMethod.GET)
    public ResponseEntity<Service> queryServiceDetails(@PathVariable String jobID, @PathVariable String serviceID) {
        JobGetServicePo jobGetServicePo = new JobGetServicePo();
        jobGetServicePo.setJobID(jobID);
        jobGetServicePo.setServiceID(serviceID);

        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryServiceDetails", jobGetServicePo.toContractParams());
        Service service = JSON.parseObject(csr.toString(), Service.class, Feature.OrderedField);
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<Service>(service, responseStatus);
    }

}
