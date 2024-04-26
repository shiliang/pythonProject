package com.chainmaker.jobservice.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.builder.JobBuilder;
import com.chainmaker.jobservice.api.builder.JobBuilderWithOptimizer;
import com.chainmaker.jobservice.api.common.errors.BizException;
import com.chainmaker.jobservice.api.common.errors.ErrorEnum;
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
import com.chainmaker.jobservice.core.parser.LogicalPlanBuilderV2;
import com.chainmaker.jobservice.service.grpc.IdaGrpc;
import com.chainmaker.jobservice.util.ProtobufBeanUtil;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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
    private CatalogConfig catalogConfig;
    private HashMap<String, JobGraph> jobGraphHashMap = new HashMap<>();
    private HashMap<String, CatalogCache> catalogCacheHashMap = new HashMap<>();

    private PlatformInfo platformInfo;

    @GrpcClient("idaGrpcClient")
    private IdaGrpc.IdaBlockingStub blockingStub;

    @org.springframework.beans.factory.annotation.Value("${grpc.client.assetGrpcClient.address}")
    private String assetServiceAddr;
    @Value("${grpc.client.keyGrpcClient.address}")
    private String keyServiceAddr;





    public void setCatalogConfig(CatalogConfig catalogConfig) {
        this.catalogConfig = catalogConfig;
    }

    @Override
    public String getOrgId() {
        return String.valueOf(getPlatformInfo().getRegisterId());
    }

    @Override
    public String getOrgName() {
        return getPlatformInfo().getAccountName();
    }

    public OrgInfo getOrgInfo() {
        return  new OrgInfo(getOrgId(), getOrgName());
    }

    public void setPlatformInfo(PlatformInfo platformInfo) {
        this.platformInfo = platformInfo;
    }
    @Override
    public PlatformInfo getPlatformInfo() {
        if (this.platformInfo != null) {
            return this.platformInfo;
        }
        this.platformInfo = getPlatformInfoFromBackend();
        return this.platformInfo;
    }

    @Override
    public void put(String key, JobGraph value) {
        jobGraphHashMap.put(key, value);
    }

    @Override
    public JobGraph getJobGraph(String key) {
        if (jobGraphHashMap.containsKey(key)) {
            return jobGraphHashMap.get(key);
        } else {
            throw new ParserException("jobID查询失败");
        }
    }

    @Override
    public void put(String key, CatalogCache value) {
        catalogCacheHashMap.put(key, value);
    }

    @Override
    public CatalogCache getCatalogCache(String key) {
        if (catalogCacheHashMap.containsKey(key)) {
            return catalogCacheHashMap.get(key);
        } else {
            throw new ParserException("jobID查询失败");
        }
    }


    @Override
    public void delete(String key) {
        jobGraphHashMap.remove(key);
        catalogCacheHashMap.remove(key);
    }

    @Override
    public JobGraphVo jobPreview(SqlVo sqlVo) {
        JobMissionDetail jobMissionDetail = parserSql(sqlVo);
        JobTemplate jobTemplate = jobMissionDetail.getJobTemplate();
        List<AssetDetail> assetDetailList = jobMissionDetail.getAssetDetailList();
        JobInfoVo jobInfoVo = JobInfoVo.converterToJobInfoVo(jobTemplate);
        JobInfo jobInfo = JobInfo.jobTemplateToJobInfo(jobTemplate);
        assetDetailList.forEach(assetDetail -> {
            String[] split = assetDetail.getAssetName().split("_");
            assetDetail.setOrgId(split[split.length - 1]);
        });
        jobInfo.setAssetDetailList(assetDetailList);
        jobInfoVo.setAssetDetailList(assetDetailList);


        JobGraphVo jobGraphVo = new JobGraphVo();
        JobGraph jobGraph = new JobGraph();
        if (jobInfo.getTasks() != null) {
            Dag dag = taskToDag(jobInfo.getTasks(), jobTemplate.getJob().getStatus());
            jobGraphVo.setDag(dag);
            jobGraph.setDag(dag);
        }
        if (jobInfo.getServices() != null) {
            Topology topology = serviceToTopology(jobInfo.getServices(), 1);
            jobGraphVo.setTopology(topology);
            jobGraph.setTopology(topology);
        }
//        if (jobInfoVo.getServices() != null) {
//            for (Service service : jobInfoVo.getServices()) {
//                for (ExposeEndpointVo exposeEndpointVo : service.getExposeEndpointList()) {
//                    exposeEndpointVo.getForm().removeIf(exposeFormVo -> Objects.equals(exposeFormVo.getKey(), "serviceCa") || Objects.equals(exposeFormVo.getKey(), "serviceCert") || Objects.equals(exposeFormVo.getKey(), "serviceKey"));
//                }
//            }
//        }
        jobGraphVo.setJobInfo(jobInfoVo);
        jobGraph.setJobInfo(jobInfo);


        String jobID = jobInfo.getJob().getJobID();
        put(jobID, jobGraph);
        CatalogCache catalogCache = new CatalogCache();
        System.out.println(jobMissionDetail.getMissionDetailVOList());
        catalogCache.setMissionDetailVOList(jobMissionDetail.getMissionDetailVOList());
        catalogCache.setModelParamsVoList(jobMissionDetail.getModelParamsVoList());
        put(jobID, catalogCache);
        return jobGraphVo;
    }

    @Override
    public MissionInfoVo jobCommit(JobGraphVo jobGraphVo) {
        String jobID = jobGraphVo.getJobInfo().getJob().getJobID();
        JobGraph jobGraph = JobGraph.updateFromJobGraphVo(jobGraphVo, getJobGraph(jobID).getJobInfo().getJob());
        put(jobID, jobGraph);
        MissionInfoVo missionInfoVo = new MissionInfoVo();
        missionInfoVo.setJobID(jobID);
        missionInfoVo.setMissionDetailVOs(getCatalogCache(jobID).getMissionDetailVOList());
        missionInfoVo.setModelParams(getCatalogCache(jobID).getModelParamsVoList());
        System.out.println(missionInfoVo.getJobID());

        List<ServiceParamsVo> serviceParamsVos = new ArrayList<>();
        for (Service service : jobGraph.getJobInfo().getServices()) {
            ServiceParamsVo serviceParamsVo = new ServiceParamsVo();
            serviceParamsVo.setServiceName(service.getServiceName());
            serviceParamsVo.setServiceClass(service.getServiceClass());
            serviceParamsVo.setServiceID(service.getServiceId());
            serviceParamsVo.setOrgDID(service.getOrgId());
            serviceParamsVos.add(serviceParamsVo);

        }
        missionInfoVo.setServiceParams(serviceParamsVos);
        return missionInfoVo;
    }

    @Override
    public JobRunner getJobRunner(JobInfoPo jobInfoPo) {
        OrgInfo orgInfo = getOrgInfo();
        JobRunnerInfo jobInfo = JobRunnerInfo.converterToJobInfo(jobInfoPo, orgInfo.getOrgId());
        JobRunner jobRunner = new JobRunner();
        jobRunner.setJob(jobInfo.getJob());
        jobRunner.setTaskList(jobInfo.getTasks());
        List<ServiceRunner> serviceRunners = converterToServiceRunner(jobInfo.getServices(), orgInfo.getOrgId());
        jobRunner.setServiceList(serviceRunners);
        return jobRunner;
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
        sqlParser.setCatalogConfig(catalogConfig);
        if (sqlVo.getIsStream() == 1) {
            JobBuilder jobBuilder = new JobBuilder(sqlVo.getModelType(), sqlVo.getIsStream(), sqlParser.parser(), getOrgInfo(), sqlParser.getSql());
            jobBuilder.build();
            JobMissionDetail jobMissionDetail = new JobMissionDetail();
            jobMissionDetail.setJobTemplate(jobBuilder.getJobTemplate());
            jobMissionDetail.setMissionDetailVOList(sqlParser.getMissionDetailVOs());
            jobMissionDetail.setModelParamsVoList(sqlParser.getModelParamsVos());
            jobMissionDetail.setAssetDetailList(sqlParser.getAssetDetailList());
            return jobMissionDetail;
        } else {
            JobBuilderWithOptimizer jobBuilder = new JobBuilderWithOptimizer(sqlVo.getModelType(), sqlVo.getIsStream(), sqlParser.parserWithOptimizer(), sqlParser.getColumnInfoMap(), getOrgId(), sqlParser.getSql());
            jobBuilder.build();
            JobMissionDetail jobMissionDetail = new JobMissionDetail();
            jobMissionDetail.setJobTemplate(jobBuilder.getJobTemplate());
            jobMissionDetail.setMissionDetailVOList(sqlParser.getMissionDetailVOs());
            jobMissionDetail.setModelParamsVoList(sqlParser.getModelParamsVos());
            jobMissionDetail.setAssetDetailList(sqlParser.getAssetDetailList());
            return jobMissionDetail;
        }
    }


    @Override
    public Dag taskToDag(List<Task> tasks, String dataStatus) {
        Dag dag = new Dag();
        List<DagNode> nodes = new ArrayList<>();
        List<DagEdge> edges = new ArrayList<>();
        int capacity = 100;
        int dataCount = 0;
        int id = dataCount;
        for (Task task : tasks) {
            DagNode dagNode = new DagNode();
            dagNode.setId(id);
            id += 1;
            String label = task.getModule().getModuleName() + "(" + task.getStatus() + ")";
            dagNode.setLabel(label);
            dagNode.setShape("image");
            dagNode.setImage(task.getModule().getModuleName().toLowerCase());
            dagNode.setFindLabel(task.getTaskName());
            nodes.add(dagNode);

            String taskName = task.getTaskName();
            for (TaskInputData taskInputData : task.getInput().getData()) {
                if (!Objects.equals(taskInputData.getTaskSrc(), "")) {
                    DagEdge dagEdge = new DagEdge();
                    dagEdge.setFrom(Integer.parseInt(taskInputData.getTaskSrc()));
                    dagEdge.setTo(Integer.parseInt(taskName));
                    dagEdge.setLabel(taskInputData.getDataName());
                    edges.add(dagEdge);
                } else {
                    DagNode dataNode = new DagNode();
                    dataNode.setId(capacity);

                    String dataLabel = taskInputData.getDataName() + "(" + dataStatus + ")";
                    dataNode.setLabel(dataLabel);
                    dataNode.setShape("image");
                    dataNode.setImage("dataSource");
                    dataNode.setFindLabel(task.getTaskName());
                    nodes.add(dataNode);

                    DagEdge dataEdge = new DagEdge();
                    dataEdge.setFrom(capacity);
                    dataEdge.setTo(Integer.parseInt(taskName));
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

    @Override
    public List<ServiceRunner> converterToServiceRunner(List<ServiceRunner> services, String orgDID) {
        List<ServiceRunner> serviceRunners = new ArrayList<>();

        HashMap<String, ExposeEndpoint> exposeEndpointMap = new HashMap<>();
        HashMap<String, String> clientIdMap = new HashMap<>();
        if (services != null) {
            for (ServiceRunner service : services) {
                clientIdMap.put(service.getId(), "");
            }
            for (ServiceRunner service : services) {
                for (ExposeEndpoint exposeEndpoint : service.getExposeEndpointList()) {
                    exposeEndpointMap.put(service.getId(), exposeEndpoint);
                }
                for (ReferExposeEndpoint referEndpoint : service.getReferExposeEndpointList()) {
                    if (!Objects.equals(referEndpoint.getReferServiceId(), "") && referEndpoint.getReferServiceId() != null) {
                        clientIdMap.put(referEndpoint.getReferServiceId(), service.getId());
                    }
                }
            }
            log.info("exposeEndpointMap {}", JSONObject.toJSONString(exposeEndpointMap));
            for (ServiceRunner service : services) {
                if (StringUtils.equals(service.getOrgId(), orgDID)) {
                    String path = service.getExposeEndpointList().get(0).getAddress();
                    String[] split = path.split(":");
                    if (split.length == 2) {
                        Integer port = Integer.valueOf(split[1]);
                        service.setNodePort((port));
                    }
                    if (null != service.getReferExposeEndpointList() && service.getReferExposeEndpointList().size() > 0) {
                        for (ReferExposeEndpoint referExposeEndpoint : service.getReferExposeEndpointList()) {
                            if (referExposeEndpoint != null) {
                                if (!Objects.equals(referExposeEndpoint.getName(), "")) {
                                    ExposeEndpoint referExposeEndpointRunner = exposeEndpointMap.get(referExposeEndpoint.getReferServiceId());
                                    if (referExposeEndpointRunner == null)
                                        continue;
                                    referExposeEndpoint.setAddress(referExposeEndpointRunner.getAddress());
//                                referExposeEndpoint.setPath(referExposeEndpoint.get("path"));
//                                referExposeEndpoint.setMethod(referExposeEndpoint.get("method"));
                                    referExposeEndpoint.setServiceCa(referExposeEndpointRunner.getServiceCa());
                                    referExposeEndpoint.setServiceCert(referExposeEndpointRunner.getServiceCert());
                                }
                            }
                        }
                    }


                    log.info("serviceRunner {}", JSONObject.toJSONString(service));
                    serviceRunners.add(service);
                }
            }
        }
        return serviceRunners;
    }

    private PlatformInfo getPlatformInfoFromBackend() {
        proto.GetAccountInfoRequest request = proto.GetAccountInfoRequest.newBuilder().setRequestId(UUID.randomUUID().toString()).build();
        proto.GetAccountInfoResponse getAccountInfoResponse = blockingStub.getAccountInfo(request);
        TypeReference<PlatformInfo> typeReference = new TypeReference<>() {
        };
        PlatformInfo platformInfo = null;
        try {
            platformInfo = ProtobufBeanUtil.toPojoBean(typeReference, getAccountInfoResponse.getData());
        } catch (IOException e) {
            log.error("getPlatformInfo. err code: {}, err msg: {}", ErrorEnum.ErrCodeProtoToBean.getErrorCode(), ErrorEnum.ErrCodeProtoToBean.getErrorMsg());
            throw new BizException(ErrorEnum.ErrCodeProtoToBean, e.getMessage());
        }
        if (platformInfo == null || platformInfo.getRegisterId() == 0) {
            log.error("getPlatformInfo. err code: {}, err msg: {}", ErrorEnum.ErrCodeLocalResourcePlatformInformationNotFound.getErrorCode(), ErrorEnum.ErrCodeLocalResourcePlatformInformationNotFound.getErrorMsg());
            throw new BizException(ErrorEnum.ErrCodeLocalResourcePlatformInformationNotFound, ErrorEnum.ErrCodeLocalResourcePlatformInformationNotFound.getErrorMsg());
        }

        String assetServiceAddr = this.assetServiceAddr.split("//")[1];
        String keyServiceAddr = this.keyServiceAddr.split("//")[1];
        platformInfo.setAssetServiceAddr(assetServiceAddr);
        platformInfo.setKeyServiceAddr(keyServiceAddr);
        this.platformInfo = platformInfo;
        return platformInfo;
    }
}



