package com.chainmaker.jobservice.api.service.impl;

import com.chainmaker.jobservice.api.response.ContractException;
import com.chainmaker.jobservice.api.response.ContractServiceResponse;
import com.chainmaker.jobservice.api.service.BlockchainContractService;
import lombok.extern.slf4j.Slf4j;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.pb.common.Request;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.sdk.*;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.FileUtils;
import org.chainmaker.sdk.utils.SdkUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

@Service
@Slf4j
public class BlockchainContractServiceImpl implements BlockchainContractService {

    private ChainClient chainClient;
    private ChainManager chainManager;
    private ArrayList<User> adminUserList;
    long rpcCallTimeout = 10000;
    long syncResultTimeout = 10000;

    @Override
    public ChainClient getChainClient() {
        return chainClient;
    }

    @Override
    public void setChainClient(ChainClient chainClient) {
        this.chainClient = chainClient;
    }

    @Override
    public ChainManager getChainManager() {
        return chainManager;
    }

    @Override
    public void setChainManager(ChainManager chainManager) {
        this.chainManager = chainManager;
    }

    @Override
    public User[] getAdminUserList() {
        return adminUserList.toArray(new User[adminUserList.size()]);
    }

    @Override
    public void setAdminUserList(ArrayList<User> adminUser) {
        this.adminUserList = adminUser;
    }

    @Override
    public void createContract(String contractFilePath, String contractName, Map<String, byte[]> params) {
        ResultOuterClass.TxResponse responseInfo = null;
        try {
            byte[] byteCode = FileUtils.getFileBytes(contractFilePath);

            // 1. create payload
            Request.Payload payload = chainClient.createContractCreatePayload(contractName, "1", byteCode,
                    ContractOuterClass.RuntimeType.WASMER, params);
            ;
            //2. create payloads with endorsement
            Request.EndorsementEntry[] endorsementEntries = SdkUtils.getEndorsers(payload, getAdminUserList());
            // 3. send request
            responseInfo = chainClient.sendContractManageRequest(payload, endorsementEntries, rpcCallTimeout, syncResultTimeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upgradeContract(String contractFilePath, String contractName, String version, Map<String, byte[]> params) {
        ResultOuterClass.TxResponse responseInfo = null;
        try {
            byte[] byteCode = FileUtils.getFileBytes(contractFilePath);

            // 1. create payload
            Request.Payload payload = chainClient.createContractUpgradePayload(contractName, version, byteCode,
                    ContractOuterClass.RuntimeType.WASMER, params);
            ;
            //2. create payloads with endorsement
            Request.EndorsementEntry[] endorsementEntries = SdkUtils.getEndorsers(payload, getAdminUserList());
            // 3. send request
            responseInfo = chainClient.sendContractManageRequest(payload, endorsementEntries, rpcCallTimeout, syncResultTimeout);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed");
        }
    }

    public void revokeContract(String contractName) {
        ResultOuterClass.TxResponse responseInfo = null;
        try {
            // 1. create payload
            Request.Payload payload = chainClient.createContractRevokePayload(contractName);

            // 2. create payloads with endorsement
            Request.EndorsementEntry[] endorsementEntries = SdkUtils.getEndorsers(payload, getAdminUserList());

            // 3. send request
            responseInfo = chainClient.sendContractManageRequest(payload, endorsementEntries, rpcCallTimeout, syncResultTimeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ContractServiceResponse invokeContract(String contractName, String contractMethod, Map<String, byte[]> params) {
        ResultOuterClass.TxResponse responseInfo = null;
        ContractServiceResponse csr = new ContractServiceResponse();
        csr.setQuery(false);
        try {
            // 创建一个StringBuilder来构建日志信息
            StringBuilder sb = new StringBuilder();
            sb.append("Contract Invoke: ").append(contractName).append("---").append(contractMethod).append("---");
            for (Map.Entry<String, byte[]> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = new String(entry.getValue(), StandardCharsets.UTF_8);
                sb.append(key).append("=").append(value).append("; ");
            }
            log.info(sb.toString());

            responseInfo = chainClient.invokeContract(contractName, contractMethod, null, params, 10000, 100000);
            csr.fromChainResponse(responseInfo);
        } catch (Exception e) {

            csr.setMessage(e.getMessage());
        }
        if (csr.isOk()) {
            return csr;
        } else {
            throw new ContractException(csr.toString());
        }
    }

    @Override
    public ContractServiceResponse queryContract(String contractName, String contractMethod, Map<String, byte[]> params) {
        ResultOuterClass.TxResponse responseInfo = null;
        ContractServiceResponse csr = new ContractServiceResponse();
        csr.setQuery(true);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Contract Query: ").append(contractName).append("---").append(contractMethod).append("---");
            for (Map.Entry<String, byte[]> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = new String(entry.getValue(), StandardCharsets.UTF_8);
                sb.append(key).append("=").append(value).append("; ");
            }
            log.info(sb.toString());

            responseInfo = chainClient.queryContract(contractName, contractMethod, null, params, 10000);
            csr.fromChainResponse(responseInfo);
            log.info("Contract return: " + contractName + "---" + contractMethod + "---" + csr.getJsonResult());
        } catch (Exception e) {
            csr.setMessage(e.getMessage());
        }
        if (csr.isOk()) {
            return csr;
        } else {
            throw new ContractException(csr.toString());
        }

    }

    public ChainmakerTransaction.TransactionInfo getTXByH(String txId) throws ChainClientException, ChainMakerCryptoSuiteException {
        ChainmakerTransaction.TransactionInfo info = this.chainClient.getTxByTxId(txId, this.rpcCallTimeout);
        return info;
    }
}