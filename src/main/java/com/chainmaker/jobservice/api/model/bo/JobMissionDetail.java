package com.chainmaker.jobservice.api.model.bo;

import com.chainmaker.jobservice.api.model.AssetDetail;
import com.chainmaker.jobservice.api.model.job.Job;
import com.chainmaker.jobservice.api.model.vo.ModelParamsVo;
import com.chainmaker.jobservice.core.analyzer.catalog.MissionDetailVO;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-23 15:24
 * @description:
 * @version:
 */

public class JobMissionDetail {
    private Job job;
    private List<MissionDetailVO> missionDetailVOList;
    private List<ModelParamsVo> modelParamsVoList;

    private List<AssetDetail> assetDetailList;

    public List<ModelParamsVo> getModelParamsVoList() {
        return modelParamsVoList;
    }

    public void setModelParamsVoList(List<ModelParamsVo> modelParamsVoList) {
        this.modelParamsVoList = modelParamsVoList;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public List<MissionDetailVO> getMissionDetailVOList() {
        return missionDetailVOList;
    }

    public void setMissionDetailVOList(List<MissionDetailVO> missionDetailVOList) {
        this.missionDetailVOList = missionDetailVOList;
    }

    public List<AssetDetail> getAssetDetailList() {
        return assetDetailList;
    }

    public void setAssetDetailList(List<AssetDetail> assetDetailList) {
        this.assetDetailList = assetDetailList;
    }

    @Override
    public String toString() {
        return "JobMissionDetail{" +
                ", missionDetailVOList=" + missionDetailVOList +
                ", modelParamsVoList=" + modelParamsVoList +
                ", assetDetailList=" + assetDetailList +
                '}';
    }
}
