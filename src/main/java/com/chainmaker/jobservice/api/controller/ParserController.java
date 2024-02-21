package com.chainmaker.jobservice.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.aspect.WebLog;
import com.chainmaker.jobservice.api.model.bo.*;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.bo.job.task.TaskOutputData;
import com.chainmaker.jobservice.api.model.po.contract.JobGetPo;
import com.chainmaker.jobservice.api.model.po.contract.JobInfoPo;
import com.chainmaker.jobservice.api.model.po.contract.JobUpdateStsPo;
import com.chainmaker.jobservice.api.model.po.contract.ServiceUpdatePo;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskPo;
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
import org.springframework.web.client.RestTemplate;

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
    public String getDIDFromOrgId(String orgId) {
        Map<String, byte[]> params = new HashMap<>();
        params.put("certOrgID", orgId.getBytes(StandardCharsets.UTF_8));
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME_3, "get_did_from_cert_org_id", params);
        JSONObject res = csr.toJSON(false);
        return res.getString("result");
    }
    @WebLog(description = "预生成DAG")
    @RequestMapping(value = "/preview/dag", method = RequestMethod.POST)
    public Result jobPreview(@RequestBody String req) {
        jobParserService.setOrgDID(getDIDFromOrgId(jobParserService.getOrgId()));
        SqlVo sqlVo = JSONObject.parseObject(req, SqlVo.class, Feature.OrderedField);
        if (sqlVo.getSqltext().contains("?")) {
            sqlVo.setIsStream(1);
        }
//        JobGraphVo jobGraphVo= jobParserService.jobPreview(sqlVo);
        JSONObject json= jobParserService.analyzeSql(sqlVo.getSqltext());
        return Result.success(json);
    }

    @WebLog(description = "提交DAG")
    @RequestMapping(value = "/commit/dag", method = RequestMethod.POST)
    public Result jobCommit(@RequestBody String req) {
        SqlVo sqlVo = JSONObject.parseObject(req, SqlVo.class, Feature.OrderedField);
        if (sqlVo.getSqltext().contains("?")) {
            sqlVo.setIsStream(1);
        }
        JobGraphVo jobGraphVo = jobParserService.jobPreview(sqlVo);
        MissionInfoVo missionInfoVo = jobParserService.jobCommit(jobGraphVo);
        JSONObject json = new JSONObject();
        json.put("jobInfo", jobGraphVo);
        json.put("missionInfo", missionInfoVo);
        return Result.success(json);
    }

    @WebLog(description = "创建JOB")
    @RequestMapping(value = "/jobs", method = RequestMethod.POST)
    public ResponseEntity<String> jobCreate(@RequestBody String req) {
        MissionInfo missionInfo = JSONObject.parseObject(req, MissionInfo.class, Feature.OrderedField);
        JobInfo jobInfo = jobParserService.jobCreate(missionInfo);
        String jobID = jobInfo.getJob().getJobID();
        JobInfoPo jobInfoPo = JobInfoPo.converterToJobInfoPo(jobInfo);
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
        jobParserService.setOrgDID(getDIDFromOrgId(jobParserService.getOrgId()));
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
    @WebLog
    @RequestMapping(value = "/jobs/{jobID}", method = RequestMethod.GET)
    public ResponseEntity<JobRunner> getJobRunner(@PathVariable String jobID) {
        jobParserService.setOrgDID(getDIDFromOrgId(jobParserService.getOrgId()));
        JobGetPo jobGetPo = new JobGetPo();
        jobGetPo.setJobID(jobID);
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryJobDetails", jobGetPo.toContractParams());
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
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
                if (result.get(0).equals(dataList.get(0))) {
                    if (i != 0) {
                        result.add(dataList.get(i));
                    }
                } else {
                    if (i == 0) {
                        headerString = result.get(i) + "," + dataList.get(i);
                    } else {
                        String temp = result.get(i) + "," + dataList.get(i);
                        inputs.add(temp);
                    }
                }
            }
            if (result.get(0).equals(dataList.get(0))) {
                CsvUtil.writeToCsv(headerString, result, filePath, false);
            } else {
                CsvUtil.writeToCsv(headerString, inputs, filePath, false);
            }
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
            List<String> keys = List.of(readData.get(0).split(","));
            if (keys.size() == 1) {
                for (String value : readData) {
                    List<String> valueList = List.of(value);
                    dataList.add(valueList);
                }
            } else if (keys.size() == 2) {
                for (String value : readData) {
                    String kept = value.substring( 0, value.indexOf(","));
                    String remainder = value.substring(value.indexOf(",")+1, value.length());
                    List<String> valueList = new ArrayList<>();
                    valueList.add(kept);
                    valueList.add(remainder);
                    dataList.add(valueList);
                }

            } else {
                for (String value : readData) {
                    List<String> valueList = List.of(value.split(","));
                    dataList.add(valueList);
                }
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
        return new ResponseEntity<JSONObject>(res, responseStatus);
    }
    @WebLog
    @RequestMapping(value = "/jobs/address/{jobID}", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> getAddress(@PathVariable String jobID) {
        JobGetPo jobGetPo = new JobGetPo();
        jobGetPo.setJobID(jobID);
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryJobDetails", jobGetPo.toContractParams());
        JobInfoPo jobInfoPo = JSONObject.parseObject(csr.toString(), JobInfoPo.class, Feature.OrderedField);
        String submitter = jobInfoPo.getJob().getSubmitter();
        Map<String, byte[]> params = new HashMap<>();
        params.put("orgDID", submitter.getBytes(StandardCharsets.UTF_8));
        ContractServiceResponse res_csr = blockchainContractService.queryContract(CONTRACT_NAME_3, "get_address_from_did", params);
        JSONObject res = res_csr.toJSON(false);
        HttpStatus responseStatus = res_csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<JSONObject>(res, responseStatus);
    }
    @WebLog
    @RequestMapping(value = "/jobs/result/list/{jobID}", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> getResultList(@PathVariable String jobID) {
        JSONObject result = new JSONObject();
        JSONArray dataIdList = new JSONArray();
        JobGetPo jobGetPo = new JobGetPo();
        jobGetPo.setJobID(jobID);
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryJobDetails", jobGetPo.toContractParams());
        JobInfoPo jobInfoPo = JSONObject.parseObject(csr.toString(), JobInfoPo.class, Feature.OrderedField);
        for (TaskPo taskPo : jobInfoPo.getTasks()) {
            for (TaskOutputData taskOutputData : taskPo.getOutput().getData()) {
                if (!taskOutputData.getFinalResult().equals("N")) {
                    String orgDID = taskOutputData.getDomainID();
                    String dataID = taskOutputData.getDataID();

                    String encUrl = "";
                    String decUrl = "";

                    JSONObject temp = new JSONObject();
                    Map<String, byte[]> params = new HashMap<>();
                    params.put("orgDID", orgDID.getBytes(StandardCharsets.UTF_8));
                    ContractServiceResponse res_csr = blockchainContractService.queryContract(CONTRACT_NAME_3, "get_address_from_did", params);
                    JSONObject res = res_csr.toJSON(false);
                    String url1 = "http://" + res.getString("result") + "/kms/sm2/file/url/result/" + jobID + "/" + dataID;
                    System.out.println(url1);
                    RestTemplate restTemplate = new RestTemplate();
                    JSONObject urlResult = JSONObject.parseObject(restTemplate.getForObject(url1, String.class), Feature.OrderedField);
                    System.out.println(urlResult);
                    if (taskOutputData.getFinalResult().equals("YE")) {
                        encUrl = urlResult.getString("url");
                        JSONObject req = new JSONObject();
                        req.put("orgDID", orgDID);
                        req.put("jobId", jobID);
                        req.put("dataID", dataID);
                        req.put("url", encUrl);
                        String url2 = "http://" + res.getString("result") + "/kms/sm2/decrypt";
                        System.out.println(url2);
                        JSONObject urlDecResult = restTemplate.postForObject(url2, req, JSONObject.class);
                        System.out.println(urlDecResult);
                        decUrl = urlDecResult.getString("url");
                    } else {
                        decUrl = urlResult.getString("url");
                    }
                    temp.put("dataID", dataID);
                    temp.put("orgDID", orgDID);
                    temp.put("encUrl", encUrl);
                    temp.put("decUrl", decUrl);

                    dataIdList.add(temp);
                }
            }
        }
        result.put("jobID", jobID);
        result.put("data", dataIdList);
        return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
    }
    @WebLog
    @RequestMapping(value = "/jobs/get/result/enc/{jobID}/{dataID}/{orgDID}", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> getEncResult(@PathVariable String jobID, @PathVariable String dataID, @PathVariable String orgDID) {
        JSONObject result = new JSONObject();
        Map<String, byte[]> params = new HashMap<>();
        params.put("orgDID", orgDID.getBytes(StandardCharsets.UTF_8));
        ContractServiceResponse res_csr = blockchainContractService.queryContract(CONTRACT_NAME_3, "get_address_from_did", params);
        JSONObject res = res_csr.toJSON(false);
        String url = "http://" + res.getString("result") + "/kms/sm2/file/url/result/" + jobID + "/" + dataID;
        System.out.println(url);
        RestTemplate restTemplate = new RestTemplate();
        JSONObject urlResult = JSONObject.parseObject(restTemplate.getForObject(url, String.class), Feature.OrderedField);
        System.out.println(urlResult);
        result.put("result", urlResult.getString("url"));
        return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
    }
//    todo
    @WebLog
    @RequestMapping(value = "/jobs/get/result/dec/{jobID}/{dataID}/{orgDID}", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> getDecResult(@PathVariable String jobID, @PathVariable String dataID, @PathVariable String orgDID) {
        JSONObject result = new JSONObject();
        Map<String, byte[]> params = new HashMap<>();
        params.put("orgDID", orgDID.getBytes(StandardCharsets.UTF_8));
        ContractServiceResponse res_csr = blockchainContractService.queryContract(CONTRACT_NAME_3, "get_address_from_did", params);
        JSONObject res = res_csr.toJSON(false);
        String url = "http://" + res.getString("result") + "/kms/sm2/file/url/result/" + jobID + "/" + dataID;
        System.out.println(url);
        RestTemplate restTemplate = new RestTemplate();
        JSONObject urlResult = JSONObject.parseObject(restTemplate.getForObject(url, String.class), Feature.OrderedField);
        String encUrl = urlResult.getString("url");

        JSONObject req = new JSONObject();
        req.put("orgDID", orgDID);
        req.put("jobId", jobID);
        req.put("dataID", dataID);
        req.put("url", urlResult.getString("url"));
        JSONObject urlDecResult = restTemplate.postForObject(url, req, JSONObject.class);
        String decUrl = urlDecResult.getString("url");
        result.put("result", decUrl);

        return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
    }
    @WebLog
    @RequestMapping(value = "/jobs/result/download/{jobID}", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> getResult(@PathVariable String jobID) {
        HashMap<String, List<String>> resultMap = new HashMap<>();
        HashMap<String, String> addressMap = new HashMap<>();
        JobGetPo jobGetPo = new JobGetPo();
        jobGetPo.setJobID(jobID);
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME, "QueryJobDetails", jobGetPo.toContractParams());
        JobInfoPo jobInfoPo = JSONObject.parseObject(csr.toString(), JobInfoPo.class, Feature.OrderedField);
        for (TaskPo taskPo : jobInfoPo.getTasks()) {
            for (TaskOutputData taskOutputData : taskPo.getOutput().getData()) {
                if (taskOutputData.getFinalResult().equals("Y")) {
                    if (resultMap.containsKey(taskOutputData.getDomainID())) {
                        resultMap.get(taskOutputData.getDomainID()).add(taskOutputData.getDataID());
                    } else {
                        List<String> temp = new ArrayList<>();
                        temp.add(taskOutputData.getDataID());
                        resultMap.put(taskOutputData.getDomainID(), temp);
                    }
                }
            }
        }
        for (String partyID : resultMap.keySet()) {
            Map<String, byte[]> params = new HashMap<>();
            params.put("orgDID", partyID.getBytes(StandardCharsets.UTF_8));
            ContractServiceResponse res_csr = blockchainContractService.queryContract(CONTRACT_NAME_3, "get_address_from_did", params);
            JSONObject res = res_csr.toJSON(false);
            addressMap.put(partyID, res.getString("result"));
        }
        JSONObject resultJson = new JSONObject();
        for (String party : resultMap.keySet()) {
            String address = addressMap.get(party);
            for (String dataId : resultMap.get(party)) {
                String url = address + "/result/" + jobID + "/" + dataId;
                resultJson.put(party, url);
            }
        }
        return new ResponseEntity<JSONObject>(resultJson, HttpStatus.OK);
    }

    @RequestMapping(value = "/did/pk/{name}", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> getPKFromDID(@PathVariable String name) {
        System.out.println("===SAVE DataCatalog===");
        Map<String, byte[]> params = new HashMap<>();
        params.put("orgDID", name.getBytes(StandardCharsets.UTF_8));
        ContractServiceResponse csr = blockchainContractService.queryContract(CONTRACT_NAME_3, "get_org_pk_from_did", params);
        JSONObject res = csr.toJSON(false);
        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        System.out.println(responseStatus);
        return new ResponseEntity<JSONObject>(res, responseStatus);
    }

//    @RequestMapping(value = "/jobs/createJob", method = RequestMethod.POST)
//    public ResponseEntity<String> createJob(@RequestBody String req) {
//        Map<String, byte[]> map = JSON.parseObject(req, Map.class);
//        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME, "CreateJob", map);
//        String response = csr.toString();
//        JSONObject result = new JSONObject();
//        result.put("result", response);
//        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
//        return new ResponseEntity<String>(result.toJSONString(), responseStatus);
//    }

    //QueryNotFinishedMissionsBySts
//    @RequestMapping(value = "/missions/queryNotFinishedMissionsBySts", method = RequestMethod.GET)
//    public ResponseEntity<String> queryNotFinishedMissionsBySts(@RequestParam String partyID, @RequestParam String sts) {
//        Map<String, byte[]> map = new HashMap<>();
//        map.put("partyID", partyID.getBytes());
//        map.put("sts", sts.getBytes());
//        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME, "QueryNotFinishedMissionsBySts", map);
//        String response = csr.toString();
//        JSONObject result = new JSONObject();
//        result.put("result", response);
//        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
//        return new ResponseEntity<String>(result.toJSONString(), responseStatus);
//    }
//
//    @RequestMapping(value = "/jobs/queryJobsByMissionIdAndSts", method = RequestMethod.GET)
//    public ResponseEntity<String> queryJobsByMissionIdAndSts (@RequestParam String missionID, @RequestParam String sts) {
//        Map<String, byte[]> map = new HashMap<>();
//        map.put("sts", sts.getBytes());
//        map.put("missionID", missionID.getBytes());
//        ContractServiceResponse csr = blockchainContractService.invokeContract(CONTRACT_NAME, "QueryJobsByMissionIdAndSts ", map);
//        String response = csr.toString();
//        JSONObject result = new JSONObject();
//        result.put("result", response);
//        HttpStatus responseStatus = csr.isOk() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
//        return new ResponseEntity<String>(result.toJSONString(), responseStatus);
//    }
}
