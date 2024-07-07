/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
    @NotNull
    public static String encryptionMD5(byte[] byteStr) {
        MessageDigest messageDigest = null;
        StringBuilder md5StrBuff = new StringBuilder();
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(byteStr);
            byte[] byteArray = messageDigest.digest();
            for (byte b : byteArray) {
                if (Integer.toHexString(0xFF & b).length() == 1) {
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF & b));
                } else {
                    md5StrBuff.append(Integer.toHexString(0xFF & b));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5StrBuff.toString();
    }

    @NotNull
    public static String encryptionMD5(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        MessageDigest messageDigest = null;
        StringBuilder md5StrBuff = new StringBuilder();
        messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.reset();
        int len;
        byte[] bytes = new byte[1024];
        while ((len = inputStream.read(bytes)) != -1)
            messageDigest.update(bytes, 0, len);
        byte[] byteArray = messageDigest.digest();
        for (byte b : byteArray) {
            if (Integer.toHexString(0xFF & b).length() == 1) {
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & b));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & b));
            }
        }
        return md5StrBuff.toString();
    }

    @NotNull
    public static String encryptionMD5(File file) throws IOException, NoSuchAlgorithmException {
        return encryptionMD5(new FileInputStream(file));
    }

    @NotNull
    public static String encryptionMD5(String path) throws IOException, NoSuchAlgorithmException {
        return encryptionMD5(new FileInputStream(path));
    }
}
