package com.chainmaker.jobservice.core.optimizer;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chainmaker.jobservice.api.response.ParserException;
import com.chainmaker.jobservice.core.calcite.optimizer.metadata.TableInfo;
import com.chainmaker.jobservice.core.optimizer.model.InputData;
import com.chainmaker.jobservice.core.optimizer.model.OutputData;
import com.chainmaker.jobservice.core.optimizer.model.SpdzInputData;
import com.chainmaker.jobservice.core.optimizer.model.TeeModel;
import com.chainmaker.jobservice.core.optimizer.nodes.DAG;
import com.chainmaker.jobservice.core.optimizer.nodes.Node;
import com.chainmaker.jobservice.core.optimizer.plans.*;
import com.chainmaker.jobservice.core.parser.plans.*;
import com.chainmaker.jobservice.core.parser.tree.*;

import java.util.*;

/**
 * @author gaokang
 * @date 2022-08-20 01:42
 * @description:对逻辑计划进行简单规则优化
 * @version: 1.0.0
 */

public class PlanOptimizer extends LogicalPlanVisitor {
    private Integer modelType, isStream;
    public DAG<PhysicalPlan> dag = new DAG<>();
    private HashMap<String, String> tableOwnerMap;
    private HashMap<String, PhysicalPlan> tableLastMap = new HashMap<>();
    private Integer count = 0;
    private HashMap<String, TableInfo> metaData;

    private HashMap<String, List<String>> kvMap = new HashMap<>();

    public PlanOptimizer(Integer modelType, Integer isStream, HashMap<String, String> tableOwnerMap, HashMap<String, TableInfo> metaData) {
        this.modelType = modelType;
        this.isStream = isStream;
        this.tableOwnerMap = tableOwnerMap;
        this.metaData = metaData;
    }

    public DAG<PhysicalPlan> getDag() {
        dag.update();
        for (Node<PhysicalPlan> node : dag.getLeaves()) {
            node.getObject().setFinalResult(true);
        }
        return dag;
    }
    public void visit(XPCPlan plan) {
        plan.accept(this);
    }

    public void visit(FederatedLearning node) {
        for (XPCPlan child: node.getChildren()) {
            child.accept(this);
        }
        FederatedLearningExpression expression = node.getExprList();
//        buildFate(expression);
    }
    public void visit(XPCProject node) {

        List<NamedExpression> preProject = node.getProjectList().getValues();
        for (int i=0; i<preProject.size(); i++) {
            Expression expression = preProject.get(i).getExpression();
            if (expression instanceof DereferenceExpression) {
                if (kvMap.containsKey(((DereferenceExpression) expression).getBase().toString())) {
                    kvMap.get(((DereferenceExpression) expression).getBase().toString()).add(((DereferenceExpression) expression).getFieldName());
                } else {
                    List<String> kvList = new ArrayList<>();
                    kvList.add(((DereferenceExpression) expression).getFieldName());
                    kvMap.put(((DereferenceExpression) expression).getBase().toString(), kvList);
                }

            }
        }
        for (XPCPlan child: node.getChildren()) {
            child.accept(this);
        }
        List<NamedExpression> namedExpressionList = node.getProjectList().getValues();

        for (int i=0; i<namedExpressionList.size(); i++) {
            NamedExpression namedExpression = namedExpressionList.get(i);
            Expression expr = namedExpression.getExpression();
            String alias = namedExpression.getIdentifier() == null? null: namedExpression.getIdentifier().toString();
            if (expr instanceof DereferenceExpression) {
                buildProject((DereferenceExpression) expr, alias);
            } else if(expr instanceof ArithmeticBinaryExpression) {
                buildMpc((ArithmeticBinaryExpression) expr, null, alias);
            } else if (expr instanceof FunctionCallExpression) {
                if (modelType == 0 && isStream == 0){
                    List<Expression> expressions = ((FunctionCallExpression) expr).getExpressions();
                    String function = ((FunctionCallExpression) expr).getFunction();
                    for (Expression expression: expressions) {
                        if (expression instanceof ArithmeticBinaryExpression) {
                            buildMpc((ArithmeticBinaryExpression) expression, function, alias);
                        } else if (expression instanceof Identifier) {
                            System.out.println("*");
                        } else {
                            throw new ParserException("暂不支持");
                        }
                    }
                } else if (modelType == 2) {
                    buildTee((FunctionCallExpression) expr, alias);
                } else {
                    throw new ParserException("todo");
                }
            } else if (expr instanceof Identifier) {
                System.out.println("*");
            } else {
                throw new ParserException("暂不支持");
            }
        }
    }


