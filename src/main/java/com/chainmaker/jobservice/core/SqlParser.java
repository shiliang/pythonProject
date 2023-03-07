package com.chainmaker.jobservice.core;

import com.chainmaker.jobservice.api.model.bo.config.CatalogConfig;
import com.chainmaker.jobservice.api.model.vo.ModelParamsVo;
import com.chainmaker.jobservice.core.analyzer.Analyzer;
import com.chainmaker.jobservice.core.analyzer.catalog.MissionDetailVO;
import com.chainmaker.jobservice.core.optimizer.PlanOptimizer;
import com.chainmaker.jobservice.core.optimizer.nodes.DAG;
import com.chainmaker.jobservice.core.optimizer.plans.PhysicalPlan;
import com.chainmaker.jobservice.core.parser.LogicalPlanBuilder;
import com.chainmaker.jobservice.core.parser.plans.LogicalPlan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-05 19:17
 * @description:SQL语句解析到隐私计算执行计划
 * @version: 1.0.0
 */

public class SqlParser {
    private final String sql;
    private final Integer isStream;
    private final Integer modelType;
    private List<MissionDetailVO> missionDetailVOs = new ArrayList<>();
    private List<ModelParamsVo> modelParamsVos = new ArrayList<>();
    private CatalogConfig catalogConfig;

    public void setCatalogConfig(CatalogConfig catalogConfig) {
        this.catalogConfig = catalogConfig;
    }

    public List<MissionDetailVO> getMissionDetailVOs() {
        return missionDetailVOs;
    }

    public List<ModelParamsVo> getModelParamsVos() {
        return modelParamsVos;
    }

    public SqlParser(String sql, Integer modelType, Integer isStream) {
        this.sql = sql;
        this.isStream = isStream;
        this.modelType = modelType;
    }

    public SqlParser(String sql) {
        this.sql = sql;
        this.isStream = 0;
        this.modelType = 0;
    }

    public DAG<PhysicalPlan> parser() {
        LogicalPlanBuilder logicalPlanBuilder = new LogicalPlanBuilder(this.sql);
        LogicalPlan logicalPlan = logicalPlanBuilder.getLogicalPlan();

        Analyzer analyzer = new Analyzer(this.catalogConfig);

        HashMap<String, String> tableOwnerMap = analyzer.getMetaData(logicalPlanBuilder.getTableNameMap(), logicalPlanBuilder.getModelNameList());
        missionDetailVOs = analyzer.getMissionDetailVOs();
        modelParamsVos = analyzer.getModelParamsVos();

        PlanOptimizer optimizer = new PlanOptimizer(this.modelType, this.isStream, tableOwnerMap);
        optimizer.visit(logicalPlan);
        DAG<PhysicalPlan> planDag = optimizer.getDag();
        return planDag;
    }
}
