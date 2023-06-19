package com.chainmaker.jobservice.api.service;

import com.chainmaker.jobservice.api.model.bo.*;
import com.chainmaker.jobservice.api.model.bo.config.CatalogConfig;
import com.chainmaker.jobservice.api.model.bo.graph.Dag;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.bo.graph.Topology;
import com.chainmaker.jobservice.api.model.bo.job.service.Service;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;
import com.chainmaker.jobservice.api.model.po.contract.JobInfoPo;
import com.chainmaker.jobservice.api.model.po.contract.ServiceUpdatePo;
import com.chainmaker.jobservice.api.model.po.data.ServiceValueParam;
import com.chainmaker.jobservice.api.model.po.data.UserInfo;
import com.chainmaker.jobservice.api.model.vo.*;

import java.util.List;

public interface JobParserService {
    void setCatalogConfig(CatalogConfig catalogConfig);
    /***
     * @description 根据类型解析SQL
     * @param sqlVo
     * @return com.chainmaker.jobservice.api.model.bo.job.JobInfo
     * @author gaokang
     * @date 2022/9/22 21:27
     */
    JobMissionDetail parserSql(SqlVo sqlVo);

    /***
     * @description 由任务列表生成有向无环图
     * @param
     * @return com.chainmaker.jobservice.api.model.bo.graph.Dag
     * @author gaokang
     * @date 2022/9/20 19:28
     */
    Dag taskToDag(List<Task> tasks, String dataStatus);


    /***
     * @description 由有向无环图更新任务
     * @param dag
     * @param tasks
     * @return java.util.List<com.chainmaker.jobservice.api.model.bo.job.task.Task>
     * @author gaokang
     * @date 2022/9/22 20:22
     */
    List<Task> dagToTask(Dag dag, List<Task> tasks);

    /***
     * @description 根据默认模板生成拓扑图
     * @param
     * @return com.chainmaker.jobservice.api.model.bo.graph.Topology
     * @author gaokang
     * @date 2022/9/19 18:04
     */
    Topology serviceToTopology(List<Service> services, Integer templateId);

    /***
     * @description 由服务拓扑图更新服务
     * @param topology
     * @param services
     * @return java.util.List<com.chainmaker.jobservice.api.model.bo.job.service.Service>
     * @author gaokang
     * @date 2022/9/22 20:24
     */
    List<Service> topologyToService(Topology topology, List<Service> services);

    List<ServiceRunner> converterToServiceRunner(List<Service> services, List<ServiceValueParam> serviceValueParams, String orgDID);

    void put(String key, JobGraph value);
    JobGraph getJobGraph(String key);
    void put(String key, CatalogCache value);
    CatalogCache getCatalogCache(String key);
    void delete(String key);

    JobGraphVo jobPreview(SqlVo sqlVo);
    MissionInfoVo jobCommit(JobGraphVo jobGraphVo);
    JobInfo jobCreate(MissionInfo missionInfo);
    JobGraphVo getJobApproval(JobInfoPo jobInfoPo);
    JobGraphVo getJobInfo(JobInfoPo jobInfoPo);
    JobRunner getJobRunner(JobInfoPo jobInfoPo);
    ServiceUpdatePo updateService(ServiceUpdateVo serviceUpdateVo);

    void save(List<ServiceValueParam> serviceValues);
    List<ServiceValueParam> get(String orgDID, String jobID);
    UserInfo getUserInfo(String userName);

}