    /***
     * @description 处理数据源节点，生成执行计划的reader
     * @param node
     * @return void
     * @author gaokang
     * @date 2022/8/24 10:29
     */
    public void visit(XPCTable node) {
        TableScan tableScan = new TableScan();
//        tableScan.setId(count);
//        count += 1;
        String tableName = node.getTableName();
        tableScan.setTableName(tableName);
        TableInfo tableInfo = metaData.get(tableName);
        if (tableOwnerMap.containsKey(tableName)) {
            tableScan.setDomainID(tableOwnerMap.get(tableName));
            tableScan.setDomainName(tableInfo.getOrgName());
        } else {
            throw new ParserException("验证失败");
        }
        OutputData outputData = new OutputData();
        outputData.setTableName(node.getTableName());
        outputData.setDomainID(tableOwnerMap.get(node.getTableName()));
        outputData.setDomainName(tableInfo.getOrgName());
        outputData.setOutputSymbol(node.getAlias());
        List<OutputData> outputDataList = new ArrayList<>();
        outputDataList.add(outputData);
        tableScan.setOutputDataList(outputDataList);
        dag.createNode(tableScan);
        tableLastMap.put(tableScan.getTableName(), tableScan);
    }

    /***
     * @description 处理join算子
     * @param node
     * @return void
     * @author gaokang
     * @date 2022/8/24 10:30
     */
    public void visit(XPCJoin node) {
        for (XPCPlan child: node.getChildren()) {
            child.accept(this);
        }
//         System.out.println("JOIN");

    }

    public void visit(XPCSubQuery node) {
        TableScan tableScan = new TableScan();
//        tableScan.setId(count);
//        count += 1;
        String tableName = node.getAlias();
        tableScan.setTableName(tableName);
        TableInfo tableInfo = metaData.get(tableName);
        if (tableOwnerMap.containsKey(tableName)) {
            tableScan.setDomainID(tableOwnerMap.get(tableName));
            tableScan.setDomainName(tableInfo.getOrgName());
        } else {
            throw new ParserException("验证失败");
        }
        OutputData outputData = new OutputData();
//        outputData.setTableName(node.getTableName());
//        outputData.setDomainID(tableOwnerMap.get(node.getTableName()));
        outputData.setDomainName(tableInfo.getOrgName());
        outputData.setOutputSymbol(node.getAlias());
        List<OutputData> outputDataList = new ArrayList<>();
        outputDataList.add(outputData);
        tableScan.setOutputDataList(outputDataList);
        dag.createNode(tableScan);
        tableLastMap.put(tableScan.getTableName(), tableScan);

    }

