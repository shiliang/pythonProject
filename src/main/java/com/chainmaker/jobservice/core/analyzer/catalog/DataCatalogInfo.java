package com.chainmaker.jobservice.core.analyzer.catalog;

import com.chainmaker.jobservice.core.analyzer.catalog.DataCatalogDetailInfo;
import lombok.Data;

import java.util.ArrayList;

@Data
public class DataCatalogInfo {
    private String id;
    private String code;
    private String name;
    private String remark;
    private int version;
    private String dataVersion;
    private long publishTime;
    private String orgId;
    private String orgDID;
    private int status;
    private String createTime;
    private ArrayList<DataCatalogDetailInfo> itemVOList;

    private String datasourceNo;

    public boolean addCatalogDetailTuple(DataCatalogDetailInfo tuple){
        return this.itemVOList.add(tuple);
    }

    @Override
    public String toString() {
        return "DataCatalogInfo{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", remark='" + remark + '\'' +
                ", version=" + version +
                ", dataVersion='" + dataVersion + '\'' +
                ", publishTime=" + publishTime +
                ", orgId='" + orgId + '\'' +
                ", orgDID='" + orgDID + '\'' +
                ", status=" + status +
                ", createTime='" + createTime + '\'' +
                ", itemVOList=" + itemVOList +
                ", datasourceNo=" + datasourceNo +
                '}';
    }


}
