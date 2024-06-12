package com.chainmaker.jobservice.core.calcite.utils;

import ch.qos.logback.core.util.SystemInfo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chainmaker.jobservice.api.Constant;
import com.chainmaker.jobservice.core.calcite.relnode.MPCFilter;
import com.chainmaker.jobservice.core.calcite.relnode.MPCJoin;
import com.chainmaker.jobservice.core.calcite.relnode.MPCProject;
import com.chainmaker.jobservice.core.calcite.relnode.MPCTableScan;
import com.chainmaker.jobservice.core.optimizer.plans.TableScan;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.volcano.RelSubset;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.externalize.RelJsonWriter;
import org.apache.calcite.rel.externalize.RelWriterImpl;
import org.apache.calcite.rel.externalize.RelXmlWriter;
import org.apache.calcite.sql.SqlExplainFormat;
import org.apache.calcite.sql.SqlExplainLevel;
import org.chainmaker.pb.config.LocalConfig;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

/**
 * 优化后的物理执行计划的输出类
 * 主要负责按照要求json格式输出优化后的计划
 */
public class PhysicalPlanPrinter {
    RelNode phyPlan;

    int cnt = 0;    // 用于缩进

    public PhysicalPlanPrinter(RelNode phyPlan) {
        this.phyPlan = phyPlan;
    }

    /**
     * 递归输出relnode
     * @param rel
     * @return
     */
    public String Print(RelNode rel) {
        String s = "";
        for (int i = 0; i < cnt; i++) {
            s += " ";
        }
        if (rel instanceof RelSubset) {
            s += Print(((RelSubset) rel).getBest());
        }
        else if (rel instanceof MPCTableScan) {
            s += rel.getRelTypeName() + " [";
            s += rel.getRowType().getFieldNames().get(0).split("\\.")[0];
            s += "]\n";
        } else if (rel instanceof MPCFilter) {
            cnt++;
            s += rel.getRelTypeName() + " [";
            s += "FilterCond[" + ((MPCFilter) rel).getCondition().toString() + "] $ from ";
            s += rel.getRowType().getFieldNames().toString();
            s += "]\n";
            for (int i = 0; i < rel.getInputs().size(); i++) {
                s += Print(rel.getInput(i));
            }
            cnt--;
        } else if (rel instanceof MPCJoin) {
            cnt++;
            s += rel.getRelTypeName() + " [";
            s += "JoinType[" + ((MPCJoin) rel).getJoinType().toString() + "], ";
            s += "JoinCond[" + ((MPCJoin) rel).getCondition().toString() + "], ";
            s += "$ from " + rel.getRowType().getFieldNames();
            s += "]\n";
            s += Print(((MPCJoin) rel).getLeft());
            s += Print(((MPCJoin) rel).getRight());
            cnt--;
        } else if (rel instanceof MPCProject){
            cnt++;
            s += rel.getRelTypeName() + " [";
            int n = ((MPCProject) rel).getNamedProjects().size();
            for (int i = 0; i < n; i++) {
                s += ((MPCProject) rel).getNamedProjects().get(i).right;
                if (i < n-1) {
                    s += ", ";
                }
            }
            s += "]\n";
            for (int i = 0; i < rel.getInputs().size(); i++) {
                s += Print(rel.getInput(i));
            }
            cnt--;
        } else {
            ;
        }
        return s;
    }

    /**
     * 将物理计划转化成最终需要的格式
     */
    public List<JSONObject> PhyPlan2Task() {
//        System.out.println(
//                RelOptUtil.dumpPlan("[Physical plan] TEXT", phyPlan, SqlExplainFormat.TEXT,
//                        SqlExplainLevel.ALL_ATTRIBUTES));
        String str = Print(phyPlan);
        System.out.println(str);
        Stack<String> stk = new Stack<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                stk.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<JSONObject> tasks = generateTask(stk);
//        List<JSONObject> tasks = null;
        return tasks;
    }

