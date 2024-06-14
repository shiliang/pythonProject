package com.chainmaker.jobservice.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.builder.JobBuilder;
import com.chainmaker.jobservice.api.builder.JobBuilderWithOptimizer;
import com.chainmaker.jobservice.api.model.*;
import com.chainmaker.jobservice.api.model.bo.*;
import com.chainmaker.jobservice.api.model.bo.config.CatalogConfig;
import com.chainmaker.jobservice.api.model.bo.graph.*;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.bo.job.JobTemplate;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;
import com.chainmaker.jobservice.api.model.bo.job.task.TaskInputData;
import com.chainmaker.jobservice.api.model.po.contract.JobInfoPo;
import com.chainmaker.jobservice.api.model.vo.*;
import com.chainmaker.jobservice.api.response.HttpResponse;
import com.chainmaker.jobservice.api.response.ParserException;
import com.chainmaker.jobservice.api.service.JobParserService;
import com.chainmaker.jobservice.core.SqlParser;
import com.chainmaker.jobservice.core.calcite.utils.ParserWithOptimizerReturnValue;
import com.chainmaker.jobservice.core.parser.LogicalPlanBuilderV2;
import lombok.extern.slf4j.Slf4j;
import java.util.*;


/**
 * @author gaokang
 * @date 2022-09-20 19:29
 * @description:Job解析
 * @version: 1.0.0
 */
@Slf4j
@org.springframework.stereotype.Service
public class JobParserServiceImpl implements JobParserService {

    @Override
    public Job jobPreview(SqlVo sqlVo) {
        JobMissionDetail jobMissionDetail = parserSql(sqlVo);
        Job jobInfo = jobMissionDetail.getJob();
        List<AssetDetail> assetDetailList = jobMissionDetail.getAssetDetailList();
        assetDetailList.forEach(assetDetail -> {
            String[] split = assetDetail.getAssetName().split("_");
            assetDetail.setOrgId(split[split.length - 1]);
        });
        jobInfo.setAssetDetailList(assetDetailList);


        if (jobInfo.getTaskList() != null) {
            Dag dag = taskToDag(jobInfo.getTaskList(), jobInfo.getStatus());
            jobInfo.setDag(dag);
        }
        if (jobInfo.getServiceList() != null) {
            Topology topology = serviceToTopology(jobInfo.getServiceList(), 1);
            jobInfo.setTopology(topology);
        }
//        if (jobInfoVo.getServices() != null) {
//            for (Service service : jobInfoVo.getServices()) {
//                for (ExposeEndpointVo exposeEndpointVo : service.getExposeEndpointList()) {
//                    exposeEndpointVo.getForm().removeIf(exposeFormVo -> Objects.equals(exposeFormVo.getKey(), "serviceCa") || Objects.equals(exposeFormVo.getKey(), "serviceCert") || Objects.equals(exposeFormVo.getKey(), "serviceKey"));
//                }
//            }
//        }



//        put(jobID, jobGraph);
        return jobInfo;
    }



    public JSONObject analyzeSql(String sql) {
        LogicalPlanBuilderV2 logicalPlanBuilder = new LogicalPlanBuilderV2(sql);
        Map<String, String> tableNames = logicalPlanBuilder.getTableNameMap();
        List<String> modelNames = logicalPlanBuilder.getModelNameList();
        JSONObject json = new JSONObject();
        json.put("tables", tableNames);
        json.put("models", modelNames);
        return json;
    }

    @Override
    public JobMissionDetail parserSql(SqlVo sqlVo) {
        String sqltext = sqlVo.getSqltext().replace("\"", "");
        if (sqltext.contains("/*+ FILTER(TEE) */".replace("\\s", ""))){
            sqlVo.setModelType(2);
        }
        sqltext = sqltext.replace("/*+ FILTER(TEE) */".replace("\\s", ""), "");
        SqlParser sqlParser = new SqlParser(sqltext, sqlVo.getIsStream(), sqlVo.getModelType(), sqlVo.getAssetInfoList(), sqlVo.getModelParams());
        if (sqlVo.getIsStream() == 1) {
            JobBuilder jobBuilder = new JobBuilder(sqlVo.getModelType(), sqlVo.getIsStream(), sqlParser.parser(), sqlVo.getOrgInfo(), sqlParser.getSql());
            jobBuilder.build();
            JobMissionDetail jobMissionDetail = new JobMissionDetail();
            jobMissionDetail.setJob(jobBuilder.getJob());
            jobMissionDetail.setMissionDetailVOList(sqlParser.getMissionDetailVOs());
            jobMissionDetail.setModelParamsVoList(sqlParser.getModelParamsVos());
            jobMissionDetail.setAssetDetailList(sqlParser.getAssetDetailList());
            return jobMissionDetail;
        } else {
            JobBuilderWithOptimizer jobBuilder = new JobBuilderWithOptimizer(sqlVo.getModelType(), sqlVo.getIsStream(), sqlParser.parserWithOptimizer(), sqlParser.getColumnInfoMap(), sqlVo.getOrgInfo(), sqlParser.getSql());
            ParserWithOptimizerReturnValue optimizer = sqlParser.parserWithOptimizer();
//            String orgId = getOrgId(); //从mira backendservice 获取orgId
            String orgId = "org1";
            JobBuilderWithOptimizer jobBuilder = new JobBuilderWithOptimizer(sqlVo.getModelType(),  sqlVo.getIsStream(),
                    optimizer, sqlParser.getColumnInfoMap(), orgId,
                    sqlParser.getSql()
            );
            jobBuilder.build();
            JobMissionDetail jobMissionDetail = new JobMissionDetail();
            jobMissionDetail.setJob(jobBuilder.getJob());
            jobMissionDetail.setMissionDetailVOList(sqlParser.getMissionDetailVOs());
            jobMissionDetail.setModelParamsVoList(sqlParser.getModelParamsVos());
            jobMissionDetail.setAssetDetailList(sqlParser.getAssetDetailList());
            return jobMissionDetail;
        }
    }


