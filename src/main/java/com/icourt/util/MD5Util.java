package com.icourt.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author liusiyuan
 */
public class MD5Util {

    public static String md5Hex(byte[] content) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        md.update(content);
        return new BigInteger(1, md.digest()).toString(16);
    }
}
