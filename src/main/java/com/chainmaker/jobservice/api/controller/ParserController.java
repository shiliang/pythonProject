package com.chainmaker.jobservice.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.aspect.WebLog;
import com.chainmaker.jobservice.api.model.bo.*;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.po.contract.JobGetPo;
import com.chainmaker.jobservice.api.model.po.contract.JobInfoPo;
import com.chainmaker.jobservice.api.model.po.contract.JobUpdateStsPo;
import com.chainmaker.jobservice.api.model.po.contract.ServiceUpdatePo;
import com.chainmaker.jobservice.api.model.vo.*;
import com.chainmaker.jobservice.api.response.ContractServiceResponse;
import com.chainmaker.jobservice.api.response.Result;
import com.chainmaker.jobservice.api.response.ResultCode;
import com.chainmaker.jobservice.api.service.BlockchainContractService;
import com.chainmaker.jobservice.api.service.JobParserService;
import com.chainmaker.jobservice.api.util.CsvUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;


@RequestMapping("/v1")
@RestController
public class ParserController {
    private static final String WASM_PATH = "/home/workspace/sdk/wasm/";
//    private static final String WASM_PATH = "D://";
    private static final String CONTRACT_NAME = "job_manager";
    private static final String CONTRACT_FILE_PATH = "job_manager.wasm";
    private static final String CONTRACT_NAME_2 = "catalog_contract2";
    private static final String CONTRACT_FILE_PATH_2 = "catalog_contract.wasm";
    private static final String CONTRACT_NAME_3 = "DID";
    private static final String CONTRACT_FILE_PATH_3 = "mock_did.wasm";

    @Autowired
    BlockchainContractService blockchainContractService;
    @Autowired
    JobParserService jobParserService;

    @RequestMapping(value = "/install/{id}", method = RequestMethod.GET)
    public void install(@PathVariable String id) {
        switch (id) {
            case "1":
                blockchainContractService.createContract(WASM_PATH + CONTRACT_FILE_PATH, CONTRACT_NAME, null);
                break;
            case "2":
                blockchainContractService.createContract(WASM_PATH + CONTRACT_FILE_PATH_2, CONTRACT_NAME_2, null);
                break;
            case "3":
                blockchainContractService.createContract(WASM_PATH + CONTRACT_FILE_PATH_3, CONTRACT_NAME_3, null);
                break;
            default:
        }
    }

    @RequestMapping(value = "/upgrade/{id}/{version}", method = RequestMethod.GET)
    public void upgrade(@PathVariable String id, @PathVariable String version) {
        switch (id) {
            case "1":
                blockchainContractService.upgradeContract(WASM_PATH + CONTRACT_FILE_PATH, CONTRACT_NAME, version, null);
                break;
            case "2":
                blockchainContractService.upgradeContract(WASM_PATH + CONTRACT_FILE_PATH_2, CONTRACT_NAME_2, version, null);
                break;
            case "3":
                blockchainContractService.upgradeContract(WASM_PATH + CONTRACT_FILE_PATH_3, CONTRACT_NAME_3, version, null);
                break;
            default:
        }

    }
    @WebLog(description = "预生成DAG")
    @RequestMapping(value = "/preview/dag", method = RequestMethod.POST)
    public Result jobPreview(@RequestBody String req) {
        SqlVo sqlVo = JSONObject.parseObject(req, SqlVo.class, Feature.OrderedField);
        if (sqlVo.getSqltext().contains("?")) {
            sqlVo.setIsStream(1);
        }
        JobGraphVo jobGraphVo= jobParserService.jobPreview(sqlVo);
        return Result.success(jobGraphVo);
    }

    @WebLog(description = "提交DAG")
    @RequestMapping(value = "/commit/dag", method = RequestMethod.POST)
    public Result jobCommit(@RequestBody String req) {
        System.out.println("req: " + req);
        JobGraphVo jobGraphVo = JSONObject.parseObject(req, JobGraphVo.class, Feature.OrderedField);
        System.out.println("jobGraphVo: " + jobGraphVo);
        MissionInfoVo missionInfoVo = jobParserService.jobCommit(jobGraphVo);
        return Result.success(missionInfoVo);
    }

    @WebLog(description = "创建JOB")
    @RequestMapping(value = "/jobs", method = RequestMethod.POST)
    public ResponseEntity<String> jobCreate(@RequestBody String req) {
        MissionInfo missionInfo = JSONObject.parseObject(req, MissionInfo.class, Feature.OrderedField);
        JobInfo jobInfo = jobParserService.jobCreate(missionInfo);
        String jobID = jobInfo.getJob().getJobID();
        JobInfoPo jobInfoPo = JobInfoPo.converterToJobInfoPo(jobInfo);
        System.out.println("jobInfoPo: " + jobInfoPo);
        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME, "CreateJobByApplication", jobInfoPo.toContractParams());
        csr.setJsonResult("{\"jobID\"" + ":\"" + jobID + "\"}");
        String response = csr.toString();
        HttpStatus responseStatus = HttpStatus.OK;
        if (csr.isOk()) {
            jobParserService.delete(jobID);
        } else {
            responseStatus = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<String>(response, responseStatus);
    }

