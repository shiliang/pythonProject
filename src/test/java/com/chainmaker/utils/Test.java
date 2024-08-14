package com.chainmaker.utils;

import cn.hutool.core.util.StrUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args) {
        String sql = "set a.b.c.d=val; SET @var1 = 'value1'; SET @var2 = 'value2'; SELECT * FROM table WHERE id = @var1;";
        List<String> eles = StrUtil.split(sql, ";").stream()
                .map(x-> x.strip())
                .filter(s -> !StrUtil.isBlank(s))
                .collect(Collectors.toList());
        List<String> setClauses = eles.stream()
                .filter(s -> s.toUpperCase().startsWith("SET"))
                .collect(Collectors.toList());
        System.out.println(setClauses);
    }


}
