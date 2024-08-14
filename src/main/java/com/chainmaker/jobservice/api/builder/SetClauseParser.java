package com.chainmaker.jobservice.api.builder;

import com.chainmaker.jobservice.api.model.job.task.*;
import com.chainmaker.jobservice.api.model.job.task.Module;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SetClauseParser {

    public static final String LOCAL_NOISE_OPERATOR = "local.noise.process";

    @Data
    @AllArgsConstructor
    public static class Pair {
        private String key;
        private String value;
    }

    public static Pair parseClauseToPair(String clause) {
        // 定义正则表达式
        clause = clause.strip();
        Pattern pattern = Pattern.compile("set\\s+([\\w.]+)\\s+=\\s+([\\w.]+)");
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

    public static Task generateTaskFromSetClauses(List<String> setClauses) {
        List<Pair> pairs = clauses2Pairs(setClauses);
        Task task = new Task();
        Input input = new Input();
        List<InputDetail> inputDetailList = Lists.newArrayList();
        Module module = new Module();
        Output output = new Output();
        List<Output> ouputList = Lists.newArrayList();
        for (Pair pair : pairs) {
            String key = pair.getKey();
            String value = pair.getValue();
            if(key.contains(LOCAL_NOISE_OPERATOR)){
                module.setModuleName(LOCAL_NOISE_OPERATOR);
            }
        }
        task.setModule(module);
        task.setInput(input);
        task.setOutputList(ouputList);
        genParties(input,task);
        return task;
    }

    public static List<Party> genParties(Input input, Task task) {
        List<Party> parties = new ArrayList<>();
        for (InputDetail inputData : input.getInputDataDetailList()) {
            Party party = new Party();
            party.setServerInfo(null);
            party.setStatus(null);
            party.setTimestamp(null);
            party.setPartyId(inputData.getDomainId());
            party.setPartyName(inputData.getDomainName());
            parties.add(party);
        }
        parties = parties.stream().filter(StreamUtils.distinctByKey(Party::getPartyId)).collect(Collectors.toList());
        task.setPartyList(parties);
        return parties;
    }

    public static void main(String[] args) {
        String example = "set local.noise.process.key1 = value1; set local.noise.process.key2 = value2; set local.noise.process.key3 = value3;";
        List<Pair> pairs = clauses2Pairs(Lists.newArrayList(example.split(";")));
        System.out.println(pairs);
    }
}
