package com.chainmaker.jobservice.api.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvUtil {

    public static void writeToCsv(String headLabel, List<String> dataList, String filePath, boolean addFlag) {
        BufferedWriter buffWriter = null;
        try {
            File csvFile = new File(filePath);
            FileWriter writer = new FileWriter(csvFile, addFlag);
            buffWriter = new BufferedWriter(writer, 1024);
            if (StringUtils.isNotBlank(headLabel)) {
                buffWriter.write(headLabel);
                buffWriter.newLine();
            }
            for (String rowStr : dataList) {
                if (StringUtils.isNotBlank(rowStr)) {
                    buffWriter.write(rowStr);
                    buffWriter.newLine();
                }
            }
            buffWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (buffWriter != null) {
                    buffWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> readFromCsv(String filePath) {
        ArrayList<String> dataList = new ArrayList<>();
        BufferedReader buffReader = null;
        try {
            File csvFile = new File(filePath);
            if (!csvFile.exists()) {
                log.info("文件不存在");
                return dataList;
            }
            FileReader fileReader = new FileReader(csvFile);
            buffReader = new BufferedReader(fileReader);
            String line = "";
            while ((line = buffReader.readLine()) != null) {
                if (StringUtils.isNotBlank(line)) {
                    dataList.add(line);
                }
            }
        } catch (Exception e) {
            log.error("读取csv文件发生异常",e);
            e.printStackTrace();
        } finally {
            try {
                if (buffReader != null) {
                    buffReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dataList;
    }

}
