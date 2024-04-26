package com.chainmaker.jobservice.api.common.errors;

public enum ErrorEnum {
    // 缺省错误码定义
    ErrCodeOK(200, "成功"),
    ErrCodeUnknown(69999, "错误未识别"),

    // 60000-60999
    ErrCodeInvalidParameter(60000, "参数错误（包括参数格式、类型等错误）"),
    ErrCodeInvalidParameterValue(60001, "参数取值错误"),
    ErrCodeMissingParameter(60002, "缺少参数错误，必传参数没填"),
    ErrCodeUnknownParameter(60003, "未知参数错误，用户多传未定义的参数会导致错误"),
    ErrCodeInternalError(60004, "网络错误"),
    ErrCodeProtoToBean(60005, "Proto转Bean失败"),


    // 资源管理错误码定义61000-61999
    ErrCodeGetAssetByIdFailed(61000, "根据id获取数据资产失败"),
    ErrCodeGetAssetListFailed(61001, "数据资产列表获取失败"),
    ErrCodeGetEnterpriseListFailed(61002, "企业列表查询失败"),
    ErrCodeGetAssetByEnNameFailed(61003, "根据EnName获取数据资产失败"),
    ErrCodeDataComputingResourceListNotFound(61021, "算力资源列表获取失败"),
    ErrCodeDataComputingResourceDetailNotFound(61022, "算力资源详情获取失败"),
    ErrCodeDataComputingResourceChainFailed(61023, "算力资源与链交互失败"),
    ErrCodeDataConfidentialModelListNotFound(61024, "机密计算模型列表获取失败"),
    ErrCodeDataConfidentialModelDetailNotFound(61025, "机密计算模型详情获取失败"),
    ErrCodeDatConfidentialModelChainFailed(61026, "机密计算模型与链交互失败"),
    ErrCodeDatConfidentialModelFailed(61027, "机密计算模型操作失败"),

    // 计算任务错误码定义 62000-62999
    ErrCodeGetMissionContract(62000, "任务信息查询链失败"),
    ErrCodeGetMissionListContract(62001, "任务列表查询链失败"),
    ErrCodeUpdatePortFail(62002, "新增任务更新端口失败"),
    ErrCodePutMissionContract(62003, "新增任务上链失败"),
    ErrCodeQueryJobsContract(62004, "Job信息查询链失败"),
    ErrCodeQueryAllJobsContract(62005, "Job列表查询链失败"),
    ErrCodePreviewDAGFail(62006, "获取DAG信息失败"),
    ErrCodeCommitDAGFail(62007, "提交DAG信息失败"),
    ErrCodeCancelJobRequestFail(62008, "取消Job请求失败"),

    ErrCodeMissionDetailParameter(62020, "任务详情参数错误"),
    ErrCodeCreateMissionApproveContract(62021, "任务审批上链失败"),
    ErrCodeGetMissionApproveListContract(62022, "任务审批列表查询链失败"),
    ErrCodeGetMissionApproveContract(62023, "任务审批详情查询链失败"),



    // 本地资源错误码定义 63000-63999
    ErrCodeLocalResourceError(63000, "本地资源错误"),
    ErrCodeLocalResourcePoolConfigurationFailed(63001, "IP和端口池配置失败"),
    ErrCodeLocalResourcePoolConfigurationNotFound(63002, "IP和端口池获取失败"),
    ErrCodeLocalResourceSecurityListNotFound(63003, "加密公私钥列表获取失败"),
    ErrCodeLocalResourceSecurityFailed(63004, "加密公私钥操作失败"),
    ErrCodeLocalResourceTLSListNotFound(63005, "TLS通讯证书列表获取失败"),
    ErrCodeLocalResourceTLSFailed(63006, "TLS通讯证书操作失败"),
    ErrCodeLocalResourceNodeListNotFound(63007, "本地算力节点列表获取失败"),
    ErrCodeLocalResourceNodeFailed(63008, "本地算力节点操作失败"),
    ErrCodeLocalResourcePlatformInformationNotFound(63020, "平台信息获取失败"),
    ErrCodeLocalResourceAvailableAddressFailed(63021, "获取可用的地址列表失败");



    ;

    private Integer errorCode;
    private String errorMsg;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    ErrorEnum(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public static String getErrorMessage(int errorCode) {
        for (ErrorEnum error : ErrorEnum.values()) {
            if (error.getErrorCode() == errorCode) {
                return error.getErrorMsg();
            }
        }
        return ErrCodeUnknown.getErrorMsg();
    }
}