    /**
     * 根据物理计划生成Task
     * @param stk
     * @return
     */
    public List<JSONObject> generateTask(Stack<String> stk) {
        List<JSONObject> tasks = new ArrayList<>();

        int cnt = 1;
        Stack<String> operands = new Stack<>();
        while (!stk.isEmpty()) {
            String s = stk.pop().trim();
//            System.out.println("stk string = " + s);
            List<String> params = List.of(s.split("\\[|]"));
            if (s.startsWith("MPCTableScan")) {
                operands.add(s);
            } else if (s.startsWith("MPCJoin")) {
                String left = operands.pop().trim();
                String right = operands.pop().trim();
                String leftDataName = null, rightDataName = null;
                if (!left.startsWith("MPC")) {
                    leftDataName = left.split(" ")[0];
                }
                if (!right.startsWith("MPC")) {
                    rightDataName = right.split(" ")[0];
                }
                JSONObject PSITask = str2Json(s, cnt, "PSI", leftDataName, rightDataName, stk.isEmpty());
                tasks.add(PSITask);
                String PSIOutput = PSITask.getJSONObject("value").getString("output");
                String dataName = PSITask.getJSONObject("value").getJSONObject("output").getJSONArray("data").getJSONObject(0).getString("dataName");
                operands.add(dataName + " " + PSIOutput);
                cnt++;
            } else if (s.startsWith("MPCFilter")) {
                if (params.get(2).startsWith("AND")) {
                    String[] conds = params.get(2).substring(4, params.get(2).length() - 1).split("\\),");
                    conds[0] += ")";
                    for (int i = 0; i < conds.length; i++) {
                        conds[i] = conds[i].trim();
                        String newFilter = s.substring(0, s.indexOf("AND"));
                        newFilter += conds[i] + "] ";
                        newFilter += s.substring(s.lastIndexOf("$"));
//                        System.out.println(newFilter);
                        stk.add(newFilter);
                    }
                } else {
                    String child = operands.pop().trim();
                    String dataName = null;
                    if (child.contains("-")) {
                        dataName = child.split(" ")[0];
                    }
                    JSONObject FilterTask;
                    String cond = params.get(2);
                    if (cond.substring(0, cond.indexOf(",")).contains("$") && cond.substring(cond.indexOf(",")).contains("$")) {
                        FilterTask = str2Json(s, cnt, "VariableFilter", dataName, null, stk.isEmpty());
                    } else {
                        FilterTask = str2Json(s, cnt, "ConstantFilter", dataName, null, stk.isEmpty());
                    }
                    tasks.add(FilterTask);
                    String FilterOutput = FilterTask.getJSONObject("value").getString("output");
                    dataName = FilterTask.getJSONObject("value").getJSONObject("output").getJSONArray("data").getJSONObject(0).getString("dataName");
                    operands.add(dataName + " " + FilterOutput);
                    cnt++;
                }
            } else if (s.startsWith("MPCProject")) {
                String tmp = s.substring(s.indexOf("[")+1, s.length()-1);
                if (tmp.contains(",")) {
                    String[] projs = tmp.split(",");
                    for (String proj : projs) {
                        proj = proj.trim();
                        String newProject = s.substring(0, s.indexOf("[")+1);
                        newProject += proj + "] ";
                        stk.add(newProject);
                    }
                } else {
                    String child = operands.pop().trim();
                    String dataName = null;
                    JSONObject ProjectTask;
                    if (child.contains("-")) {
                        dataName = child.split(" ")[0];
                    }
                    if (s.toUpperCase().contains("MAX") || s.toUpperCase().contains("MIN") || s.toUpperCase().contains("COUNT") ||
                            s.toUpperCase().contains("AVG") || s.toUpperCase().contains("SUM")) {
                        ProjectTask = str2Json(s, cnt, "MPC", dataName, null, stk.isEmpty());
                    } else {
                        ProjectTask = str2Json(s, cnt, "QUERY", dataName, null, stk.isEmpty());
                    }
                    tasks.add(ProjectTask);
                    String ProjectOutput = ProjectTask.getJSONObject("value").getString("output");
                    dataName = ProjectTask.getJSONObject("value").getJSONObject("output").getJSONArray("data").getJSONObject(0).getString("dataName");
                    operands.add(dataName + " " + ProjectOutput);
                    cnt++;
                }
            } else {
                ;
            }
        }

        return tasks;
    }

