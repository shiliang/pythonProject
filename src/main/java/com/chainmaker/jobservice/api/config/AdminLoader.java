package com.chainmaker.jobservice.api.config;

import org.chainmaker.sdk.User;
import org.chainmaker.sdk.config.AuthType;
import org.chainmaker.sdk.config.ChainClientConfig;
import org.chainmaker.sdk.config.SdkConfig;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.FileUtils;
import org.chainmaker.sdk.utils.UtilsException;

import java.util.ArrayList;

public class AdminLoader {
    public static ArrayList<User> loadAdminList(SdkConfig sdkConfig) throws UtilsException, ChainMakerCryptoSuiteException {
        ArrayList<User> res = new ArrayList<>();
        ChainClientConfig chainClient = sdkConfig.getChain_client(); // Use a local variable for better readability

        if (chainClient.getAuth_type().equals(AuthType.PermissionedWithCert.getMsg())) {
            res.add(createUserFromPermissionedType(chainClient));
        } else {
            res.add(createUserFromPublicType(chainClient));
        }

        return res;
    }

    private static User createUserFromPermissionedType(ChainClientConfig chainClient) throws UtilsException, ChainMakerCryptoSuiteException {
        return new User(chainClient.getOrgId(),
                FileUtils.getFileBytes(chainClient.getUserSignKeyFilePath()),
                FileUtils.getFileBytes(chainClient.getUserSignCrtFilePath()),
                FileUtils.getFileBytes(chainClient.getUserKeyFilePath()),
                FileUtils.getFileBytes(chainClient.getUserCrtFilePath()),
                false);
    }

    private static User createUserFromPublicType(ChainClientConfig chainClient) throws UtilsException, ChainMakerCryptoSuiteException {
        byte[] privateKeyBytes = FileUtils.getFileBytes(chainClient.getUserSignKeyFilePath());
        byte[] publicKeyBytes = FileUtils.getFileBytes(chainClient.getUserSignCrtFilePath());

        return new User("public",
                privateKeyBytes, "".getBytes(),
                publicKeyBytes, AuthType.Public.getMsg());
    }
}
