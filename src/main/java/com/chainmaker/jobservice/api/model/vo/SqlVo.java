package com.chainmaker.jobservice.api.model.vo;

import com.chainmaker.jobservice.core.analyzer.catalog.DataCatalogInfo;
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

    private List<DataCatalogInfo> dataCatalogInfoList;

    private List<ModelParamsVo> modelParams;
}