    public void visit(XPCFilter node) {
        for (XPCPlan child: node.getChildren()) {
            child.accept(this);
        }
        System.out.println("Filter");
        // 目前只处理pir
        PirFilter pirFilter = new PirFilter();
        pirFilter.setId(count);
        if (!kvMap.isEmpty()) {
            pirFilter.setProject(kvMap);
        }
        count += 1;

        HashSet<String> parties = new HashSet<>();
        List<InputData> inputDataList = new ArrayList<>();
        List<OutputData> outputDataList = new ArrayList<>();
        if (node.getCondition() instanceof ComparisonExpression) {
//            if (((ComparisonExpression) node.getCondition()).getRight().toString().equals("?")) {
            dealPir(((ComparisonExpression) node.getCondition()).getLeft(), inputDataList, outputDataList, parties);
            pirFilter.setInputDataList(inputDataList);
            pirFilter.setOutputDataList(outputDataList);
            pirFilter.setCondition(node.getCondition());
            pirFilter.setParties(new ArrayList<>(parties));
//            }

        }
        for (InputData inputData : pirFilter.getInputDataList()) {
            PhysicalPlan parent = tableLastMap.get(inputData.getAssetName());
            dag.addEdge(parent, pirFilter);
        }

    }
    private void dealPir(Expression condition, List<InputData> inputDataList, List<OutputData> outputDataList, HashSet<String> parties) {
        if (condition instanceof ComparisonExpression) {
            dealPir(((ComparisonExpression) condition).getLeft(), inputDataList, outputDataList, parties);
            dealPir(((ComparisonExpression) condition).getRight(), inputDataList, outputDataList, parties);
        } else if (condition instanceof DereferenceExpression) {
            DereferenceExpression expression = (DereferenceExpression) condition;
            PhysicalPlan parent = tableLastMap.get(expression.getBase().toString());
            InputData inputData = new InputData();
            OutputData outputData = new OutputData();
            inputData.setNodeSrc(parent.getId());
            String assetName = expression.getBase().toString();
            TableInfo tableInfo = this.metaData.get(assetName);
            inputData.setTableName(tableInfo.getName());
            inputData.setAssetName(assetName);
            inputData.setColumn(expression.getFieldName());
            inputData.setDomainID(tableOwnerMap.get(expression.getBase().toString()));
            inputData.setDomainName(tableInfo.getOrgName());
            parties.add(inputData.getDomainID());
            outputData.setTableName(expression.getBase().toString());
            outputData.setDomainID(tableOwnerMap.get(expression.getBase().toString()));
            outputData.setDomainName(tableInfo.getOrgName());
            outputData.setOutputSymbol(expression.getBase().toString() + "." + expression.getFieldName());
            inputDataList.add(inputData);
            outputDataList.add(outputData);
        } else {
            throw new ParserException("暂不支持");
        }
    }

//    private void buildFate(FederatedLearningExpression expression) {
//        Fate fate = new Fate();
//        fate.setId(count);
//        count += 1;
//        HashSet<String> parties = new HashSet<>();
//
//        JSONObject fl = listFlExpressionHandler(expression.getFl());
//        JSONObject eval = listFlExpressionHandler(expression.getEval());
//        JSONObject model = listFlExpressionHandler(expression.getModel());
//        JSONObject intersection = listFlExpressionHandler(expression.getPsi());
//        FateModel fateModel = new FateModel();
//        fateModel.setFl(fl);
//        fateModel.setModel(model);
//        fateModel.setIntersection(intersection);
//        fateModel.setEval(eval);
//        fate.setFateModel(fateModel);
//
//        String outputDomainID = "";
//        List<PhysicalPlan> parents = new ArrayList<>();
//        List<InputData> flInputDataList = new ArrayList<>();
//        for (int i=0; i<expression.getLabels().size(); i++) {
//            JSONObject label = listFlExpressionHandler(expression.getLabels().get(i));
//            FlInputData flInputData = new FlInputData();
//            flInputData.setTableName(label.getString("SOURCE_DATA"));
//            flInputData.setLabel_type(label.getString("LABEL_TYPE"));
//            flInputData.setWith_label(label.getString("WITH_LABEL"));
//            flInputData.setOutput_format(label.getString("OUTPUT_FORMAT"));
//            flInputData.setNamespace(label.getString("NAMESPACE"));
//
//            PhysicalPlan parent = tableLastMap.get(flInputData.getTableName());
//            parents.add(parent);
//            String tableName = flInputData.getTableName();
//            flInputData.setDomainID(tableOwnerMap.get(tableName));
//            TableInfo tableInfo = metaData.get(tableName);
//            flInputData.setDomainName(tableInfo.getOrgName());
//            parties.add(flInputData.getDomainID());
//            flInputData.setNodeSrc(parent.getId());
//            if (flInputData.getWith_label().equals("FALSE")) {
//                flInputData.setRole("HOST");
//                outputDomainID = flInputData.getDomainID();
//            } else {
//                flInputData.setRole("GUEST");
//            }
//            flInputDataList.add(flInputData);
//        }
//        fate.setInputDataList(flInputDataList);
//
//        List<OutputData> outputDataList = new ArrayList<>();
//        OutputData outputData = new OutputData();
//
//        outputData.setTableName("FL");
//        outputData.setOutputSymbol("FL");
//        outputData.setDomainID(outputDomainID);
//        outputDataList.add(outputData);
//        fate.setOutputDataList(outputDataList);
//        fate.setParties(new ArrayList<>(parties));
//        for (PhysicalPlan plan: parents) {
//            dag.addEdge(plan, fate);
//        }
//
//    }


