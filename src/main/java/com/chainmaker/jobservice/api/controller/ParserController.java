package com.chainmaker.jobservice.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.aspect.WebLog;
import com.chainmaker.jobservice.api.model.job.Job;
import com.chainmaker.jobservice.api.model.vo.*;
import com.chainmaker.jobservice.api.response.Result;
import com.chainmaker.jobservice.api.response.ResultCode;
import com.chainmaker.jobservice.api.service.JobParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/v1")
@RestController
public class ParserController {

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
    public Result jobCommit(@RequestBody String req) throws Exception {
        SqlVo sqlVo = JSONObject.parseObject(req, SqlVo.class, Feature.OrderedField);
        log.info(sqlVo.getSqltext().toString());
        if (sqlVo.getSqltext().contains("?") && sqlVo.getIsStream() != 1) {
            return Result.failure(ResultCode.NOT_STREAM_WITH_STREAM_PARAM_EXCEPTION);
        }
        if (!sqlVo.getSqltext().contains("?") && sqlVo.getIsStream() == 1) {
            return Result.failure(ResultCode.NOT_STREAM_WITH_STREAM_PARAM_EXCEPTION);
        }
        Job job = jobParserService.jobPreview(sqlVo);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("job", job);
        return Result.success(jsonObject);
    }









}
