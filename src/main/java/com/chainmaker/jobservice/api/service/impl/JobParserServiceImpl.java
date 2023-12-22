package com.chainmaker.jobservice.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.builder.JobBuilder;
import com.chainmaker.jobservice.api.builder.JobBuilderWithOptimizer;
import com.chainmaker.jobservice.api.model.bo.*;
import com.chainmaker.jobservice.api.model.bo.config.CatalogConfig;
import com.chainmaker.jobservice.api.model.bo.graph.*;
import com.chainmaker.jobservice.api.model.bo.job.JobInfo;
import com.chainmaker.jobservice.api.model.bo.job.JobTemplate;
import com.chainmaker.jobservice.api.model.bo.job.service.ReferEndpoint;
import com.chainmaker.jobservice.api.model.bo.job.service.Service;
import com.chainmaker.jobservice.api.model.bo.job.task.Task;
import com.chainmaker.jobservice.api.model.bo.job.task.TaskInputData;
import com.chainmaker.jobservice.api.model.po.contract.JobInfoPo;
import com.chainmaker.jobservice.api.model.po.contract.ServiceParamsPo;
import com.chainmaker.jobservice.api.model.po.contract.ServiceUpdatePo;
import com.chainmaker.jobservice.api.model.po.data.ServiceValueParam;
import com.chainmaker.jobservice.api.model.po.data.UserInfo;
import com.chainmaker.jobservice.api.model.vo.*;
import com.chainmaker.jobservice.api.response.ParserException;
import com.chainmaker.jobservice.api.service.JobParserService;
import com.chainmaker.jobservice.core.SqlParser;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @author gaokang
 * @date 2022-09-20 19:29
 * @description:Job解析
 * @version: 1.0.0
 */

@org.springframework.stereotype.Service
public class JobParserServiceImpl implements JobParserService {
    private CatalogConfig catalogConfig;
    private String orgId;
    private String orgDID;
    private HashMap<String, JobGraph> jobGraphHashMap = new HashMap<>();
    private HashMap<String, CatalogCache> catalogCacheHashMap = new HashMap<>();

    public void setCatalogConfig(CatalogConfig catalogConfig) {
        this.catalogConfig = catalogConfig;
    }
    @Override
    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    @Override
    public String getOrgId() {
        return this.orgId;
    }

    @Override
    public String getOrgDID() {
        return orgDID;
    }

    @Override
    public void setOrgDID(String orgDID) {
        this.orgDID = orgDID;
    }

    @Override
    public void save(List<ServiceValueParam> serviceValues) {
        String url = "http://" + catalogConfig.getAddress() + ":" + catalogConfig.getPort() + "/missions/services";
        RestTemplate restTemplate = new RestTemplate();
        JSONObject response = restTemplate.postForObject(url, serviceValues, JSONObject.class);
        if (response == null) {
            throw new ParserException("本地持久化失败");
        }
    }

    @Override
    public List<ServiceValueParam> get(String orgDID, String jobID) {
        String url = "http://" + catalogConfig.getAddress() + ":" + catalogConfig.getPort() + "/missions/serviceValues/" + orgDID + "/local/" + jobID;
        RestTemplate restTemplate = new RestTemplate();
        JSONObject result = JSONObject.parseObject(restTemplate.getForObject(url, String.class), Feature.OrderedField);
        ServiceValueParam[] serviceValueParams = JSONObject.parseObject(result.getString("data"), ServiceValueParam[].class, Feature.OrderedField);
        return new ArrayList<>(Arrays.asList(serviceValueParams));
    }