    private void buildProject(DereferenceExpression expression, String alias) {
        HashSet<String> parties = new HashSet<>();
        Project project = new Project();
        project.setId(count);
        count += 1;

        PhysicalPlan parent = tableLastMap.get(expression.getBase().toString());

        List<InputData> inputDataList = new ArrayList<>();
        List<OutputData> outputDataList = new ArrayList<>();
        InputData inputData = new InputData();
        OutputData outputData = new OutputData();
        inputData.setNodeSrc(parent.getId());
        String assetName = expression.getBase().toString();
        TableInfo tableInfo = metaData.get(assetName);
        inputData.setTableName(tableInfo.getName());
        inputData.setAssetName(assetName);
        inputData.setColumn(expression.getFieldName());
        inputData.setDomainID(tableInfo.getOrgDId());
        inputData.setDomainName(tableInfo.getOrgName());
        parties.add(inputData.getDomainID());
        outputData.setTableName(expression.getBase().toString());
        outputData.setDomainID(tableOwnerMap.get(expression.getBase().toString()));
        outputData.setDomainName(tableInfo.getOrgName());
        if (Objects.equals(alias, "")) {
            outputData.setOutputSymbol(expression.getBase().toString() + "." + expression.getFieldName());
        } else {
            outputData.setOutputSymbol(alias);
        }
        inputDataList.add(inputData);
        outputDataList.add(outputData);
        project.setInputDataList(inputDataList);
        project.setOutputDataList(outputDataList);
        project.setParties(new ArrayList<>(parties));
        dag.addEdge(parent, project);
    }
    /***
     * @description 解析自定义函数，生成TEE计算节点
     * @param expression
     * @return void
     * @author gaokang
     * @date 2022/8/21 21:30
     */
    private void buildTee(FunctionCallExpression expression, String alias) {
        String teePartyKey = "TEE-PARTY";
        HashSet<String> parties = new HashSet<>();
        TeeModel teeModel = new TeeModel();
        teeModel.setMethodName(expression.getFunction());
        teeModel.setDomainID(tableOwnerMap.get(teePartyKey));
        parties.add(teeModel.getDomainID());

        List<InputData> inputDataList = new ArrayList<>();
        List<Expression> expressions = expression.getExpressions();
        List<PhysicalPlan> parents = new ArrayList<>();
        for (Expression value: expressions) {
            if (value instanceof DereferenceExpression) {
                PhysicalPlan parent = tableLastMap.get(((DereferenceExpression) value).getBase().toString());
                parents.add(parent);
                InputData inputData = new InputData();
                String assetName = ((DereferenceExpression) value).getBase().toString();
                TableInfo tableInfo = metaData.get(assetName);
                inputData.setTableName(tableInfo.getName());
                inputData.setAssetName(assetName);
                inputData.setColumn(((DereferenceExpression) value).getFieldName());
                inputData.setDomainID(tableOwnerMap.get(((DereferenceExpression) value).getBase().toString()));
                inputData.setNodeSrc(parent.getId());
                parties.add(inputData.getDomainID());
                inputDataList.add(inputData);
            } else {
                throw new ParserException("TEE语法错误");
            }
        }
        List<OutputData> outputDataList = new ArrayList<>();
        OutputData outputData = new OutputData();
        if (Objects.equals(alias, "")) {
            alias = expression.getFunction();
        }
        outputData.setTableName(alias);
        outputData.setOutputSymbol(alias);
        outputData.setDomainID(teeModel.getDomainID());
        outputDataList.add(outputData);

        TeeMpc teeMpc = new TeeMpc();
        teeMpc.setId(count);
        count += 1;
        teeMpc.setTeeModel(teeModel);
        teeMpc.setInputDataList(inputDataList);
        teeMpc.setOutputDataList(outputDataList);
        teeMpc.setParties(new ArrayList<>(parties));
        for (PhysicalPlan plan: parents) {
            dag.addEdge(plan, teeMpc);
        }
    }

    /***
     * @description 解析算术表达式，生成MPC计算节点
     * @param expression
     * @param function
     * @return void
     * @author gaokang
     * @date 2022/8/21 21:29
     */
    private void buildMpc(ArithmeticBinaryExpression expression, String function, String alias) {
        HashSet<String> parties = new HashSet<>();
        List<String> arithmeticList = new ArrayList<>();
        HashMap<String, HashMap<String, List<Integer>>> arithmeticIndexMap = new HashMap<>();
        Integer xCount = 0;
        arithmeticBinaryExpressionHandler(expression, arithmeticList, arithmeticIndexMap, xCount);

        List<InputData> inputDataList = new ArrayList<>();
        List<PhysicalPlan> parents = new ArrayList<>();
        for (String tableName: arithmeticIndexMap.keySet()) {
            PhysicalPlan parent = tableLastMap.get(tableName);
            parents.add(parent);
            for (String column: arithmeticIndexMap.get(tableName).keySet()) {
                SpdzInputData inputData = new SpdzInputData();
                inputData.setNodeSrc(parent.getId());
                inputData.setTableName(tableName);
                inputData.setColumn(column);
                inputData.setIndex(arithmeticIndexMap.get(tableName).get(column));
                inputData.setDomainID(tableOwnerMap.get(tableName));
                TableInfo tableInfo = metaData.get(tableName);
                inputData.setAssetName(tableInfo.getAssetName());
                inputData.setDomainName(tableInfo.getOrgName());
                parties.add(inputData.getDomainID());
                inputDataList.add(inputData);
            }
        }
        List<OutputData> outputDataList = new ArrayList<>();
        OutputData outputData = new OutputData();
        outputData.setTableName(alias);
        outputData.setOutputSymbol(alias);
        outputData.setDomainID(inputDataList.get(0).getDomainID());
        outputData.setDomainName(inputDataList.get(0).getDomainName());
        outputDataList.add(outputData);
        SpdzMpc spdzMpc = new SpdzMpc();
        spdzMpc.setId(count);
        count += 1;
        spdzMpc.setExpression(arithmeticList);
        spdzMpc.setInputDataList(inputDataList);
        spdzMpc.setOutputDataList(outputDataList);
        spdzMpc.setParties(new ArrayList<>(parties));
        if (function != null) {
            spdzMpc.setAggregateType(function);
        }
        for (PhysicalPlan plan: parents) {
            dag.addEdge(plan, spdzMpc);
        }
    }


