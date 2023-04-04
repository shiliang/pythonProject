package com.chainmaker.jobservice.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.aspect.WebLog;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.bo.job.service.Service;
import com.chainmaker.jobservice.api.model.po.contract.*;
import com.chainmaker.jobservice.api.model.po.contract.job.ServicePo;
import com.chainmaker.jobservice.api.response.ContractServiceResponse;
import com.chainmaker.jobservice.api.response.Result;
import com.chainmaker.jobservice.api.response.ResultCode;
import com.chainmaker.jobservice.api.service.BlockchainContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gaokang
 * @date 2022-09-25 21:58
 * @description:与链交互接口
 * @version: 1.0.0
 */

@RequestMapping("/v1")
@RestController
public class ContractController {
    private static final String CONTRACT_NAME = "job_manager";


    @Autowired
    BlockchainContractService blockchainContractService;

//    @WebLog(description = "获取JOB状态")
    @RequestMapping(value = "/jobs/{jobID}/sts", method = RequestMethod.GET)
    public Result queryJobSts(@PathVariable String jobID) {
        JobGetPo jobGetPo = new JobGetPo();
        jobGetPo.setJobID(jobID);
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryJobSts", jobGetPo.toContractParams());
        if (csr.isOk()) {
            return Result.success(csr.toString());
        } else {
            return Result.failure(ResultCode.CONTRACT_FAILED, csr.toString());
        }
    }

    @WebLog(description = "启动JOB")
    @RequestMapping(value = "/jobs/{jobID}/actions/launch", method = RequestMethod.POST)
    public ResponseEntity<String> setJobStsReady(@PathVariable String jobID, @RequestBody String req) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String partyID = JSON.parseObject(req).getString("partyID");
        JobUpdatePo jobUpdatePo = new JobUpdatePo();
        jobUpdatePo.setJobID(jobID);
        jobUpdatePo.setPartyID(partyID);
        jobUpdatePo.setTimestamp(timestamp);
        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME, "SetJobStsReady", jobUpdatePo.toContractParams());

        String response = csr.toString();

        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<String>(response, responseStatus);
    }

    @WebLog(description = "撤销JOB")
    @RequestMapping(value = "/jobs/{jobID}/actions/cancel", method = RequestMethod.POST)
    public ResponseEntity<String> cancelJob(@PathVariable String jobID, @RequestBody String req) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String partyID = JSON.parseObject(req).getString("partyID");
        JobUpdatePo jobUpdatePo = new JobUpdatePo();
        jobUpdatePo.setJobID(jobID);
        jobUpdatePo.setPartyID(partyID);
        jobUpdatePo.setTimestamp(timestamp);
        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME, "CancelJob", jobUpdatePo.toContractParams());
        String response = csr.toString();
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<String>(response, responseStatus);
    }

    @RequestMapping(value = "/jobs/{jobID}/services/{serviceID}/actions/sts", method = RequestMethod.POST)
    public ResponseEntity<JSONObject> updateServiceStatus(@PathVariable String jobID,@PathVariable String serviceID,@RequestBody String req) {
        JSONObject request = JSON.parseObject(req);
        String partyID = request.getString("partyID");
        String status = request.getString("status");
        String timestamp = String.valueOf(System.currentTimeMillis());

        ServiceUpdateStatusPo serviceUpdateStatusPo = new ServiceUpdateStatusPo();
        serviceUpdateStatusPo.setJobID(jobID);
        serviceUpdateStatusPo.setServiceID(serviceID);
        serviceUpdateStatusPo.setSts(status);
        serviceUpdateStatusPo.setPartyID(partyID);
        serviceUpdateStatusPo.setTimestamp(timestamp);

        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME, "UpdateServiceStatus", serviceUpdateStatusPo.toContractParams());
        JSONObject result = csr.toJSON(false);
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<JSONObject>(result, responseStatus);
    }

    @RequestMapping(value = "/jobs/{jobID}/services/{serviceID}", method = RequestMethod.GET)
    public ResponseEntity<Service> queryServiceDetails(@PathVariable String jobID,@PathVariable String serviceID) {
        JobGetServicePo jobGetServicePo = new JobGetServicePo();
        jobGetServicePo.setJobID(jobID);
        jobGetServicePo.setServiceID(serviceID);

        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryServiceDetails", jobGetServicePo.toContractParams());
        ServicePo servicePo = JSON.parseObject(csr.toString(), ServicePo.class, Feature.OrderedField);
        Service service = Service.servicePoToService(servicePo);
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<Service>(service, responseStatus);
    }

