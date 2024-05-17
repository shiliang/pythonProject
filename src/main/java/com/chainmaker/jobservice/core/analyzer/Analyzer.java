package com.chainmaker.jobservice.core.analyzer;

import com.alibaba.fastjson.JSONArray;
import com.chainmaker.jobservice.api.model.vo.ModelParamsVo;
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


    private static final String TEE_PARTY_KEY = "TEE-PARTY";
    private static final String TEE_PARTY = "69vkhy6org.cm-5w2wtw3afr";
    private List<MissionDetailVO> missionDetailVOs = new ArrayList<>();
    private List<ModelParamsVo> modelParamsVos = new ArrayList<>();

    private JSONArray dataCatalogInfoList;
    private HashMap<String, String> columnInfoMap = new HashMap<>();

    public HashMap<String, String> getColumnInfoMap() {
        return columnInfoMap;
    }

    public JSONArray getDataCatalogInfoList() {return dataCatalogInfoList; }
    public List<MissionDetailVO> getMissionDetailVOs() {
        return missionDetailVOs;
    }


    public List<ModelParamsVo> getModelParamsVos() {
        return modelParamsVos;
    }



}
