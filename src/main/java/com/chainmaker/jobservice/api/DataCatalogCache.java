package com.chainmaker.jobservice.api;

import com.chainmaker.jobservice.core.analyzer.catalog.DataCatalogInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DataCatalogCache {

    public static final Map<String, DataCatalogInfo> dataCatalogInfoMap = new ConcurrentHashMap<>();

    public static void put(String key, DataCatalogInfo value) {
        dataCatalogInfoMap.put(key, value);
    }

    public static DataCatalogInfo getById(String key) {
        return dataCatalogInfoMap.get(key);
    }

    public static DataCatalogInfo getByDIDAndName(String DID, String name){
        for(DataCatalogInfo info : dataCatalogInfoMap.values()){
            if(info.getOrgDID().equals(DID) && info.getName().equals(name)){
                return info;
            }
        }
        return null;
    }
}
