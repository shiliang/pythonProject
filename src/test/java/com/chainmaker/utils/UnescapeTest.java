package com.chainmaker.utils;

import org.apache.commons.lang3.StringEscapeUtils;

public class UnescapeTest {

    public static void main(String[] args) {
        // 原始 JSON 字符串，包含反斜杠
        String jsonStringWithEscapes = "{\"inputDataDetailList\":[{\"dataId\":\"atest_1\",\"dataName\":\"atest_1\",\"domainId\":\"1\",\"params\":\"{\\\"field\\\":\\\"a1\\\",\\\"noise\\\":\\\"{\\\\\\\"algo\\\\\\\": \\\\\\\"\\\\\\\", \\\\\\\"epsilon\\\\\\\": \\\\\\\"\\\\\\\", \\\\\\\"sensitivity\\\\\\\": \\\\\\\"\\\\\\\", \\\\\\\"delta\\\\\\\": \\\\\\\"\\\\\\\"}\\\",\\\"table\\\":\\\"atest_1\\\"}\",\"role\":\"client\",\"taskSrc\":\"\"},{\"dataId\":\"btest_2\",\"dataName\":\"btest_2\",\"domainId\":\"2\",\"params\":\"{\\\"field\\\":\\\"b2\\\",\\\"table\\\":\\\"btest_2\\\"}\",\"role\":\"client\",\"taskSrc\":\"\"}],\"taskId\":\"1\"}";

        // 去除 JSON 字符串中的反斜杠
        String jsonStringWithoutEscapes = unescapeJson(jsonStringWithEscapes);

        // 输出结果
        System.out.println(jsonStringWithoutEscapes);
    }

    /**
     * 去除 JSON 字符串中的反斜杠。
     *
     * @param jsonString 带有反斜杠的 JSON 字符串
     * @return 不含反斜杠的 JSON 字符串
     */
    private static String unescapeJson(String jsonString) {
        // 使用 StringEscapeUtils 的 unescapeJava 方法去除反斜杠
        return StringEscapeUtils.unescapeJson(jsonString);
    }
}

