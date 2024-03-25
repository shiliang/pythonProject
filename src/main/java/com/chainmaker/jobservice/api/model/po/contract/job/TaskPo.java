package com.chainmaker.jobservice.api.model.po.contract.job;

import com.chainmaker.jobservice.api.model.bo.job.task.*;
import lombok.Data;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-19 19:08
 * @description:持久化的Task结构
 * @version: 1.0.0
 */
@Data
public class TaskPo {
    private String version;
    private String jobID;
    private String taskName;
    private String taskLabel;
    private String status;
    private String updateTime;
    private String createTime;

    private ModulePo module;
    private InputPo input;
    private Output output;
    private List<Party> parties;
}