    @WebLog(description = "查询Job待审批信息")
    @RequestMapping(value = "/jobs/dag/{jobID}", method = RequestMethod.GET)
    public Result getJobApproval(@PathVariable String jobID) {
        JobGetPo jobGetPo = new JobGetPo();
        jobGetPo.setJobID(jobID);
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryJobDetails", jobGetPo.toContractParams());
        JobInfoPo jobInfoPo = JSONObject.parseObject(csr.toString(), JobInfoPo.class, Feature.OrderedField);
        JobGraphVo jobGraphVo = jobParserService.getJobApproval(jobInfoPo);
        return Result.success(jobGraphVo);
    }
    @WebLog(description = "查询JOB详细信息")
    @RequestMapping(value = "/jobs/info/{jobID}", method = RequestMethod.GET)
    public Result getJobInfo(@PathVariable String jobID) {
        JobGetPo jobGetPo = new JobGetPo();
        jobGetPo.setJobID(jobID);
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryJobDetails", jobGetPo.toContractParams());
        JobInfoPo jobInfoPo = JSONObject.parseObject(csr.toString(), JobInfoPo.class, Feature.OrderedField);
        JobGraphVo jobGraphVo = jobParserService.getJobInfo(jobInfoPo);
        return Result.success(jobGraphVo);
    }

