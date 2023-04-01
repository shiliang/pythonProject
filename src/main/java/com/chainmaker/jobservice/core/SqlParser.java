package com.chainmaker.jobservice.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.FieldSerializer;
import com.chainmaker.jobservice.api.model.bo.config.CatalogConfig;
import com.chainmaker.jobservice.api.model.vo.ModelParamsVo;
import com.chainmaker.jobservice.core.analyzer.Analyzer;
import com.chainmaker.jobservice.core.analyzer.catalog.DataCatalogDetailInfo;
import com.chainmaker.jobservice.core.analyzer.catalog.DataCatalogInfo;
import com.chainmaker.jobservice.core.analyzer.catalog.MissionDetailVO;
import com.chainmaker.jobservice.core.calcite.adapter.LogicPlanAdapter;
import com.chainmaker.jobservice.core.calcite.optimizer.OptimizerPlanner;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.FieldInfo;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.TableInfo;
import com.chainmaker.jobservice.core.calcite.utils.parserWithOptimizerReturnValue;
import com.chainmaker.jobservice.core.optimizer.PlanOptimizer;
import com.chainmaker.jobservice.core.optimizer.nodes.DAG;
import com.chainmaker.jobservice.core.optimizer.plans.PhysicalPlan;
import com.chainmaker.jobservice.core.parser.LogicalPlanBuilder;
import com.chainmaker.jobservice.core.parser.LogicalPlanBuilderV2;
import com.chainmaker.jobservice.core.parser.plans.LogicalPlan;
import com.chainmaker.jobservice.core.parser.printer.LogicalPlanPrinter;
import org.apache.calcite.rel.RelNode;

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
        LogicalPlanBuilderV2 logicalPlanBuilder = new LogicalPlanBuilderV2(this.sql);
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

    /**
     * 带查询优化的parser
     * @return
     */
    public parserWithOptimizerReturnValue parserWithOptimizer() {
        LogicalPlanBuilderV2 logicalPlanBuilder = new LogicalPlanBuilderV2(this.sql);
        LogicalPlan logicalPlan = logicalPlanBuilder.getLogicalPlan();

        LogicalPlanPrinter printer = new LogicalPlanPrinter();
        printer.visitTree(logicalPlan, 0);
        System.out.println(printer.logicalPlanString);

        Analyzer analyzer = new Analyzer(this.catalogConfig);

        HashMap<String, String> tableOwnerMap = analyzer.getMetaData(logicalPlanBuilder.getTableNameMap(), logicalPlanBuilder.getModelNameList());
        missionDetailVOs = analyzer.getMissionDetailVOs();
        modelParamsVos = analyzer.getModelParamsVos();
        JSONArray dataCatalogInfoList = analyzer.getDataCatalogInfoList();
        HashMap<String, TableInfo> metadata = new HashMap<>();

        // 获取所需的元数据，即HashMap<String, TableInfo>
        for (DataCatalogInfo dataCatalogInfo : dataCatalogInfoList.toJavaObject(DataCatalogInfo[].class)) {
            String tableName = dataCatalogInfo.getName().toUpperCase();
            HashMap<String, FieldInfo> fields = new HashMap<>();
            for (DataCatalogDetailInfo detailInfo : dataCatalogInfo.getItemVOList()) {
                FieldInfo field = new FieldInfo(tableName+"."+detailInfo.getName().toUpperCase(), detailInfo.getDataType(), null, null, FieldInfo.DistributionType.Uniform,
                        tableName, detailInfo.getDataLength(), dataCatalogInfo.getOrgDID());
                fields.put(field.getFieldName(), field);
            }
            // 后续需要添加rowCount的获取
            int rowCount = 100;
            TableInfo tableInfo = new TableInfo(fields, rowCount, tableName, dataCatalogInfo.getOrgDID());
            metadata.put(tableName, tableInfo);
        }

        LogicPlanAdapter planAdapter = new LogicPlanAdapter(this.sql.toUpperCase(), logicalPlan, metadata, modelType);
        planAdapter.CastToRelNode();

        // 查询优化部分
        RelNode root = planAdapter.getRoot();
        OptimizerPlanner planner = new OptimizerPlanner(root, true);
        RelNode phyPlan = planner.optimize();

        return new parserWithOptimizerReturnValue(phyPlan, logicalPlan);
    }
}
