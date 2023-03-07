package com.chainmaker.jobservice.core.optimizer.model.FL;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author gaokang
 * @date 2022-09-06 11:33
 * @description:Fate任务模型参数
 * @version: 1.0.0
 */

public class FateModel {
    private JSONObject eval;
    private JSONObject fl;
    private JSONObject model;
    private JSONObject intersection;

    public JSONObject getEval() {
        return eval;
    }

    public void setEval(JSONObject eval) {
        this.eval = eval;
    }

    public JSONObject getFl() {
        return fl;
    }

    public void setFl(JSONObject fl) {
        this.fl = fl;
    }

    public JSONObject getModel() {
        return model;
    }

    public void setModel(JSONObject model) {
        this.model = model;
    }

    public JSONObject getIntersection() {
        return intersection;
    }

    public void setIntersection(JSONObject intersection) {
        this.intersection = intersection;
    }

    @Override
    public String toString() {
        return "FateModel{" +
                "eval=" + eval +
                ", fl=" + fl +
                ", model=" + model +
                ", intersection=" + intersection +
                '}';
    }
}
