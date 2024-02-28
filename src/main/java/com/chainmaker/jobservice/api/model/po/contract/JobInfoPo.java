package com.chainmaker.jobservice.api.model.po.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.DataCatalogCache;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.bo.job.service.Service;
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

    public static JobInfoPo converterToJobInfoPo(JobInfo jobInfo) {
        JobInfoPo jobInfoPo = new JobInfoPo();
        jobInfo.update();
        jobInfoPo.setApplicationID(jobInfo.getJob().getJobID());
        jobInfoPo.setJob(JobPo.converterToJobPo(jobInfo.getJob()));

        if (jobInfo.getTasks() != null) {
            List<TaskPo> taskPoList = new ArrayList<>();
            for (Task task : jobInfo.getTasks()) {
                TaskPo taskPo = TaskPo.converterToTaskPo(task);
                taskPoList.add(taskPo);
            }
            jobInfoPo.setTasks(taskPoList);
        }

        if (jobInfo.getServices() != null) {
            List<ServicePo> servicePoList = new ArrayList<>();
            for (Service service : jobInfo.getServices()) {
                ServicePo servicePo = ServicePo.converterToServicePo(service);
                servicePoList.add(servicePo);
            }
            jobInfoPo.setServices(servicePoList);
        }
        return jobInfoPo;
    }

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

    public static void main(String[] args) {
        String joinInfo = "{\"applicationID\":\"cbcae9ca38624e4d9536855e67148aae\",\"job\":{\"common\":\"null\",\"createTime\":\"1704441669900\",\"jobID\":\"cbcae9ca38624e4d9536855e67148aae\",\"jobType\":\"FQ\",\"parties\":[\"wx-org3.chainmaker.orgDID\",\"wx-org2.chainmaker.orgDID\"],\"projectID\":\"0cdecc91-a946-41d5-9f88-77500ca20ccf\",\"status\":\"WAITING\",\"tasksDAG\":\"taskDAG\",\"updateTime\":\"1704441669900\"},\"services\":[],\"tasks\":[{\"createTime\":\"1704439857113\",\"input\":{\"data\":[{\"dataID\":\"T_ORG_1\",\"dataName\":\"T_ORG_1\",\"domainID\":\"wx-org3.chainmaker.orgDID\",\"params\":\"{\\\"table\\\":\\\"T_ORG_1\\\",\\\"field\\\":\\\"ID\\\"}\",\"role\":\"client\",\"taskSrc\":\"\"},{\"dataID\":\"T_ORG_2\",\"dataName\":\"T_ORG_2\",\"domainID\":\"wx-org2.chainmaker.orgDID\",\"params\":\"{\\\"table\\\":\\\"T_ORG_2\\\",\\\"field\\\":\\\"ID\\\"}\",\"role\":\"server\",\"taskSrc\":\"\"}]},\"jobID\":\"cbcae9ca38624e4d9536855e67148aae\",\"module\":{\"moduleName\":\"TEEPSI\",\"params\":\"{\\\"joinType\\\":\\\"INNER\\\",\\\"operator\\\":\\\"=\\\",\\\"teeHost\\\":\\\"192.168.40.21\\\",\\\"teePort\\\":\\\"30091\\\",\\\"domainID\\\":\\\"wx-org3.chainmaker.orgDID\\\"}\"},\"output\":{\"data\":[{\"dataID\":\"\",\"dataName\":\"wx-org3.chainmaker.orgDID-1\",\"domainID\":\"wx-org3.chainmaker.orgDID\",\"finalResult\":\"N\"},{\"dataID\":\"\",\"dataName\":\"wx-org2.chainmaker.orgDID-1\",\"domainID\":\"wx-org2.chainmaker.orgDID\",\"finalResult\":\"N\"}]},\"parties\":[{\"partyID\":\"wx-org3.chainmaker.orgDID\"},{\"partyID\":\"wx-org2.chainmaker.orgDID\"}],\"status\":\"WAITING\",\"taskName\":\"0\",\"updateTime\":\"1704439857113\",\"version\":\"1.0.0\"},{\"createTime\":\"1704439857113\",\"input\":{\"data\":[{\"dataID\":\"\",\"dataName\":\"wx-org3.chainmaker.orgDID-1\",\"domainID\":\"wx-org3.chainmaker.orgDID\",\"params\":\"{\\\"table\\\":\\\"T_ORG_1\\\",\\\"field\\\":\\\"VAL1\\\"}\",\"role\":\"server\",\"taskSrc\":\"0\"},{\"dataID\":\"\",\"dataName\":\"wx-org2.chainmaker.orgDID-1\",\"domainID\":\"wx-org2.chainmaker.orgDID\",\"params\":\"{\\\"table\\\":\\\"T_ORG_2\\\",\\\"field\\\":\\\"VAL1\\\"}\",\"role\":\"client\",\"taskSrc\":\"0\"}]},\"jobID\":\"cbcae9ca38624e4d9536855e67148aae\",\"module\":{\"moduleName\":\"TEE\",\"params\":\"{\\\"methodName\\\":\\\"MUL\\\",\\\"teeHost\\\":\\\"192.168.40.21\\\",\\\"teePort\\\":\\\"30091\\\",\\\"domainID\\\":\\\"wx-org3.chainmaker.orgDID\\\"}\"},\"output\":{\"data\":[{\"dataID\":\"\",\"dataName\":\"wx-org3.chainmaker.orgDID-2\",\"domainID\":\"wx-org3.chainmaker.orgDID\",\"finalResult\":\"Y\"}]},\"parties\":[{\"partyID\":\"wx-org3.chainmaker.orgDID\"},{\"partyID\":\"wx-org2.chainmaker.orgDID\"}],\"status\":\"WAITING\",\"taskName\":\"1\",\"updateTime\":\"1704439857113\",\"version\":\"1.0.0\"}]}";
        JobInfoPo jobInfoPo = JSON.parseObject(joinInfo, JobInfoPo.class);
        String cache = "{\"T_ORG_2\":{\"code\":\"60-29-34-34\",\"id\":\"60e027fd-b9b9-455a-99b8-557ba1893260\",\"itemVOList\":[{\"dataLength\":12,\"dataType\":4,\"description\":\"\",\"id\":\"2e4fe5c5-9576-4512-bf7e-0f62792bff87\",\"name\":\"id\",\"primaryKey\":0},{\"dataLength\":12,\"dataType\":4,\"description\":\"\",\"id\":\"9b61af23-9a81-4f5a-84fe-3f668af19a51\",\"name\":\"val1\",\"primaryKey\":0}],\"name\":\"T_ORG_2\",\"orgDID\":\"wx-org2.chainmaker.orgDID\",\"orgId\":\"wx-org2.chainmaker.orgId\",\"publishTime\":1702613258,\"remark\":\"org2数据目录\",\"status\":1,\"version\":1},\"T_ORG_1\":{\"code\":\"82-29-99-0\",\"id\":\"ebdcc368-2fb3-4a58-b2b5-490b6a64cf94\",\"itemVOList\":[{\"dataLength\":12,\"dataType\":4,\"description\":\"\",\"id\":\"567faf65-05f5-4d3b-9645-6d68fdcb047e\",\"name\":\"id\",\"primaryKey\":1},{\"dataLength\":12,\"dataType\":4,\"description\":\"\",\"id\":\"5e095cc4-bdb4-4bdf-91b4-1601324a45ab\",\"name\":\"val1\",\"primaryKey\":0}],\"name\":\"T_ORG_1\",\"orgDID\":\"wx-org3.chainmaker.orgDID\",\"orgId\":\"wx-org3.chainmaker.orgId\",\"publishTime\":1702613230,\"remark\":\"org1数据目录\",\"status\":1,\"version\":1}}";
        JSONObject json = JSON.parseObject(cache);
        for(String key : json.keySet()){
            DataCatalogInfo value = JSONObject.parseObject(json.getString(key), DataCatalogInfo.class);
            value.setDatasourceNo(key + "88888");
            for(DataCatalogDetailInfo item : value.getItemVOList())
            {
                DataCatalogDataSecurityInfo info = new DataCatalogDataSecurityInfo();
                info.setIsQueryable(1);
                info.setColumnName("ID");
                item.setDataSecurity(info);
            }
            DataCatalogCache.put(key, value);
        }
        jobInfoPo.toContractParams();
    }
}
