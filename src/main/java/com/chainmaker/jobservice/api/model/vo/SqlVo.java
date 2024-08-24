package com.chainmaker.jobservice.api.model.vo;

import com.chainmaker.jobservice.api.builder.Pair;
import com.chainmaker.jobservice.api.model.AssetInfo;
import com.chainmaker.jobservice.api.model.OrgInfo;
import lombok.Data;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-22 21:34
 * @description:用户提交SQL
 * @version: 1.0.0
 */

@Data
public class SqlVo {
    private String sqltext;

    private Integer modelType;

    private Integer isStream;

    private List<AssetInfo> assetInfoList;

    private List<ModelParamsVo> modelParams;

    private OrgInfo orgInfo;

    private String executeSql;

    private List<String> setClauses;

    private List<Pair> setPairs;
}
