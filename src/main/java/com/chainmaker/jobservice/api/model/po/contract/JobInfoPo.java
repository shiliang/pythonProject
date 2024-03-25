package com.chainmaker.jobservice.api.model.po.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.DataCatalogCache;
import com.chainmaker.jobservice.api.model.Service;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;
import com.chainmaker.jobservice.api.model.po.contract.job.JobPo;
import com.chainmaker.jobservice.api.model.po.contract.job.ServicePo;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskInputDataPo;
import com.chainmaker.jobservice.api.model.po.contract.job.TaskPo;
import com.chainmaker.jobservice.api.response.ParserException;
import com.chainmaker.jobservice.core.analyzer.catalog.DataCatalogDataSecurityInfo;
import com.chainmaker.jobservice.core.analyzer.catalog.DataCatalogDetailInfo;
import com.chainmaker.jobservice.core.analyzer.catalog.DataCatalogInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gaokang
 * @date 2022-09-20 10:47
 * @description:上链存储job结构
 * @version: 1.0.0
 */

@Data
@Slf4j
public class JobInfoPo {
    private String applicationID;

    private JobPo job;

    private List<TaskPo> tasks;

    private List<ServicePo> services;
    public HashMap<String, byte[]> toContractParams() {
        HashMap<String, byte[]> contractParams = new HashMap<>();
        contractParams.put("applicationID", this.getApplicationID().getBytes());
        contractParams.put("job", JSON.toJSONString(this.getJob()).getBytes());

        //在Task的input params中增加datasourceId信息，无法添加数据源的连接详情，因为datasourceId是上链的，但是数据源的连接详情保留在各方本地。
        List<TaskPo> tasks = this.getTasks();
        for(TaskPo po: tasks){
            List<TaskInputDataPo> list = po.getInput().getData();
            for(TaskInputDataPo inputDataPo: list) {
                if (inputDataPo.getParams() != null) {
                    JSONObject params = JSONObject.parseObject(inputDataPo.getParams());
                    String orgDID = inputDataPo.getDomainID();
                    String dataCatalogName = params.getString("table");
                    String fieldName = params.getString("field");
                    DataCatalogInfo info = DataCatalogCache.getByDIDAndName(orgDID, dataCatalogName);
                    assert info != null;
                    if(fieldIsQuerable(info, fieldName)) {
                        if (info.getDatasourceNo() != null) {
                            params.put("datasourceNo", info.getDatasourceNo());
                        }
                    }else{
                        throw new ParserException("字段[" + fieldName + "]不支持查询");
                    }
                    inputDataPo.setParams(JSON.toJSONString(params));
                }
            }
        }
        log.info("contractParams: {}", JSON.toJSONString(contractParams));
        contractParams.put("tasks", JSON.toJSONString(this.getTasks()).getBytes());
        contractParams.put("services", JSON.toJSONString(this.getServices()).getBytes());
        return contractParams;
    }

    public boolean fieldIsQuerable(DataCatalogInfo dataCatalogInfo, String fieldName){
        Set<String> noQueryFields = dataCatalogInfo.getItemVOList().stream()
                .map(DataCatalogDetailInfo::getDataSecurity)
                .filter(Objects::nonNull)
                .filter(x -> x.getIsQueryable() == 0)
                .map(DataCatalogDataSecurityInfo::getColumnName)
                .collect(Collectors.toSet());
        return !noQueryFields.contains(fieldName);
    }

    @Override
    public String toString() {
        return "JobInfoPo{" +
                "applicationID='" + applicationID + '\'' +
                ", job=" + job +
                ", tasks=" + tasks +
                ", services=" + services +
                '}';
    }
}
