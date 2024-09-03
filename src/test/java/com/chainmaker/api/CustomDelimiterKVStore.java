package com.chainmaker.api;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Properties;

public class CustomDelimiterKVStore {

    private static final String DB_PATH = "./kv_store_custom" + formatTimestamp(System.currentTimeMillis()) + ".text";
    private static final String DEFAULT_DB_PATH = "./kv_store_custom.text";
    private static final String DELIMITER = "\u0001"; // 自定义分隔符

    public static void main(String[] args) {
        // 设置键值对
        put("key1", "value1");
        put("key2", "中文");

        // 保存到文件
        saveToFile(DEFAULT_DB_PATH);

        // 从文件加载
        loadFromFile(DEFAULT_DB_PATH);

        // 获取键值对
        String value1 = get("key2");
        System.out.println("Value for key1: " + value1);

        // 删除键值对
        delete("key2");
        saveToFile(DEFAULT_DB_PATH);
    }


//    private static Properties store = new Properties();
    private static LinkedHashMap<String, String> store = Maps.newLinkedHashMap();

    public static void put(String key, String value) {
        store.put(key, value);
    }

    public static String formatTimestamp(long timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).format(formatter);
    }
    public static String get(String key) {
        return store.get(key);
    }

    public static void delete(String key) {
        store.remove(key);
    }

    public static void saveToFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, StandardCharsets.UTF_8, false))) {
            // 自定义分隔符
            for (String key : store.keySet()) {
                writer.write(key + DELIMITER + store.get(key) + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            store.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER, 2); // 分割为两部分
                if (parts.length == 2) {
                    store.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
