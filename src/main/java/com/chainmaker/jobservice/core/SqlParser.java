package com.chainmaker.jobservice.core;

import com.chainmaker.jobservice.api.model.AssetInfo;
import com.chainmaker.jobservice.api.model.DataInfo;
import com.chainmaker.jobservice.api.model.SaveTableColumnItem;
import com.chainmaker.jobservice.api.model.bo.config.CatalogConfig;
import com.chainmaker.jobservice.api.model.vo.ModelParamsVo;
import com.chainmaker.jobservice.core.analyzer.catalog.MissionDetailVO;
import com.chainmaker.jobservice.core.calcite.adapter.LogicPlanAdapter;
import com.chainmaker.jobservice.core.calcite.optimizer.OptimizerPlanner;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.FieldInfo;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.TableInfo;
import com.chainmaker.jobservice.core.calcite.utils.parserWithOptimizerReturnValue;
import com.chainmaker.jobservice.core.optimizer.PlanOptimizer;
import com.chainmaker.jobservice.core.optimizer.nodes.DAG;
import com.chainmaker.jobservice.core.optimizer.plans.PhysicalPlan;
import com.chainmaker.jobservice.core.parser.LogicalPlanBuilderV2;
import com.chainmaker.jobservice.core.parser.plans.LogicalPlan;
import com.chainmaker.jobservice.core.parser.printer.LogicalPlanPrinter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.calcite.rel.RelNode;

import java.util.HashMap;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-05 19:17
 * @description:SQL语句解析到隐私计算执行计划
 * @version: 1.0.0
 */

@Data
public class SqlParser {
    private final String sql;
    private final Integer isStream;
    private final Integer modelType;

    private List<AssetInfo> assetInfoList;

    private List<ModelParamsVo> modelParamsVos;

    private List<MissionDetailVO> missionDetailVOs = Lists.newArrayList();

    private HashMap<String, String> columnInfoMap = Maps.newHashMap();

    private CatalogConfig catalogConfig;


    private static final String TEE_PARTY_KEY = "TEE-PARTY";
    private static final String TEE_PARTY = "69vkhy6org.cm-5w2wtw3afr";

    public SqlParser(String sql, Integer isStream, Integer modelType,
                     List<AssetInfo> assetInfoList,
                     List<ModelParamsVo> modelParamsVos) {
        this.sql = sql;
        this.isStream = isStream;
        this.modelType = modelType;
        this.assetInfoList = assetInfoList;
        if(modelType == 2) {
            this.modelParamsVos = modelParamsVos;
        }
    }


    public HashMap<String, String> buildMetaData(List<AssetInfo> dataCatalogInfoList){
        HashMap<String, String> tableOwnerMap = new HashMap<>();
        for (AssetInfo dataCatalogInfo : dataCatalogInfoList) {
            tableOwnerMap.put(dataCatalogInfo.getAssetEnName(), dataCatalogInfo.getHolderCompany());
            MissionDetailVO missionDetailVO = new MissionDetailVO();
            missionDetailVO.setName(dataCatalogInfo.getAssetEnName());
            missionDetailVO.setRemark(dataCatalogInfo.getIntro());
            missionDetailVO.setVersion(dataCatalogInfo.getScale());
            missionDetailVO.setDatacatalogId(dataCatalogInfo.getAssetId());
            missionDetailVOs.add(missionDetailVO);
            System.out.println("dataCatalogInfo: " + dataCatalogInfo);
            DataInfo dataInfo = dataCatalogInfo.getDataInfo();
            for (SaveTableColumnItem dataCatalogDetailInfo : dataInfo.getItemList()) {
                columnInfoMap.put(dataCatalogInfo.getAssetName().toUpperCase()+"."+dataCatalogDetailInfo.getName().toUpperCase(), String.valueOf(dataCatalogDetailInfo.getDataType()));
            }
        }
        tableOwnerMap.put(TEE_PARTY_KEY, TEE_PARTY);
        return tableOwnerMap;
    }

    public DAG<PhysicalPlan> parser() {
        LogicalPlanBuilderV2 logicalPlanBuilder = new LogicalPlanBuilderV2(this.sql);
        LogicalPlan logicalPlan = logicalPlanBuilder.getLogicalPlan();
        LogicalPlanPrinter printer = new LogicalPlanPrinter();
        printer.visitTree(logicalPlan, 0);
        System.out.println(printer.logicalPlanString);
        HashMap<String, String> tableOwnerMap = buildMetaData(assetInfoList);

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

        HashMap<String, String> tableOwnerMap = buildMetaData(assetInfoList);

        HashMap<String, TableInfo> metadata = new HashMap<>();

        // 获取所需的元数据，即HashMap<String, TableInfo>
        for (AssetInfo assetInfo : assetInfoList) {
            HashMap<String, FieldInfo> fields = new HashMap<>();
            DataInfo dataInfo = assetInfo.getDataInfo();
            String tableName = dataInfo.getTableName().toUpperCase();
            String assetName = assetInfo.getAssetEnName().toUpperCase();
            for (SaveTableColumnItem detailInfo : dataInfo.getItemList()) {
                FieldInfo field = new FieldInfo(assetName+"."+detailInfo.getName().toUpperCase(), detailInfo.getDataType(), null, null, FieldInfo.DistributionType.Uniform,
                        tableName, detailInfo.getDataLength(), assetInfo.getHolderCompany());
                fields.put(field.getFieldName(), field);
            }
            // 后续需要添加rowCount的获取
            int rowCount = 100;
            TableInfo tableInfo = new TableInfo(fields, rowCount, tableName, assetInfo.getHolderCompany());
            metadata.put(tableName, tableInfo);
        }

        // 之前的sqlparser约用时1500ms
        // 接入Calcite    3000ms
        LogicPlanAdapter planAdapter = new LogicPlanAdapter(this.sql.toUpperCase(), logicalPlan, metadata, modelType);

        planAdapter.CastToRelNode();

        // 查询优化部分   300ms
        RelNode root = planAdapter.getRoot();
        OptimizerPlanner planner = new OptimizerPlanner(root, true);
        RelNode phyPlan = planner.optimize();

        return new parserWithOptimizerReturnValue(phyPlan, logicalPlan);
    }
}
