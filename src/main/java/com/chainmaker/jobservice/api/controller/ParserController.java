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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RequestMapping("/v1")
@RestController
public class ParserController {
    private static final String WASM_PATH = "/home/workspace/sdk/wasm/";
//    private static final String WASM_PATH = "D://";
    private static final String CONTRACT_NAME = "mission_manager";
    private static final String CONTRACT_FILE_PATH = "job_manager.wasm";
    private static final String CONTRACT_NAME_2 = "catalog_contract2";
    private static final String CONTRACT_FILE_PATH_2 = "catalog_contract.wasm";
    private static final String CONTRACT_NAME_3 = "DID";
    private static final String CONTRACT_FILE_PATH_3 = "mock_did.wasm";

    @Autowired
    BlockchainContractService blockchainContractService;
    @Autowired
    JobParserService jobParserService;

    @WebLog(description = "预生成DAG")
    @RequestMapping(value = "/preview/dag", method = RequestMethod.POST)
    public Result jobPreview(@RequestBody String req) {
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
        log.info(sqlVo.getSqltext().toString());
        if (sqlVo.getSqltext().contains("?") && sqlVo.getIsStream() != 1) {
            return Result.failure(ResultCode.NOT_STREAM_WITH_STREAM_PARAM_EXCEPTION);
        }
        if (!sqlVo.getSqltext().contains("?") && sqlVo.getIsStream() == 1) {
            return Result.failure(ResultCode.NOT_STREAM_WITH_STREAM_PARAM_EXCEPTION);
        }
        JobGraphVo jobGraphVo = jobParserService.jobPreview(sqlVo);
        MissionInfoVo missionInfoVo = jobParserService.jobCommit(jobGraphVo);
        JSONObject json = new JSONObject();
        json.put("jobInfo", jobGraphVo);
        json.put("missionInfo", missionInfoVo);
        return Result.success(json);
    }









}