    @Override
    public Dag taskToDag(List<Task> tasks, Integer dataStatus) {
        Dag dag = new Dag();
        List<DagNode> nodes = new ArrayList<>();
        List<DagEdge> edges = new ArrayList<>();
        int capacity = 100;
        int dataCount = 0;
        int id = dataCount;
        for (Task task : tasks) {
            DagNode dagNode = new DagNode();
            dagNode.setId(String.valueOf(id));
            id += 1;
            String label = task.getModule().getModuleName() + "(" + task.getStatus() + ")";
            dagNode.setLabel(label);
            dagNode.setShape("image");
            dagNode.setImage(task.getModule().getModuleName().toLowerCase());
            dagNode.setFindLabel(task.getTaskName());
            nodes.add(dagNode);

            String taskName = task.getTaskName();
            for (InputDetail taskInputData : task.getInput().getInputDataDetailList()) {
                if (!Objects.equals(taskInputData.getTaskSrc(), "")) {
                    DagEdge dagEdge = new DagEdge();
                    dagEdge.setFrom(taskInputData.getTaskSrc());
                    dagEdge.setTo(taskName);
                    dagEdge.setLabel(taskInputData.getDataName());
                    edges.add(dagEdge);
                } else {
                    DagNode dataNode = new DagNode();
                    dataNode.setId(String.valueOf(capacity));

                    String dataLabel = taskInputData.getDataName() + "(" + dataStatus + ")";
                    dataNode.setLabel(dataLabel);
                    dataNode.setShape("image");
                    dataNode.setImage("dataSource");
                    dataNode.setFindLabel(task.getTaskName());
                    nodes.add(dataNode);

                    DagEdge dataEdge = new DagEdge();
                    dataEdge.setFrom(String.valueOf(capacity));
                    dataEdge.setTo(taskName);
                    dataEdge.setLabel(taskInputData.getDataName());
                    edges.add(dataEdge);
                    capacity += 1;

                }
            }
        }
        dag.setNodeList(nodes);
        dag.setEdgeList(edges);
        return dag;
    }

    @Override
    public List<Task> dagToTask(Dag dag, List<Task> tasks) {
        return null;
    }

    @Override
    public Topology serviceToTopology(List<Service> services, Integer templateId) {
        if (templateId == 1) {
            Topology topology = new Topology();
            List<TopologyNode> nodes = new ArrayList<>();
            List<TopologyEdge> edges = new ArrayList<>();
            for (Service service : services) {
                TopologyNode node = new TopologyNode();
                node.setId(service.getServiceId());
                node.setStatus("WAITING");
//                node.setStatus(Constant.SERVICE_STATUS);
                node.setServiceType(service.getServiceClass());
                node.setNodeError(true);
                node.setAverageTime("-");
                node.setTotalQueries("-");
                node.setSuccessRate("-");
                node.setName(service.getServiceName());
                node.setDataType("ROOT");
                if (Integer.parseInt(service.getServiceId()) > 1) {
                    node.setDataType("SUBROOT");
                }
                nodes.add(node);
                for (ReferExposeEndpoint referExposeEndpoint : service.getReferExposeEndpointList()) {
                    if (!Objects.equals(referExposeEndpoint.getName(), "")) {
                        TopologyEdge edge = new TopologyEdge();
                        edge.setSource(service.getServiceId());
                        edge.setTarget(referExposeEndpoint.getReferServiceId());
                        TopologyEdgeData topologyEdgeData = new TopologyEdgeData();
                        topologyEdgeData.setType("data");
                        edge.setData(topologyEdgeData);
                        edges.add(edge);
                    }
                }
            }
            topology.setNodeList(nodes);
            topology.setEdgeList(edges);
            return topology;
        } else {
            throw new ParserException("暂不支持的模板");
        }
    }

    @Override
    public List<Service> topologyToService(Topology topology, List<Service> services) {
        return null;
    }


}