    @WebLog(description = "更新service信息")
    @RequestMapping(value = "/service/list/update", method = RequestMethod.POST)
    public Result updateService(@RequestBody String req) {
        ServiceUpdateVo serviceUpdateVo = JSONObject.parseObject(req, ServiceUpdateVo.class, Feature.OrderedField);
        ServiceUpdatePo serviceUpdatePo = jobParserService.updateService(serviceUpdateVo);
        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME, "UpdateServices", serviceUpdatePo.toContractParams());
        JSONObject res = csr.toJSON(false);
        return Result.success(res);

    }

    // @WebLog(description = "获取JOB信息")
    @RequestMapping(value = "/jobs/{jobID}", method = RequestMethod.GET)
    public ResponseEntity<JobRunner> getJobRunner(@PathVariable String jobID) {
        JobGetPo jobGetPo = new JobGetPo();
        jobGetPo.setJobID(jobID);
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryJobDetails", jobGetPo.toContractParams());
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        System.out.println("csr: " + csr);
        JobInfoPo jobInfoPo = JSONObject.parseObject(csr.toString(), JobInfoPo.class, Feature.OrderedField);
        JobRunner jobRunner = jobParserService.getJobRunner(jobInfoPo);
        return new ResponseEntity<JobRunner>(jobRunner, responseStatus);
    }

    @WebLog(description = "检查更新JOB状态")
    @RequestMapping(value = "/jobs/{jobID}/actions/sts", method = RequestMethod.GET)
    public ResponseEntity<String> jobStatusCheckAndUpdate(@PathVariable String jobID) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        JobUpdateStsPo jobUpdateStsPo = new JobUpdateStsPo();
        jobUpdateStsPo.setJobID(jobID);
        jobUpdateStsPo.setTimestamp(timestamp);
        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME, "JobStatusCheckAndUpdate", jobUpdateStsPo.toContractParams());
        String response = csr.toString();
        JSONObject result = new JSONObject();
        result.put("result", response);
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<String>(result.toJSONString(), responseStatus);
    }
    @WebLog(description = "获取job模型")
    @RequestMapping(value = "/model/name/{jobID}", method = RequestMethod.GET)
    public ResponseEntity<String> getModelByJobId(@PathVariable String jobID) {
        JobGetPo jobGetPo = new JobGetPo();
        jobGetPo.setJobID(jobID);
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryJobCommon", jobGetPo.toContractParams());
        String response = csr.toString();
        JSONObject res = JSONObject.parseObject(csr.toString(), Feature.OrderedField);
        JSONObject result = new JSONObject();
        result.put("result", res.get("method_name"));
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<String>(result.toJSONString(), responseStatus);
    }


    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public Result queryVersion() {
        HashMap<String, byte[]> params = new HashMap<>();
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "version", params);

        if (csr.isOk()) {
            return Result.success(csr.toString());
        } else {
            return Result.failure(ResultCode.CONTRACT_FAILED, csr.toString());
        }
    }
    @RequestMapping(value = "/catalog/version", method = RequestMethod.GET)
    public Result queryCatalogVersion() {
        HashMap<String, byte[]> params = new HashMap<>();
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME_2, "version", params);

        if (csr.isOk()) {
            return Result.success(csr.toString());
        } else {
            return Result.failure(ResultCode.CONTRACT_FAILED, csr.toString());
        }
    }
    @RequestMapping(value = "/did/version", method = RequestMethod.GET)
    public Result queryDidVersion() {
        HashMap<String, byte[]> params = new HashMap<>();
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME_3, "version", params);

        if (csr.isOk()) {
            return Result.success(csr.toString());
        } else {
            return Result.failure(ResultCode.CONTRACT_FAILED, csr.toString());
        }
    }

    @WebLog(description = "更新结果")
    @RequestMapping(value = "/result", method = RequestMethod.POST)
    public Result putResult(@RequestBody String req) {
        JSONObject request = JSON.parseObject(req);
        String jobId = request.getString("jobId");
        String data = request.getString("data");
        String key = request.getString("key");

        String filePath = "result/" + jobId;

        List<String> valueList = List.of(StringUtils.strip(data, "[]").split("#@#"));
//        List<String> valueList = List.of(StringUtils.strip(data, "[]"));
        File file = new File(filePath);
        if (!file.exists()) {
            CsvUtil.writeToCsv(key, valueList, filePath, false);
        } else {
            List<String> dataList = new ArrayList<>();
            dataList.add(key);
            dataList.addAll(valueList);
            List<String> result = CsvUtil.readFromCsv(filePath);

            List<String> inputs = new ArrayList<>();
            String headerString = "";
            for (int i = 0; i < Math.min(dataList.size(), result.size()); i++) {
                if (i == 0) {
                    headerString = result.get(i) + "," + dataList.get(i);
                } else {
                    String temp = result.get(i) + "," + dataList.get(i);
                    inputs.add(temp);
                }
            }
            CsvUtil.writeToCsv(headerString, inputs, filePath, false);
        }
        return Result.success(jobId);
    }

    @WebLog(description = "查询预览结果")
    @RequestMapping(value = "/preview/result/{jobID}", method = RequestMethod.GET)
    public Result getPreview(@PathVariable String jobID) throws IOException {
        String filePath = "result/" + jobID;
        JSONObject result = new JSONObject();
        List<String> readData = CsvUtil.readFromCsv(filePath);
        if (readData == null) {
            result.put("result", readData);
        } else {
            List<List<String>> dataList = new ArrayList<>();
            for (String value : readData) {
                List<String> valueList = List.of(value.split(","));
                dataList.add(valueList);
            }

            JSONArray data = new JSONArray();
            for (int i = 1; i < dataList.size(); i++) {
                if (i < 11) {
                    JSONObject tableData = new JSONObject();
                    for (int j = 0; j < dataList.get(0).size(); j++) {
                        tableData.put(dataList.get(0).get(j), dataList.get(i).get(j));
                    }
                    data.add(tableData);
                }
            }
            result.put("result", data);
        }

        return Result.success(result);
    }

    @WebLog(description = "下载结果")
    @RequestMapping(value = "/download/result/{jobID}", method = RequestMethod.GET)
    public ResponseEntity<String> download(@PathVariable String jobID) {
        String filePath = "result/" + jobID;
        List<String> data = CsvUtil.readFromCsv(filePath);
        JSONObject result = new JSONObject();
        result.put("result", data);
        return new ResponseEntity<String>(result.toJSONString(), HttpStatus.OK);
    }
    @RequestMapping(value = "/delete/org/{did}", method = RequestMethod.DELETE)
    public Result deleteOrg(@PathVariable String did) {
        HashMap<String, byte[]> params = new HashMap<>();
        params.put("orgId", did.getBytes());
        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME_3, "delete_from_org_id", params);

        if (csr.isOk()) {
            return Result.success(csr.toString());
        } else {
            return Result.failure(ResultCode.CONTRACT_FAILED, csr.toString());
        }
    }
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test() {
        String result = "v1";
        return result;
    }
    @RequestMapping(value = "/did/address/{name}", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> getAddressFromDID(@PathVariable String name) {
        System.out.println("===SAVE DataCatalog===");
        Map<String, byte[]> params = new HashMap<>();
        params.put("orgDID", name.getBytes(StandardCharsets.UTF_8));
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME_3, "get_address_from_did", params);
        JSONObject res = csr.toJSON(false);
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        System.out.println(responseStatus);
        return new ResponseEntity<JSONObject>(res, responseStatus);
    }
    @RequestMapping(value = "/jobs/address/{jobID}", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> getAddress(@PathVariable String jobID) {
        JobGetPo jobGetPo = new JobGetPo();
        jobGetPo.setJobID(jobID);
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryJobDetails", jobGetPo.toContractParams());
        System.out.println("csr: " + csr);
        JobInfoPo jobInfoPo = JSONObject.parseObject(csr.toString(), JobInfoPo.class, Feature.OrderedField);
        String submitter = jobInfoPo.getJob().getSubmitter();
        Map<String, byte[]> params = new HashMap<>();
        params.put("orgDID", submitter.getBytes(StandardCharsets.UTF_8));
        ContractServiceResponse res_csr = blockchainContractService.queryContract(CONTRACT_NAME_3, "get_address_from_did", params);
        JSONObject res = res_csr.toJSON(false);
        HttpStatus responseStatus = res_csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<JSONObject>(res, responseStatus);
    }
}
