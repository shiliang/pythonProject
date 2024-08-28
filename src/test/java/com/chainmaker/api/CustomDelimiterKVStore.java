package com.chainmaker.api;

import cn.hutool.core.util.StrUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
        saveToFile();

        // 从文件加载
        loadFromFile(null);

        // 获取键值对
        String value1 = get("key2");
        System.out.println("Value for key1: " + value1);

        // 删除键值对
        delete("key2");
        saveToFile();
    }


    private static Properties store = new Properties();

    public static void put(String key, String value) {
        store.setProperty(key, value);
    }

    private static String formatTimestamp(long timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).format(formatter);
    }
    public static String get(String key) {
        if(store.isEmpty()){
            loadFromFile(DEFAULT_DB_PATH);
        }
        return store.getProperty(key);
    }

    public static void delete(String key) {
        store.remove(key);
    }

    public static void saveToFile() {
        ;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DB_PATH, StandardCharsets.UTF_8, false))) {
            // 自定义分隔符
            for (String key : store.stringPropertyNames()) {
                writer.write(key + DELIMITER + store.getProperty(key) + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadFromFile(String filePath) {
        if(StrUtil.isEmpty(filePath)){
            filePath = DB_PATH;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            store.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER, 2); // 分割为两部分
                if (parts.length == 2) {
                    store.setProperty(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
