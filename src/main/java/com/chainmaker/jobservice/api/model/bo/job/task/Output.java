package com.chainmaker.jobservice.api.model.bo.job.task;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-13 11:26
 * @description:任务输出结构
 * @version: 1.0.0
 */

public class Output {
    private List<TaskOutputData> data;

    public List<TaskOutputData> getData() {
        return data;
    }

    public void setData(List<TaskOutputData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Output{" +
                "data=" + data +
                '}';
    }
}
