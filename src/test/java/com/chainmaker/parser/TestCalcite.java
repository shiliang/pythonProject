package com.chainmaker.parser;

import com.alibaba.fastjson.JSON;
import com.chainmaker.jobservice.api.model.AssetInfo;
import com.chainmaker.jobservice.api.model.DataInfo;
import com.chainmaker.jobservice.api.model.SaveTableColumnItem;
import com.chainmaker.jobservice.core.calcite.cost.MPCCost;
import com.chainmaker.jobservice.core.calcite.cost.MPCRelMetaDataProvider;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.FieldInfo;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.MPCMetadata;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.TableInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.DataContext;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class TestCalcite {
    public static void main(String[] args) {
        String sql = "select whh_enterprise_3.balance, tmp_table.socialid from whh_enterprise_3, whh_security_1,(select '1' as socialid, cnt, tot_val from (select count(*) as cnt, sum(balance) as tot_val from whh_security_1 ) tmp_inner ) tmp_table where whh_enterprise_3.socialid= whh_security_1.socialid and tmp_table.socialid= whh_security_1.socialid";

        String columnStr = "[\"tmp_table.socialid\",\"whh_enterprise_3.balance\",\"whh_enterprise_3.socialid\",\"whh_security_1.socialid\"]";
        List<String> columnList = JSON.parseArray(columnStr, String.class);

        String assetInfoStr = "[{\"assetEnName\":\"whh_security_1\",\"assetId\":\"37\",\"assetName\":\"whh_security\",\"assetNumber\":\"120240311000014376\",\"assetType\":1,\"cycle\":\"1分\",\"dataInfo\":{\"dbName\":\"asset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"description\":\"\",\"isPrimaryKey\":1,\"name\":\"id\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"description\":\"名称\",\"isPrimaryKey\":0,\"name\":\"name\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"description\":\"地址\",\"isPrimaryKey\":0,\"name\":\"adress\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"name\":\"phone\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"description\":\"用途\",\"isPrimaryKey\":0,\"name\":\"purpose\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"int\",\"description\":\"金额\",\"isPrimaryKey\":0,\"name\":\"balance\",\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"date\",\"description\":\"时间\",\"isPrimaryKey\":0,\"name\":\"time\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"description\":\"备注\",\"isPrimaryKey\":0,\"name\":\"remarks\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"description\":\"社会id\",\"isPrimaryKey\":0,\"name\":\"socialid\",\"privacyQuery\":1}],\"tableName\":\"whh_security\"},\"holderCompany\":\"ida2\",\"intro\":\"whh_security\",\"scale\":\"2000条\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\",\"txId\":\"17bbaf383ac09a28ca60731891c3be39ef04b3fc414648eeab71fb4e9d6bf080\",\"uploadedAt\":\"2024-03-11 18:31:56\"},{\"assetEnName\":\"whh_enterprise_3\",\"assetId\":\"36\",\"assetName\":\"whh_enterprise\",\"assetNumber\":\"320240311000015941\",\"assetType\":1,\"cycle\":\"1分\",\"dataInfo\":{\"dbName\":\"asset\",\"itemList\":[{\"dataLength\":0,\"dataType\":\"int\",\"description\":\"\",\"isPrimaryKey\":1,\"name\":\"id\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"description\":\"名称\",\"isPrimaryKey\":0,\"name\":\"name\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"description\":\"地址\",\"isPrimaryKey\":0,\"name\":\"adress\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"description\":\"联系方式\",\"isPrimaryKey\":0,\"name\":\"phone\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"description\":\"用途\",\"isPrimaryKey\":0,\"name\":\"purpose\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"int\",\"description\":\"金额\",\"isPrimaryKey\":0,\"name\":\"balance\",\"privacyQuery\":1},{\"dataLength\":0,\"dataType\":\"date\",\"description\":\"时间\",\"isPrimaryKey\":0,\"name\":\"time\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"description\":\"备注\",\"isPrimaryKey\":0,\"name\":\"remarks\",\"privacyQuery\":1},{\"dataLength\":255,\"dataType\":\"varchar\",\"description\":\"社会id\",\"isPrimaryKey\":0,\"name\":\"socialid\",\"privacyQuery\":1}],\"tableName\":\"whh_enterprise\"},\"holderCompany\":\"ida1\",\"intro\":\"whh_enterprise\",\"scale\":\"2000条\",\"timeSpan\":\"2024/03/01 ~ 2024/03/31\",\"txId\":\"17bbaf122cd2116cca2135fa7ee19dd565cb6f8aabe84a658c14997bde054372\",\"uploadedAt\":\"2024-03-11 18:29:37\"}]";
        List<AssetInfo> assetList = JSON.parseArray(assetInfoStr,AssetInfo.class);

        HashMap<String, TableInfo> metadata = getMetadata(assetList, columnList);
        MPCMetadata mpcMetadata = MPCMetadata.getInstance();

        mpcMetadata.init(sql, metadata);

        RelBuilder builder = generateBuilder(mpcMetadata);

        List<RexNode> projections = new ArrayList<>();
        List<String> projectionNames = new ArrayList<>();


        /**
         * 此处开始calcite的逻辑树生成
         */
        RexNode sumNode = builder.call(SqlStdOperatorTable.SUM, builder.literal("balance"));
        RexNode cntNode = builder.call(SqlStdOperatorTable.COUNT, builder.literal("*"));
        projections.addAll(Lists.newArrayList(sumNode, cntNode));
        projectionNames.addAll(Lists.newArrayList("wen3.total_val", "wen3.cnt"));
        RelNode ans0 =builder.scan("whh_enterprise_3").as("wen3").build();
        RelNode ans1 = builder.push(ans0).project(projections, projectionNames).build();

        List<String> fieldNames = ans1.getRowType().getFieldNames();
        int cnt = ans1.getRowType().getFieldCount();
        builder.push(ans1);
//        List<RexNode> newProjects = fieldNames.stream().map(x -> builder.alias(builder.field(x), "tmp_inner." + x)).collect(Collectors.toList());
        List<RexNode> newProjects = Lists.newArrayList();
        for(int i = 0; i < cnt; i++) {
            RexNode node = builder.getRexBuilder().makeInputRef(ans1, i);
            newProjects.add(node);
        }
        newProjects.clear();
        newProjects.add(builder.getRexBuilder().makeRangeReference(ans1));
        RelNode ans2 = builder.push(ans1).project(newProjects).as("test1").build();
        builder.build();

        RexNode constNode = builder.literal(1);
        RexNode cntNode2 = builder.literal("cnt");
        RexNode sumNode2 = builder.literal("total_val");
        projections.clear();projectionNames.clear();
        projections.addAll(Lists.newArrayList(constNode, cntNode2, sumNode2));
        projectionNames.addAll(Lists.newArrayList( "socialid","cnt","total_val"));
        RelNode ans3 = builder.push(ans2).project(projections, projectionNames).build();

        log.info("finish");
    }




    public static RelBuilder generateBuilder(MPCMetadata metadata) {
        SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();

        HashMap<String, HashMap<String, SqlTypeName>> tableFieldMap = metadata.getTableFieldMap();
        List<Object[]> _DATA = Arrays.asList();

        for (String key : tableFieldMap.keySet()) {
            RelDataTypeFactory.Builder builder = new RelDataTypeFactory.Builder(typeFactory);
            for (String fieldName : tableFieldMap.get(key).keySet()) {
                builder.add(fieldName, tableFieldMap.get(key).get(fieldName));
            }
            rootSchema.add(key, new ListTable(builder.build(), _DATA));
        }


        FrameworkConfig config = Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.Config.DEFAULT)
                .defaultSchema(rootSchema)
                .traitDefs((List<RelTraitDef>) null)
                .costFactory(MPCCost.FACTORY)           // 此处将cost计算方式换成自行实现的版本
                .build();

        RelBuilder builder = RelBuilder.create(config);
        builder.getCluster().setMetadataProvider(MPCRelMetaDataProvider.relMetaDataProvider);

        return builder;
    }



    public static HashMap<String, TableInfo> getMetadata(List<AssetInfo> assetInfoList ,List<String> columnList) {
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

    private static class ListTable extends AbstractTable implements ScannableTable {
        private RelDataType rowType;
        private List<Object[]> data;

        ListTable(RelDataType rowType, List<Object[]> data) {
            this.rowType = rowType;
            this.data = data;
        }

        @Override public Enumerable<Object[]> scan(final DataContext root) {
            return Linq4j.asEnumerable(data);
        }

        @Override public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
            return rowType;
        }

    }
}