    /***
     * @description 递归处理算术表达式
     * @param expression
     * @param arithmeticList
     * @param arithmeticIndexMap
     * @param xCount
     * @return void
     * @author gaokang
     * @date 2022/8/21 21:29
     */
    private void arithmeticBinaryExpressionHandler(Expression expression, List<String> arithmeticList, HashMap<String, HashMap<String, List<Integer>>> arithmeticIndexMap, Integer xCount) {
        if (expression instanceof DereferenceExpression) {
            String x = "x";
            String tableName = ((DereferenceExpression) expression).getBase().toString();
            String column = ((DereferenceExpression) expression).getFieldName();
            arithmeticList.add(x);
            if (arithmeticIndexMap.containsKey(tableName)) {
                if (arithmeticIndexMap.get(tableName).containsKey(column)) {
                    arithmeticIndexMap.get(tableName).get(column).add(xCount);
                } else {
                    List<Integer> indexList = new ArrayList<>();
                    indexList.add(xCount);
                    arithmeticIndexMap.get(tableName).put(column, indexList);
                }
            } else {
                List<Integer> indexList = new ArrayList<>();
                indexList.add(xCount);
                HashMap<String, List<Integer>> indexMap = new HashMap<>();
                indexMap.put(column, indexList);
                arithmeticIndexMap.put(tableName, indexMap);
            }
        } else if (expression instanceof ArithmeticBinaryExpression) {
            arithmeticBinaryExpressionHandler(((ArithmeticBinaryExpression) expression).getLeft(), arithmeticList, arithmeticIndexMap, xCount);
            xCount += 1;
            arithmeticList.add(((ArithmeticBinaryExpression) expression).getOperator().getValue());
            arithmeticBinaryExpressionHandler(((ArithmeticBinaryExpression) expression).getRight(), arithmeticList, arithmeticIndexMap, xCount);
        } else if (expression instanceof ParenthesizedExpression) {
            arithmeticList.add(((ParenthesizedExpression) expression).getLeft());
            arithmeticBinaryExpressionHandler(((ParenthesizedExpression) expression).getExpression(), arithmeticList, arithmeticIndexMap, xCount);
            arithmeticList.add(((ParenthesizedExpression) expression).getRight());
        }
    }

    private JSONObject listFlExpressionHandler(List<FlExpression> flExpressions) {
        JSONObject params = new JSONObject();
        for (FlExpression flExpression : flExpressions) {
            flToJson(flExpression, params);
        }
        return params;
    }

    private void flToJson(FlExpression flExpression, JSONObject params) {
        if (flExpression.getLeft() instanceof Identifier) {
            params.put(flExpression.getLeft().toString(), flExpression.getRight().toString());
        } else if (flExpression.getLeft() instanceof DereferenceExpression) {
            List<String> keys = new ArrayList<>();
            getBase((DereferenceExpression) flExpression.getLeft(), keys);

            String paramsStr = "";
            for (int i=0; i<keys.size(); i++) {
                JSONObject temp = new JSONObject();
                if (i == 0) {
                    temp.put(keys.get(0), flExpression.getRight().toString());
                    paramsStr = temp.toJSONString();
                } else if (i == keys.size()-1) {
                    params.put(keys.get(i), JSONObject.parseObject(paramsStr, Feature.OrderedField));
                } else {
                    temp.put(keys.get(i), JSONObject.parseObject(paramsStr, Feature.OrderedField));
                    paramsStr = temp.toJSONString();
                }
            }
        }
    }
    private void getBase(DereferenceExpression expression, List<String> keys) {
        keys.add(expression.getFieldName());
        if (expression.getBase() instanceof Identifier) {
            keys.add(expression.getBase().toString());
        } else if (expression.getBase() instanceof DereferenceExpression) {
            getBase((DereferenceExpression) expression.getBase(), keys);
        } else {
            throw new ParserException("SQL参数格式错误");
        }
    }
}
