package com.chainmaker.jobservice.api.model.vo;

import com.chainmaker.jobservice.core.analyzer.catalog.MissionDetailVO;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-10-20 19:53
 * @description:
 * @version:
 */
public class CatalogCache {
    private List<MissionDetailVO> missionDetailVOList;
    private List<ModelParamsVo> modelParamsVoList;

    public List<MissionDetailVO> getMissionDetailVOList() {
        return missionDetailVOList;
    }

    public void setMissionDetailVOList(List<MissionDetailVO> missionDetailVOList) {
        this.missionDetailVOList = missionDetailVOList;
    }

    public List<ModelParamsVo> getModelParamsVoList() {
        return modelParamsVoList;
    }

    public void setModelParamsVoList(List<ModelParamsVo> modelParamsVoList) {
        this.modelParamsVoList = modelParamsVoList;
    }
}
