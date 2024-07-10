package com.chainmaker.jobservice.api.model.job.task;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gaokang
 * @date 2022-08-13 10:17
 * @description:任务的模型参数
 * @version: 1.0.0
 */
@Data
public class Module {
    private String moduleName;
    private List<ModuleParam> paramList;

    public Object getValueByKey(String key){
        for (ModuleParam moduleParam : paramList) {
            if (moduleParam.getKey().equals(key)){
                return moduleParam.getValue();
            }
        }
        return null;
    }

    public void deleteByKey(String key){
        paramList = paramList.stream().filter(e -> !e.getKey().equals(key)).collect(Collectors.toList());
    }
    public boolean containsKey(String key){
        for (ModuleParam moduleParam : paramList) {
            if (moduleParam.getKey().equals(key)){
                return true;
            }
        }
        return false;
    }



    @Override
    public String toString() {
        return "Module{" +
                "moduleName='" + moduleName + '\'' +
                ", params=" + paramList +
                '}';
    }
}
