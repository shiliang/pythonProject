package com.chainmaker.jobservice.api.response;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.chainmaker.pb.common.ResultOuterClass;

import java.util.Objects;

public class ContractServiceResponse {
    //1 for query; 0 for invoke
    private boolean isQuery;
    private String message;
    private String jsonResult;

    public boolean isQuery() {
        return isQuery;
    }

    public void setQuery(boolean query) {
        isQuery = query;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(String jsonResult) {
        this.jsonResult = jsonResult;
    }

    public void fromChainResponse(ResultOuterClass.TxResponse rInfo) {
        if (rInfo == null) {
            this.setMessage("unknown error");
        } else {
            if (isContractTransactionOk(rInfo)) {
                if (this.isQuery()) {
                    this.setJsonResult(rInfo.getContractResult().getResult().toStringUtf8());
                } else {
                    this.setJsonResult(rInfo.getTxId());
                }
            } else {
                this.setMessage(rInfo.getContractResult().getMessage());
            }
        }
    }

    @Override
    public String toString() {
        if (isOk()) {
            return jsonResult;
        } else {
            return message;
        }
    }

    public JSONObject toJSON(boolean isArray) {
        JSONObject res = new JSONObject();
        if (this.isQuery()) {
            if (this.isOk()) {
                if(isArray) {
                    res.put("result",JSONArray.parseArray(this.getJsonResult()));
                } else {
                    res.put("result", JSON.parse(this.getJsonResult()));
                }
            } else {
                res.put("result", this.getMessage());
            }
        } else {
            res.put("result", this.toString());
        }
        return res;
    }

    public boolean isOk() {
        return Objects.equals(this.getMessage(), "") || this.message == null;
    }

    public static boolean isContractTransactionOk(ResultOuterClass.TxResponse rInfo) {
        return
                rInfo.getCode().equals(ResultOuterClass.TxStatusCode.SUCCESS) &&
                        rInfo.getContractResult().getCode() == 0;
    }
}
