package com.chainmaker.jobservice.api.builder;

import cn.hutool.core.util.StrUtil;
import com.chainmaker.jobservice.api.model.job.task.*;
import com.chainmaker.jobservice.api.model.job.task.Module;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SetClauseParser {

    public static final String LOCAL_NOISE_OPERATOR = "local.noise.process";


    public static Pair parseClauseToPair2(String clause) {
        clause = clause.toLowerCase().strip();
        if(clause.startsWith("set") && clause.contains("=")){
            List<String> eles = StrUtil.split(clause, "=");
            return new Pair(eles.get(0), eles.get(1));
        } else {
            throw new IllegalArgumentException("Invalid clause format: " + clause);
        }
    }

    public static Pair parseClauseToPair(String clause) {
        // 定义正则表达式
        clause = clause.strip();
        Pattern pattern = Pattern.compile("set\\s+([\\w.]+)\\s+=\\s+(.+)");
        Matcher matcher = pattern.matcher(clause);

        if (matcher.matches()) {
            String key = matcher.group(1); // 匹配的键
            String value = matcher.group(2); // 匹配的值
            return new Pair(key, value);
        } else {
            throw new IllegalArgumentException("Invalid clause format: " + clause);
        }
    }

    public static List<Pair> clauses2Pairs(List<String> clauses) {
        return clauses.stream()
                .map(SetClauseParser::parseClauseToPair)
                .collect(Collectors.toList());
    }


    public static void main(String[] args) {
//        String example = "set local.noise.process.key1 = value1; set local.noise.process.key2 = value2; set local.noise.process.key3 = value3;";
        String example = "set table.field1.noise = {\"algo\": \"\", \"epsilon\": \"\", \"sensitivity\": \"\", \"delta\": \"\"};set local.noise.process.key2 = value2; set t1 = ?;";
        List<Pair> pairs = clauses2Pairs(Lists.newArrayList(example.split(";")));
        System.out.println(pairs);
    }
}
