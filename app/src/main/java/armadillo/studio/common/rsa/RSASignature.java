/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.rsa;

import android.util.Base64;

import org.jetbrains.annotations.NotNull;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import armadillo.studio.CloudApp;
import armadillo.studio.R;

public class RSASignature {
    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    public static byte[] sign(byte[] content) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(CloudApp.getContext().getString(R.string.sign_key), Base64.NO_WRAP));
        KeyFactory keyf = KeyFactory.getInstance("RSA");
        PrivateKey priKey = keyf.generatePrivate(priPKCS8);
        Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
        signature.initSign(priKey);
        signature.update(byteMerger(byteMerger(content, "Armadillo".getBytes()), CloudApp.getContext().getString(R.string.tencent_appid).getBytes()));
        return signature.sign();
    }

    @NotNull
    private static byte[] byteMerger(@NotNull byte[] bt1, @NotNull byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }
}
