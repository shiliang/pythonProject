package com.chainmaker.utils;
import cn.hutool.core.util.ReUtil;

public class HutoolExtractNumber {
    public static void main(String[] args) {
        String input = "{1}";
        // 定义正则表达式，匹配被大括号包围的数字
        String pattern = "\\{(\\d+)\\}";

        // 使用ReUtil.getMatch 获取第一个匹配的数字
        String match = ReUtil.get(pattern, input, 1);
        if (match != null) {
            // 将提取出的字符串转换为整数
            int number = Integer.parseInt(match);
            System.out.println("提取的数字是: " + number);
        } else {
            System.out.println("没有找到匹配的数字");
        }
    }
}

