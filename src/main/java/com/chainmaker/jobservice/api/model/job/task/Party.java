package com.chainmaker.jobservice.api.model.job.task;

import lombok.Data;

/**
 * @author gaokang
 * @date 2022-08-13 11:28
 * @description:任务参与方信息
 * @version: 1.0.0
 */
@Data
public class Party {
    private String partyId;
    private String partyName;
    private ServerInfo serverInfo;
    private String status;
    private String timestamp;
}
