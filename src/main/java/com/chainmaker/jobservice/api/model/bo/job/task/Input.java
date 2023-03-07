package com.chainmaker.jobservice.api.model.bo.job.task;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-08-13 11:11
 * @description:任务输入结构
 * @version: 1.0.0
 */

public class Input {
    private List<TaskInputData> data;

    public List<TaskInputData> getData() {
        return data;
    }

    public void setData(List<TaskInputData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Input{" +
                "data=" + data +
                '}';
    }
}
