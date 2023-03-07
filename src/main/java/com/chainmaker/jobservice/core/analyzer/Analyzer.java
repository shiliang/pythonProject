package com.chainmaker.jobservice.core.analyzer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.model.bo.config.CatalogConfig;
import com.chainmaker.jobservice.api.model.vo.ModelParamsVo;
import com.chainmaker.jobservice.api.response.ParserException;
import com.chainmaker.jobservice.api.response.RestException;
import com.chainmaker.jobservice.core.analyzer.catalog.DataCatalogInfo;
import com.chainmaker.jobservice.core.analyzer.catalog.MissionDetailVO;
import com.chainmaker.jobservice.core.parser.plans.LogicalPlanVisitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author gaokang
 * @date 2022-08-30 17:54
 * @description:根据数据目录解析，得到解析后的逻辑计划
 * @version: 1.0.0
 */
@Slf4j
public class Analyzer extends LogicalPlanVisitor {
    private CatalogConfig catalogConfig;


    private static final String TEE_PARTY_KEY = "TEE-PARTY";
    private static final String TEE_PARTY = "69vkhy6org.cm-5w2wtw3afr";
    private List<MissionDetailVO> missionDetailVOs = new ArrayList<>();
    private List<ModelParamsVo> modelParamsVos = new ArrayList<>();
    public List<MissionDetailVO> getMissionDetailVOs() {
        return missionDetailVOs;
    }

    public Analyzer(CatalogConfig catalogConfig) {
        this.catalogConfig = catalogConfig;
    }

    public List<ModelParamsVo> getModelParamsVos() {
        return modelParamsVos;
    }

    /***
     * @description 根据表名获取表信息，获取计算资源信息
     * @param
     * @return void
     * @author gaokang
     * @date 2022/9/5 17:26
     */
    public HashMap<String, String> getMetaData(HashMap<String, String> tableNameMap, List<String> modelNameList) {
        String catalog_url = "http://" + catalogConfig.getAddress() + ":" + catalogConfig.getPort() + "/dataCatalog/name/all";
        String model_url = "http://" + catalogConfig.getAddress() + ":" + catalogConfig.getPort() + "/confidentialComputings/methodName/";
        HashMap<String, String> tableOwnerMap = new HashMap<>();

        List<MissionDetailVO> missionDetailVOList = new ArrayList<>();
        for (String name : tableNameMap.values()) {
            MissionDetailVO missionDetailVO = new MissionDetailVO();
            missionDetailVO.setName(name);
            missionDetailVOList.add(missionDetailVO);
        }
        log.info("request data： " + missionDetailVOList);
        log.info("request url： " + catalog_url);
        RestTemplate restTemplate = new RestTemplate();
        JSONObject response = restTemplate.postForObject(catalog_url, missionDetailVOList, JSONObject.class);
        log.info("response： " + response);
        if (response == null || response.getJSONArray("data").isEmpty()) {
            throw new RestException(response.toString());
        }
        JSONArray dataCatalogInfoList = response.getJSONArray("data");
        for (DataCatalogInfo dataCatalogInfo : dataCatalogInfoList.toJavaObject(DataCatalogInfo[].class)) {
            tableOwnerMap.put(dataCatalogInfo.getName(), dataCatalogInfo.getOrgDID());

            MissionDetailVO missionDetailVO = new MissionDetailVO();
            missionDetailVO.setName(dataCatalogInfo.getName());
            missionDetailVO.setRemark(dataCatalogInfo.getRemark());
            missionDetailVO.setVersion(dataCatalogInfo.getVersion());
            missionDetailVO.setOrgId(dataCatalogInfo.getOrgId());
            missionDetailVO.setDatacatalogId(dataCatalogInfo.getId());
            missionDetailVOs.add(missionDetailVO);
        }
        for (String modelName : modelNameList) {
            JSONObject modelResult = JSONObject.parseObject(restTemplate.getForObject(model_url + modelName, String.class));
            if (modelResult == null || modelResult.getJSONObject("data").isEmpty()) {
                throw new RestException(response.toString());
            }
            ModelParamsVo modelParamsVo = JSONObject.parseObject(modelResult.getString("data"), ModelParamsVo.class);
            modelParamsVos.add(modelParamsVo);
        }


        tableOwnerMap.put(TEE_PARTY_KEY, TEE_PARTY);
        return tableOwnerMap;
    }

}