//    @WebLog(description = "更改TASK参与方状态")
    @RequestMapping(value = "/jobs/{jobID}/tasks/{taskName}/actions/sts", method = RequestMethod.POST)
    public ResponseEntity<String> setTaskStsINIT(@PathVariable String jobID, @PathVariable String taskName, @RequestBody String req) {
        JSONObject request = JSON.parseObject(req);
        String partyID = request.getString("partyID");
        String status = request.getString("status");
        long time = System.currentTimeMillis();
        String timestamp = String.valueOf(time);
        TaskUpdateStatusPo taskUpdateStatusPo = new TaskUpdateStatusPo();
        taskUpdateStatusPo.setJobID(jobID);
        taskUpdateStatusPo.setTaskName(taskName);
        taskUpdateStatusPo.setPartyID(partyID);
        taskUpdateStatusPo.setSts(status);
        taskUpdateStatusPo.setTimestamp(timestamp);
        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME, "SetTaskStsINIT", taskUpdateStatusPo.toContractParams());
        String response = csr.toString();
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<String>(response, responseStatus);
    }

//    @WebLog(description = "更新TASK参与方信息")
    @RequestMapping(value = "/jobs/{jobID}/tasks/{taskName}/party", method = RequestMethod.POST)
    public ResponseEntity<String> setTaskPtyInfo(@PathVariable String jobID, @PathVariable String taskName,
                                                 @RequestBody String req) {
        JSONObject request = JSON.parseObject(req);
        JobUpdatePartyPo jobUpdatePartyPo = new JobUpdatePartyPo();
        jobUpdatePartyPo.setJobID(jobID);
        jobUpdatePartyPo.setTaskName(taskName);
        jobUpdatePartyPo.setPartyID(request.getString("partyID"));
        PartyInfoPo partyInfoPo = new PartyInfoPo();
        partyInfoPo.setServerInfo(request.getJSONObject("serverInfo"));
        partyInfoPo.setTimestamp(request.getString("timestamp"));
        jobUpdatePartyPo.setParty(partyInfoPo);

        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME, "SetTaskPtyInfo", jobUpdatePartyPo.toContractParams());
        String response = csr.toString();
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<String>(response, responseStatus);
    }

//    @WebLog(description = "更新TASK输出")
    @RequestMapping(value = "/jobs/{jobID}/tasks/{taskName}/output", method = RequestMethod.POST)
    public ResponseEntity<String> setTaskOutput(@PathVariable String jobID, @PathVariable String taskName, @RequestBody String req) {
        JSONObject request = JSON.parseObject(req);
        JobUpdateOutputPo jobUpdateOutputPo = new JobUpdateOutputPo();
        jobUpdateOutputPo.setJobID(jobID);
        jobUpdateOutputPo.setTaskName(taskName);
        jobUpdateOutputPo.setPartyID(request.getString("partyID"));
        jobUpdateOutputPo.setOutput(request.getJSONObject("output"));

        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME, "SetTaskOutput", jobUpdateOutputPo.toContractParams());
        String response = csr.toString();
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<String>(response, responseStatus);
    }

//    @WebLog(description = "获取参与方的JOB")
    @RequestMapping(value = "/jobs/search", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> queryJobsBySts(@RequestParam String partyID, @RequestParam String sts) {
        JobSearchPo jobSearchPo = new JobSearchPo();
        jobSearchPo.setPartyID(partyID);
        String[] temp = sts.split(",");
        String stsstr = "";
        for (int i = 0; i < temp.length; i++) {
            if (i == temp.length - 1) {
                stsstr = stsstr + "\"" + temp[i] + "\"";
            } else {
                stsstr = stsstr + "\"" + temp[i] + "\",";
            }
        }
        jobSearchPo.setSts("[" + stsstr + "]");
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryJobsBySts", jobSearchPo.toContractParams());
        String response = csr.toString();
        JSONArray jobsList = JSONArray.parseArray(response);
        JSONObject res = new JSONObject();
        res.put("jobs", jobsList);
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<JSONObject>(res, responseStatus);
    }

//    @WebLog(description = "获取TASK信息")
    @RequestMapping(value = "/jobs/{jobID}/tasks/{taskName}", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> queryTaskDetails(@PathVariable String jobID, @PathVariable String taskName) {
        JobGetTaskPo jobGetTaskPo = new JobGetTaskPo();
        jobGetTaskPo.setJobID(jobID);
        jobGetTaskPo.setTaskName(taskName);
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryTaskDetails", jobGetTaskPo.toContractParams());
        String response = csr.toString();
        JSONObject res = JSON.parseObject(response, Feature.OrderedField);
        String moduleParamsStr = res.getJSONObject("module").getString("params");
        JSONObject moduleParams = JSON.parseObject(moduleParamsStr, Feature.OrderedField);
        res.getJSONObject("module").put("params", moduleParams);
        for (int i = 0; i < res.getJSONObject("input").getJSONArray("data").size(); i++) {
            String dataParamsStr = res.getJSONObject("input").getJSONArray("data").getJSONObject(i).getString("params");
            JSONObject dataParams = JSON.parseObject(dataParamsStr, Feature.OrderedField);
            res.getJSONObject("input").getJSONArray("data").getJSONObject(i).put("params", dataParams);
        }
        JSONObject taskInfo = new JSONObject();
        taskInfo.put("task", res);
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<JSONObject>(taskInfo, responseStatus);
    }

}
