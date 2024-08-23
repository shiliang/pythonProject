package com.chainmaker.jobservice.api.service;

import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.model.job.Job;
import com.chainmaker.jobservice.api.model.job.service.Service;
import com.chainmaker.jobservice.api.model.bo.JobMissionDetail;
import com.chainmaker.jobservice.api.model.graph.Dag;
import com.chainmaker.jobservice.api.model.graph.Topology;
import com.chainmaker.jobservice.api.model.job.task.Task;
import com.chainmaker.jobservice.api.model.vo.SqlVo;

import java.util.List;

public interface JobParserService {

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
    Dag taskToDag(List<Task> tasks, Integer dataStatus);


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


    Job jobPreview(SqlVo sqlVo);


    JSONObject analyzeSql(SqlVo sqlVo);
}
