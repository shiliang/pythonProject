package com.chainmaker.jobservice.core.analyzer.catalog;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chainmaker.jobservice.api.response.ParserException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-01 15:11
 * @description:访问数据目录，获取元数据
 * @version: 1.0.0
 */

public class CatalogService {
    private static final String GET_DATA_CATALOG_URL = "http://172.16.13.32:8008/dataCatalog/name/all";
    public void getCatalogByName(List<String> NameList) {
        RestTemplate restTemplate = new RestTemplate();
        JSONObject response = restTemplate.postForObject(GET_DATA_CATALOG_URL, NameList, JSONObject.class);
        if (response == null || response.getJSONArray("data") == null) {
            throw new ParserException("请求数据目录失败");
        }
        JSONArray dataCatalogInfoList = response.getJSONArray("data");

    }
}
