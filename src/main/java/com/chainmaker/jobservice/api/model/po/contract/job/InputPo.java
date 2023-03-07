package com.chainmaker.jobservice.api.model.po.contract.job;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-19 19:29
 * @description:
 * @version:
 */

public class InputPo {
    private List<TaskInputDataPo> data;

    public List<TaskInputDataPo> getData() {
        return data;
    }

    public void setData(List<TaskInputDataPo> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "InputPo{" +
                "data=" + data +
                '}';
    }
}
