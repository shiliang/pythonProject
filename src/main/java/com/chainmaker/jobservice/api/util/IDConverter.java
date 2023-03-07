package com.chainmaker.jobservice.api.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;
//import org.springframework.security.crypto.codec.Hex;
import java.util.Base64;

public class IDConverter {
    public static String getSHA256HexStr(String str, int length) {
        MessageDigest messageDigest;
        byte[] returnHash = new byte[length];
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(str.getBytes("UTF-8"));
            for (int i = 0; i < length; i++) {
                returnHash[i] = hash[i];
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Hex.encodeHexString( returnHash );
    }
    public static String getSHA256Base64Str(String str, int length) {
        MessageDigest messageDigest;
        byte[] returnHash = new byte[length];
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(str.getBytes("UTF-8"));
            for (int i = 0; i < length; i++) {
                returnHash[i] = hash[i];
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(returnHash);
    }
}