    @Override
    public UserInfo getUserInfo(String userName) {
        String url = "http://" + catalogConfig.getAddress() + ":" + catalogConfig.getPort() + "/login/orgDID/" + userName;
        RestTemplate restTemplate = new RestTemplate();
        JSONObject result = JSONObject.parseObject(restTemplate.getForObject(url, String.class), Feature.OrderedField);
        return JSONObject.parseObject(result.getString("data"), UserInfo.class, Feature.OrderedField);
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
        JobInfoVo jobInfoVo = JobInfoVo.converterToJobInfoVo(jobTemplate);
        JobInfo jobInfo = JobInfo.jobTemplateToJobInfo(jobTemplate);


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
        if (jobInfoVo.getServices() != null) {
            for (ServiceVo serviceVo : jobInfoVo.getServices()) {
                for (ExposeEndpointVo exposeEndpointVo : serviceVo.getExposeEndpoints()) {
                    exposeEndpointVo.getForm().removeIf(exposeFormVo -> Objects.equals(exposeFormVo.getKey(), "serviceCa") || Objects.equals(exposeFormVo.getKey(), "serviceCert") || Objects.equals(exposeFormVo.getKey(), "serviceKey"));
                }
            }
        }
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
            serviceParamsVo.setServiceID(service.getId());
            serviceParamsVo.setOrgDID(service.getOrgDID());
            serviceParamsVos.add(serviceParamsVo);

        }
        missionInfoVo.setServiceParams(serviceParamsVos);
        return missionInfoVo;
    }

    @Override
    public JobInfo jobCreate(MissionInfo missionInfo) {
        String jobID = missionInfo.getJobID();
        JobGraph jobGraph = getJobGraph(jobID);
        JobInfo jobInfo = jobGraph.getJobInfo();

        jobInfo.getJob().setJobName(missionInfo.getName());
        jobInfo.getJob().setCreateTime(String.valueOf(System.currentTimeMillis()));
        jobInfo.getJob().setUpdateTime(String.valueOf(System.currentTimeMillis()));
        jobInfo.getJob().setProjectID(missionInfo.getId());
        jobInfo.getJob().setRequestData(missionInfo.getSqltext());
        jobInfo.getJob().setSubmitter(missionInfo.getCreateOrgDID());
        HashSet<String> parties = new HashSet<>();
        for (ServiceParamsVo serviceParamsVo : missionInfo.getServiceParams()) {
            if (!serviceParamsVo.getOrgDID().equals("")) {
                parties.add(serviceParamsVo.getOrgDID());
            }
        }
//        for (MissionDetailInfo missionDetailInfo : missionInfo.getMissionDetailInfos()) {
//            parties.add(missionDetailInfo.getOrgId());
//        }
        for (String value: jobInfo.getJob().getParties()) {
            parties.add(value);
        }
        jobInfo.getJob().setParties(new ArrayList<>(parties));
        return jobInfo;
    }

    @Override
    public JobGraphVo getJobApproval(JobInfoPo jobInfoPo) {
        JobInfo jobInfo = JobInfo.converterToJobInfo(jobInfoPo);

        int nodePort = 31004;
        if (jobInfo.getServices() != null) {
            for (Service service : jobInfo.getServices()) {
                if (service.getOrgDID().equals(getOrgDID())) {
                    UserInfo userInfo = getUserInfo(service.getOrgDID());
                    service.setNodePort(nodePort);
                    nodePort += 1;
                    for (HashMap<String, String> exposeEndpoint : service.getExposeEndpoints().values()) {
                        exposeEndpoint.put("serviceCa", userInfo.getCaCert());
                        exposeEndpoint.put("serviceCert", userInfo.getTlsCert());
                        exposeEndpoint.put("serviceKey", userInfo.getTlsKey());
                    }
                }
            }
        }
        JobInfoVo jobInfoVo = JobInfoVo.jobInfoToJobInfoVo(jobInfo);


        JobGraphVo jobGraphVo = new JobGraphVo();
        if (jobInfo.getServices() != null) {
            Topology topology = serviceToTopology(jobInfo.getServices(), 1);
            jobGraphVo.setTopology(topology);
        }
        if (jobInfo.getTasks() != null) {
            Dag dag = taskToDag(jobInfo.getTasks(), jobInfoPo.getJob().getStatus());
            jobGraphVo.setDag(dag);
        }
        jobGraphVo.setJobInfo(jobInfoVo);
//        jobGraphVo.checkSql(jobInfoPo.getJob().getRequestData());
        return jobGraphVo;
    }

    @Override
    public JobGraphVo getJobInfo(JobInfoPo jobInfoPo) {
        JobInfo jobInfo = JobInfo.converterToJobInfo(jobInfoPo);
        JobInfoVo jobInfoVo = JobInfoVo.jobInfoToJobInfoVo(jobInfo);


        JobGraphVo jobGraphVo = new JobGraphVo();
        if (jobInfo.getTasks() != null) {
            Dag dag = taskToDag(jobInfo.getTasks(), jobInfoPo.getJob().getStatus());
            jobGraphVo.setDag(dag);
        }
        if (jobInfo.getServices() != null) {
            Topology topology = serviceToTopology(jobInfo.getServices(), 1);
            jobGraphVo.setTopology(topology);

            for (ServiceVo serviceVo : jobInfoVo.getServices()) {
                for (ExposeEndpointVo exposeEndpointVo : serviceVo.getExposeEndpoints()) {
                    exposeEndpointVo.getForm().removeIf(exposeFormVo -> Objects.equals(exposeFormVo.getKey(), "serviceKey"));
                }
            }
        }
        jobGraphVo.setJobInfo(jobInfoVo);


        return jobGraphVo;
    }

    @Override
    public JobRunner getJobRunner(JobInfoPo jobInfoPo) {
        JobInfo jobInfo = JobInfo.converterToJobInfo(jobInfoPo);
        String jobID = jobInfo.getJob().getJobID();
        String orgDID = getOrgDID();
        List<ServiceValueParam> serviceValueParams = get(orgDID, jobID);
        List<ServiceRunner> serviceRunners = converterToServiceRunner(jobInfo.getServices(), serviceValueParams, orgDID);
        JobRunner jobRunner = new JobRunner();
        jobRunner.setJob(jobInfo.getJob());
        jobRunner.setTasks(jobInfo.getTasks());
        jobRunner.setServices(serviceRunners);
        return jobRunner;
    }

    @Override
    public ServiceUpdatePo updateService(ServiceUpdateVo serviceUpdateVo) {
        String jobID = serviceUpdateVo.getJobInfo().getJob().getJobID();
        String orgDID = serviceUpdateVo.getOrgDID();

        List<ServiceValueParam> serviceValueParamList = new ArrayList<>();
        List<ServiceParamsPo> serviceParamsPoList = new ArrayList<>();
        for (ServiceVo serviceVo : serviceUpdateVo.getJobInfo().getServices()) {
            if (Objects.equals(serviceVo.getOrgDID(), orgDID)) {
                Service service = Service.serviceVoToService(serviceVo, jobID);
                ServiceValueParam serviceValueParam = new ServiceValueParam();
                serviceValueParam.setId(service.getId());
                serviceValueParam.setOrgDID(service.getOrgDID());
                serviceValueParam.setJobID(service.getJobID());
                serviceValueParam.setName(service.getServiceName());
                serviceValueParam.setNodePort(String.valueOf(service.getNodePort()));
                for (HashMap<String, String> exposeEndpoint : service.getExposeEndpoints().values()) {
                    serviceValueParam.setServiceCa(exposeEndpoint.get("serviceCa").getBytes());
                    serviceValueParam.setServiceCert(exposeEndpoint.get("serviceCert").getBytes());
                    serviceValueParam.setServiceKey(exposeEndpoint.get("serviceKey").getBytes());
                }
                serviceValueParamList.add(serviceValueParam);

                ServiceParamsPo serviceParamsPo = ServiceParamsPo.converterToServiceParamsPo(service);
                serviceParamsPoList.add(serviceParamsPo);
            }
        }
        save(serviceValueParamList);
        ServiceUpdatePo serviceUpdatePo = new ServiceUpdatePo();
        serviceUpdatePo.setServiceParamsPoList(serviceParamsPoList);
        return serviceUpdatePo;
    }

    @Override
    public JobMissionDetail parserSql(SqlVo sqlVo) {
        String sqltext = sqlVo.getSqltext().replace("\"", "");
        SqlParser sqlParser = new SqlParser(sqltext, sqlVo.getModelType(), sqlVo.getIsStream());
        sqlParser.setCatalogConfig(catalogConfig);
        if (sqlVo.getIsStream() == 1) {
            JobBuilder jobBuilder = new JobBuilder(sqlVo.getModelType(), sqlVo.getIsStream(), sqlParser.parser(), getOrgDID());
            jobBuilder.build();
            JobMissionDetail jobMissionDetail = new JobMissionDetail();
            jobMissionDetail.setJobTemplate(jobBuilder.getJobTemplate());
            jobMissionDetail.setMissionDetailVOList(sqlParser.getMissionDetailVOs());
            jobMissionDetail.setModelParamsVoList(sqlParser.getModelParamsVos());
            return jobMissionDetail;
        } else {
            JobBuilderWithOptimizer jobBuilder = new JobBuilderWithOptimizer(sqlVo.getModelType(), sqlVo.getIsStream(), sqlParser.parserWithOptimizer(), sqlParser.getColumnInfoMap());
            jobBuilder.build();
            JobMissionDetail jobMissionDetail = new JobMissionDetail();
            jobMissionDetail.setJobTemplate(jobBuilder.getJobTemplate());
            jobMissionDetail.setMissionDetailVOList(sqlParser.getMissionDetailVOs());
            jobMissionDetail.setModelParamsVoList(sqlParser.getModelParamsVos());
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
            for (TaskInputData taskInputData: task.getInput().getData()) {
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
        dag.setNodes(nodes);
        dag.setEdges(edges);
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
                node.setId(service.getId());
                node.setStatus(service.getStatus());
                node.setServiceType(service.getServiceClass());
                node.setNodeError(true);
                node.setAverageTime("-");
                node.setTotalQueries("-");
                node.setSuccessRate("-");
                node.setName(service.getServiceName());
                node.setDataType("ROOT");
                if (Integer.parseInt(service.getId()) > 1) {
                    node.setDataType("SUBROOT");
                }
                nodes.add(node);
                for (ReferEndpoint referEndpoint : service.getReferEndpoints().values()) {
                    if (!Objects.equals(referEndpoint.getName(), "")) {
                        TopologyEdge edge = new TopologyEdge();
                        edge.setSource(service.getId());
                        edge.setTarget(referEndpoint.getReferServiceID());
                        TopologyEdgeData topologyEdgeData = new TopologyEdgeData();
                        topologyEdgeData.setType("data");
                        edge.setData(topologyEdgeData);
                        edges.add(edge);
                    }
                }
            }
            topology.setNodes(nodes);
            topology.setEdges(edges);
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
    public List<ServiceRunner> converterToServiceRunner(List<Service> services, List<ServiceValueParam> serviceValueParams, String orgDID) {
        List<ServiceRunner> serviceRunners = new ArrayList<>();

        HashMap<String, HashMap<String, String>> exposeEndpointMap = new HashMap<>();
        HashMap<String, String> clientIdMap = new HashMap<>();
        if (services != null) {
            for (Service service : services) {
                clientIdMap.put(service.getId(), "");
            }
            for (Service service : services) {
                for (HashMap<String, String> exposeEndpoint : service.getExposeEndpoints().values()) {
                    exposeEndpointMap.put(service.getId(), exposeEndpoint);
                }
                for (ReferEndpoint referEndpoint : service.getReferEndpoints().values()) {
                    if (!Objects.equals(referEndpoint.getReferServiceID(), "") && referEndpoint.getReferServiceID() != null) {
                        clientIdMap.put(referEndpoint.getReferServiceID(), service.getId());
                    }
                }
            }
            for (Service service : services) {
                if (Objects.equals(service.getOrgDID(), orgDID)) {
                    for (ServiceValueParam serviceValueParam : serviceValueParams) {
                        if (Objects.equals(service.getId(), serviceValueParam.getId())) {
                            String serviceStr = JSONObject.toJSONString(service);
                            ServiceRunner serviceRunner = JSONObject.parseObject(serviceStr, ServiceRunner.class, Feature.OrderedField);
                            serviceRunner.setNodePort(Integer.parseInt(serviceValueParam.getNodePort()));
                            for (HashMap<String, String> exposeEndpoint : serviceRunner.getExposeEndpoints().values()) {
                                if (!Objects.equals(clientIdMap.get(serviceRunner.getId()), "")) {
                                    HashMap<String, String> clientExposeEndpoint = exposeEndpointMap.get(clientIdMap.get(serviceRunner.getId()));
                                    exposeEndpoint.put("clientCa", clientExposeEndpoint.get("serviceCa"));
                                    exposeEndpoint.put("clientCert", clientExposeEndpoint.get("serviceCert"));
                                }
                                exposeEndpoint.put("serviceKey", new String(serviceValueParam.getServiceKey()));
                            }
                            for (ReferEndpointRunner referEndpointRunner : serviceRunner.getReferEndpoints().values()) {
                                if (referEndpointRunner != null) {
                                    if (!Objects.equals(referEndpointRunner.getName(), "")) {
                                        HashMap<String, String> referExposeEndpoint = exposeEndpointMap.get(referEndpointRunner.getReferServiceID());
                                        referEndpointRunner.setAddress(referExposeEndpoint.get("address"));
                                        referEndpointRunner.setPath(referExposeEndpoint.get("path"));
                                        referEndpointRunner.setMethod(referExposeEndpoint.get("method"));
                                        referEndpointRunner.setServiceCa(referExposeEndpoint.get("serviceCa"));
                                        referEndpointRunner.setServiceCert(referExposeEndpoint.get("serviceCert"));
                                    }
                                }
                            }
                            serviceRunners.add(serviceRunner);
                        }
                    }
                }
            }
        }
        return serviceRunners;
    }



}