    /**
     * 将字符串转换成Task所需的Json格式
     * @param str
     * @return
     */
    public JSONObject str2Json(String str, int curTaskName, String moduleName, String leftDataName, String rightDataName, boolean isFinalResult) {
        JSONObject json = new JSONObject(true);
        JSONObject value = new JSONObject(true);
        List<String> params = List.of(str.split("\\[|]"));

        // 基本信息
        value.put("version", "");
        value.put("jobID", "");
        value.put("taskName", String.valueOf(curTaskName));
//        value.put("status", "WAITING");
        value.put("status", Constant.TASK_STATUS);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
        value.put("updateTime", dateFormat.format(date).toString());
        value.put("createTime", "");

        // module信息（即进行什么操作）
        JSONObject module = new JSONObject(true);
        module.put("moduleName", moduleName);
        JSONObject moduleparams = new JSONObject(true);
        switch (moduleName) {
            case "PSI": {
                String joinType = params.get(2);
                moduleparams.put("joinType", joinType);
                String joinOp = params.get(4).substring(0, 1);
                moduleparams.put("operator", joinOp);
                break;
            }
            case "ConstantFilter": {
                String cond = params.get(2);
                moduleparams.put("operator", cond.substring(0, cond.indexOf("(")));
                String[] sp = cond.split("\\(|,|\\)");
                for (String s : sp) {
                    s = s.trim();
                    if (s.charAt(0) >= '0' && s.charAt(0) <= '9') {
                        moduleparams.put("constant", s);
                        break;
                    }
                }
                break;
            }
            case "VariableFilter": {

                break;
            }
            case "MPC": {
                String MPCProj = str.substring(str.indexOf("[") + 1, str.length() - 1);
                String func = MPCProj.substring(0, MPCProj.indexOf("["));
                moduleparams.put("function", func);

                String MPCExpr = MPCProj.substring(MPCProj.indexOf("["), MPCProj.length()-1);
                String expression = "";
                int chNum = 0;
                for (int i = 0; i < MPCExpr.length(); i++) {
                    char ch = MPCExpr.charAt(i);
                    if (ch == '+' || ch == '-' || ch == '*' || ch == '/' ||
                            ch == '(' || ch == ')') {
                        if (chNum != 0) {
                            expression += "x";
                            chNum = 0;
                        }
                        expression += ch;
                    } else {
                        chNum++;
                    }
                }
                if (chNum != 0) {
                    expression += "x";
                }
                moduleparams.put("expression", expression);
                break;
            }
            case "QUERY": {
                // 不需要额外的params
//                String field = str.substring(str.indexOf("[") + 1, str.length() - 1);

                break;
            }
            default:
                break;
        }
        module.put("params", moduleparams);
        value.put("module", module);

        // 输入信息
        JSONObject input = new JSONObject(true);
        JSONArray inputData = new JSONArray();


        switch (moduleName) {
            case "PSI": {
                JSONObject inputdata1 = new JSONObject(true), inputdata2 = new JSONObject(true);
                JSONObject inputData1Params = new JSONObject(true);
                JSONObject inputData2Params = new JSONObject(true);
                // Join左侧
                String index = params.get(4).substring(params.get(4).indexOf("$") + 1, params.get(4).indexOf(","));
                String tableField = params.get(6).split(",")[Integer.parseInt(index)].trim();

                if (leftDataName != null) {
                    inputdata1.put("dataName", leftDataName);
                    inputdata1.put("taskSrc", leftDataName.split("-")[1]);
                } else {
                    inputdata1.put("dataName", tableField.split("\\.")[0]);
                    inputdata1.put("taskSrc", "0");
                }
                inputdata1.put("dataID", "");
                inputdata1.put("domainID", "");
                inputdata1.put("role", "");
                inputData1Params.put("table", tableField.split("\\.")[0]);
                inputData1Params.put("field", tableField.split("\\.")[1]);
                inputdata1.put("params", inputData1Params);

                // Join右侧
                index = params.get(4).substring(params.get(4).lastIndexOf("$") + 1, params.get(4).indexOf(")"));
                tableField = params.get(6).split(",")[Integer.parseInt(index)].trim();

                if (rightDataName != null) {
                    inputdata2.put("dataName", rightDataName);
                    inputdata2.put("taskSrc", rightDataName.split("-")[1]);
                } else {
                    inputdata2.put("dataName", tableField.split("\\.")[0]);
                    inputdata2.put("taskSrc", "0");
                }
                inputdata2.put("dataID", "");
                inputdata2.put("domainID", "");
                inputdata2.put("role", "");

                inputData2Params.put("table", tableField.split("\\.")[0]);
                inputData2Params.put("field", tableField.split("\\.")[1]);
                inputdata2.put("params", inputData2Params);

                inputData.add(inputdata1);
                inputData.add(inputdata2);
                break;
            }
            case "ConstantFilter": {
                String cond = params.get(2);
                String[] sp = cond.split("\\(|,|\\)");
                int index = 0;
                for (String s : sp) {
                    s = s.trim();
                    if (s.startsWith("$")) {
                        index = Integer.parseInt(s.substring(1));
                        break;
                    }
                }
                JSONObject jsonObject = new JSONObject(true);
                String tableField = params.get(4).split(",")[index].trim();
                if (leftDataName != null) {
                    jsonObject.put("dataName", leftDataName);
                    jsonObject.put("taskSrc", leftDataName.split("-")[1]);
                } else {
                    jsonObject.put("dataName", tableField.split("\\.")[0]);
                    jsonObject.put("taskSrc", 0);
                }
                jsonObject.put("dataID", "");
                jsonObject.put("domainID", "");
                jsonObject.put("role", "");
                JSONObject jsonObjectParams = new JSONObject(true);
                jsonObjectParams.put("table", tableField.split("\\.")[0]);
                jsonObjectParams.put("field", tableField.split("\\.")[1]);
                jsonObject.put("params", jsonObjectParams);
                inputData.add(jsonObject);
                break;
            }
            case "VariableFilter": {

                break;
            }
            case "MPC": {
                String MPCProj = str.substring(str.indexOf("[") + 1, str.length() - 1);
                String func = MPCProj.substring(0, MPCProj.indexOf("["));

                String[] fields = MPCProj.substring(MPCProj.indexOf("[")+1, MPCProj.length()-1).split("\\+|-|\\*|/|\\(|\\)");
                for (String field : fields) {
                    if (field.length() == 0) {
                        continue;
                    }
                    JSONObject jsonObject = new JSONObject(true);
                    if (leftDataName != null) {
                        jsonObject.put("dataName", leftDataName);
                        jsonObject.put("taskSrc", leftDataName.split("-")[1]);
                    } else {
                        jsonObject.put("dataName", field.split("\\.")[0]);
                        jsonObject.put("taskSrc", "0");
                    }
                    jsonObject.put("dataID", "");
                    jsonObject.put("domainID", "");
                    jsonObject.put("role", "");
                    JSONObject jsonObjectParams = new JSONObject(true);
                    jsonObjectParams.put("table", field.split("\\.")[0]);
                    jsonObjectParams.put("field", field.split("\\.")[1]);
                    jsonObject.put("params", jsonObjectParams);
                    inputData.add(jsonObject);
                }
                break;
            }
            case "QUERY": {
                String field = str.substring(str.indexOf("[") + 1, str.length() - 1);
                JSONObject jsonObject = new JSONObject(true);
                if (leftDataName != null) {
                    jsonObject.put("dataName", leftDataName);
                    jsonObject.put("taskSrc", leftDataName.split("-")[1]);
                } else {
                    jsonObject.put("dataName", field.split("\\.")[0]);
                    jsonObject.put("taskSrc", "0");
                }
                jsonObject.put("dataID", "");
                jsonObject.put("domainID", "");
                jsonObject.put("role", "");
                JSONObject jsonObjectParams = new JSONObject(true);
                jsonObjectParams.put("table", field.split("\\.")[0]);
                jsonObjectParams.put("field", field.split("\\.")[1]);
                jsonObject.put("params", jsonObjectParams);
                inputData.add(jsonObject);
                break;
            }
            default:
                break;
        }

        input.put("data", inputData);
        value.put("input", input);

        // 输出信息
        JSONObject output = new JSONObject(true);
        JSONArray outputData = new JSONArray();
        JSONObject outputdata1 = new JSONObject(true);
        JSONObject outputdata2 = new JSONObject(true);

        switch (moduleName) {
            case "PSI":
                outputdata1.put("dataName", moduleName+"-"+curTaskName);
                outputdata1.put("finalResult", "N");
                outputdata1.put("domainID", "");
                outputdata1.put("dataID", "");

                // PSI需要两个output的原因主要是保存到不同的提供者domainID中
                outputdata2.put("dataName", moduleName+"-"+curTaskName);
                outputdata2.put("finalResult", "N");
                outputdata2.put("domainID", "-");
                outputdata2.put("dataID", "");

                if (isFinalResult) {
                    outputdata1.put("finalResult", "Y");
                    outputdata2.put("finalResult", "Y");
                }

                outputData.add(outputdata1);
                outputData.add(outputdata2);
                break;
            case "ConstantFilter":
            case "VariableFilter":
                outputdata1.put("dataName", moduleName+"-"+curTaskName);
                outputdata1.put("finalResult", "N");
                outputdata1.put("domainID", "");
                outputdata1.put("dataID", "");
                if (isFinalResult) {
                    outputdata1.put("finalResult", "Y");
                }
                outputData.add(outputdata1);
                break;
            case "MPC":
            case "QUERY":
                outputdata1.put("dataName", moduleName+"-"+curTaskName);
                outputdata1.put("finalResult", "N");
                outputdata1.put("domainID", "");
                outputdata1.put("dataID", "");
                if (isFinalResult) {
                    outputdata1.put("finalResult", "Y");
                }
                outputData.add(outputdata1);
                break;
            default:
                break;
        }

        output.put("data", outputData);
        value.put("output", output);

        // parties信息
        JSONArray parties = new JSONArray();
        JSONObject party1 = new JSONObject(true);
        party1.put("partyID", "");      // input 中的 domainID
        party1.put("status", "");
        party1.put("timestamp", "");
        JSONObject serverInfo = new JSONObject(true);
        serverInfo.put("ip", "");
        serverInfo.put("port", "");
        party1.put("serverInfo", serverInfo);
        parties.add(party1);
        value.put("parties", parties);

        json.put("key", "");
        json.put("value", value);
        return json;
    }
}
