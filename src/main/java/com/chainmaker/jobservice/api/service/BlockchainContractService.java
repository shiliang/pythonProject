package com.chainmaker.jobservice.api.service;

import com.chainmaker.jobservice.api.response.ContractServiceResponse;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.chainmaker.sdk.ChainClient;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.ChainManager;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;

import java.util.ArrayList;
import java.util.Map;

public interface BlockchainContractService {
    ChainClient getChainClient();

    void setChainClient(ChainClient chainClient);

    ChainManager getChainManager();

    void setChainManager(ChainManager chainManager);

    User[] getAdminUserList();

    void setAdminUserList(ArrayList<User> adminUser);

    void createContract(String contractFilePath, String contractName, Map<String, byte[]> params);

    public void upgradeContract(String contractFilePath, String contractName,String version, Map<String, byte[]> params);

    ContractServiceResponse invokeContract(String contractName, String contractMethod, Map<String, byte[]> params);

    ContractServiceResponse queryContract(String contractName, String contractMethod, Map<String, byte[]> params);

    public ChainmakerTransaction.TransactionInfo getTXByH(String txId) throws ChainClientException, ChainMakerCryptoSuiteException;
}