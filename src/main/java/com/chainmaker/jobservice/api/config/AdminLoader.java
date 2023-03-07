package com.chainmaker.jobservice.api.config;

import org.chainmaker.sdk.User;
import org.chainmaker.sdk.config.SdkConfig;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.FileUtils;
import org.chainmaker.sdk.utils.UtilsException;

import java.util.ArrayList;

public class AdminLoader {
    public static ArrayList<User> loadAdminList(SdkConfig sdkConfig) throws UtilsException, ChainMakerCryptoSuiteException {
        ArrayList<User> res = new ArrayList<>();
        res.add(new User(sdkConfig.getChain_client().getOrgId(),
                FileUtils.getFileBytes(sdkConfig.getChain_client().getUserSignKeyFilePath()),
                FileUtils.getFileBytes(sdkConfig.getChain_client().getUserSignCrtFilePath()),
                FileUtils.getFileBytes(sdkConfig.getChain_client().getUserKeyFilePath()),
                FileUtils.getFileBytes(sdkConfig.getChain_client().getUserCrtFilePath()),false));
        return res;
    }
}
