package com.chainmaker.jobservice.core;

import com.chainmaker.jobservice.api.model.AssetDetail;
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
import com.chainmaker.jobservice.core.calcite.utils.ParserWithOptimizerReturnValue;
import com.chainmaker.jobservice.core.optimizer.PlanOptimizer;
import com.chainmaker.jobservice.core.optimizer.nodes.DAG;
import com.chainmaker.jobservice.core.optimizer.plans.PhysicalPlan;
import com.chainmaker.jobservice.core.parser.LogicalPlanBuilderV2;
import com.chainmaker.jobservice.core.parser.plans.XPCPlan;
import com.chainmaker.jobservice.core.parser.printer.LogicalPlanPrinter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-05 19:17
 * @description:SQL语句解析到隐私计算执行计划
 * @version: 1.0.0
 */

@Data
@Slf4j
public class SqlParser {
    private final String sql;
    private final Integer isStream;
    private final Integer modelType;

    private List<AssetInfo> assetInfoList;

    private List<ModelParamsVo> modelParamsVos;

    private List<MissionDetailVO> missionDetailVOs = Lists.newArrayList();

    private HashMap<String, String> columnInfoMap = Maps.newHashMap();

    private CatalogConfig catalogConfig;

    private List<AssetDetail> assetDetailList = new ArrayList<>();


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
            String assetEnName = dataCatalogInfo.getAssetEnName();
            String[] splits = assetEnName.split("_");
            tableOwnerMap.put(assetEnName, splits[splits.length - 1]);
            MissionDetailVO missionDetailVO = new MissionDetailVO();
            missionDetailVO.setName(dataCatalogInfo.getAssetEnName());
            missionDetailVO.setRemark(dataCatalogInfo.getIntro());
            char[] charArray = dataCatalogInfo.getScale().toCharArray();
            char[] newVerArray = Arrays.copyOfRange(charArray, 0, charArray.length - 1);
//            missionDetailVO.setVersion(Integer.parseInt(newVerArray.toString()));
            missionDetailVO.setDatacatalogId(dataCatalogInfo.getAssetId());
            missionDetailVOs.add(missionDetailVO);
            log.info("dataCatalogInfo: " + dataCatalogInfo);
            DataInfo dataInfo = dataCatalogInfo.getDataInfo();
            for (SaveTableColumnItem dataCatalogDetailInfo : dataInfo.getItemList()) {
                columnInfoMap.put(dataCatalogInfo.getAssetEnName().toUpperCase()+"."+dataCatalogDetailInfo.getName().toUpperCase(), String.valueOf(dataCatalogDetailInfo.getDataType()));
                AssetDetail assetDetail = new AssetDetail();
                assetDetail.setDBName(dataInfo.getDbName());
                assetDetail.setAssetId(dataCatalogInfo.getAssetId());
                assetDetail.setAssetName(dataCatalogInfo.getAssetEnName());
                assetDetail.setType(dataCatalogDetailInfo.getDataType());
                assetDetail.setLength(dataCatalogDetailInfo.getDataLength());
                assetDetail.setTableName(dataInfo.getTableName());
//                assetDetail.setOrgId();
                assetDetail.setColumnName(dataCatalogDetailInfo.getName());
                assetDetail.setComments(dataCatalogDetailInfo.getDescription());
                assetDetail.setOrgName(dataCatalogInfo.getHolderCompany());
                assetDetail.setHolderCompany(dataCatalogInfo.getHolderCompany());
                assetDetailList.add(assetDetail);
            }
        }
        tableOwnerMap.put(TEE_PARTY_KEY, TEE_PARTY);
        return tableOwnerMap;
    }

    public DAG<PhysicalPlan> parser() {
        LogicalPlanBuilderV2 logicalPlanBuilder = new LogicalPlanBuilderV2(this.sql);
        XPCPlan logicalPlan = logicalPlanBuilder.getLogicalPlan();
        LogicalPlanPrinter printer = new LogicalPlanPrinter();
        printer.visitTree(logicalPlan, 0);
        System.out.println(printer.logicalPlanString);
        HashMap<String, String> tableOwnerMap = buildMetaData(assetInfoList);

        List<String> columnList = logicalPlanBuilder.getColumnList();

        // 获取所需的元数据，即HashMap<String, TableInfo>
        HashMap<String, TableInfo> metadata = new HashMap<>();
        for (AssetInfo assetInfo : assetInfoList) {
            HashMap<String, FieldInfo> fields = new HashMap<>();
            DataInfo dataInfo = assetInfo.getDataInfo();
            String tableName = dataInfo.getTableName();
            String databaseName = dataInfo.getDbName();
            String assetName = assetInfo.getAssetEnName();
            String[] spits = assetName.split("_");
            String domainId = spits[spits.length - 1];
            for (SaveTableColumnItem detailInfo : dataInfo.getItemList()) {
                if (!columnList.contains(assetName + "." + detailInfo.getName())) {
                    continue;
                }
                FieldInfo field = new FieldInfo(detailInfo.getName(), detailInfo.getDataType(), null, null, FieldInfo.DistributionType.Uniform,
                        assetName, detailInfo.getDataLength(), domainId, assetInfo.getHolderCompany(), detailInfo.getDescription(), databaseName, assetName);
                fields.put(field.getUniqueName(), field);
            }
            // 后续需要添加rowCount的获取
            int rowCount = 100;
            TableInfo tableInfo = new TableInfo(fields, rowCount, tableName, assetInfo.getHolderCompany(), domainId, assetName);
            metadata.put(assetInfo.getAssetEnName(), tableInfo);
        }

        PlanOptimizer optimizer = new PlanOptimizer(this.modelType, this.isStream, tableOwnerMap, metadata);
        optimizer.visit(logicalPlan);
        DAG<PhysicalPlan> planDag = optimizer.getDag();
        return planDag;
    }

    /**
     * 带查询优化的parser
     * @return
     */
    public ParserWithOptimizerReturnValue parserWithOptimizer() throws Exception {
        LogicalPlanBuilderV2 logicalPlanBuilder = new LogicalPlanBuilderV2(this.sql);
        XPCPlan logicalPlan = logicalPlanBuilder.getLogicalPlan();

        LogicalPlanPrinter printer = new LogicalPlanPrinter();
        printer.visitTree(logicalPlan, 0);
        log.info(String.valueOf(printer.logicalPlanString));

        List<String> columnList = logicalPlanBuilder.getColumnList();
        HashMap<String, TableInfo> metadata = getMetadata(columnList);
        //检查sql是否可执行？
        // 之前的sqlparser约用时1500ms
        // 接入Calcite    3000ms
        LogicPlanAdapter planAdapter = new LogicPlanAdapter(this.sql.toUpperCase(), logicalPlan, metadata, modelType);
        try {
            planAdapter.CastToRelNode(); //核心步骤，将LogicalPlan转换为Calcite的RelNode
        }catch (Exception e){
            log.error(e.getMessage(), e);
//            System.exit(-1);
        }

        // 查询优化部分   300ms
        RelNode root = planAdapter.getRoot();
        log.info("calcite reltree: " + RelOptUtil.toString(root, SqlExplainLevel.ALL_ATTRIBUTES));

        OptimizerPlanner planner = new OptimizerPlanner(root, true);
        RelNode phyPlan = planner.optimize();

        return new ParserWithOptimizerReturnValue(phyPlan, logicalPlan);
    }

    private HashMap<String, TableInfo> getMetadata(List<String> columnList) {
        HashMap<String, TableInfo> metadata = new HashMap<>();
        // 获取所需的元数据，即HashMap<String, TableInfo>
        for (AssetInfo assetInfo : assetInfoList) {
            HashMap<String, FieldInfo> fields = new HashMap<>();
            DataInfo dataInfo = assetInfo.getDataInfo();
            String tableName = dataInfo.getTableName();
            String databaseName = dataInfo.getDbName();
            String assetName = assetInfo.getAssetEnName();
            String[] splits = assetName.split("_");
            String domainId = splits[splits.length -1];
            for (SaveTableColumnItem detailInfo : dataInfo.getItemList()) {
                if  (!columnList.contains(assetName + "." + detailInfo.getName())) {
                    continue;
                }
                String dataType = detailInfo.getDataType();
                SqlTypeName sqlDataType = SqlTypeName.get(dataType.toUpperCase());
                if(sqlDataType == null){
                    //todo
                    //throw new Exception("dataType: " + dataType + " is not supported");
                }
                FieldInfo field = new FieldInfo(detailInfo.getName(), dataType, null, null, FieldInfo.DistributionType.Uniform,
                        assetName, detailInfo.getDataLength(), domainId, assetInfo.getHolderCompany(), detailInfo.getDescription(), databaseName, assetName);
                fields.put(field.getUniqueName(), field);
            }
            // 后续需要添加rowCount的获取
            int rowCount = 100;
            TableInfo tableInfo = new TableInfo(fields, rowCount, tableName, assetInfo.getHolderCompany(), domainId, assetName);
            metadata.put(assetInfo.getAssetEnName(), tableInfo);
        }
        return metadata;
    }
}
