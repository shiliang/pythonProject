package com.chainmaker.jobservice.api.model.job.task;

import lombok.Data;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-13 11:11
 * @description:任务输入结构
 * @version: 1.0.0
 */
@Data
public class Input {
    private String taskId;
//    private String srcTaskId;
//    private String srcTaskName;
    private List<InputDetail> inputDataDetailList;

    @Override
    public String toString() {
        return "Input{" +
                "data=" + inputDataDetailList +
                '}';
    }
}
