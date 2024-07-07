/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.jks;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.android.apksig.ApkSigner;
import com.android.apksig.apk.ApkFormatException;
import com.google.common.collect.ImmutableList;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.common.utils.MD5Utils;
import armadillo.studio.common.enums.SignerEnums;

public class SignerApk {
    @NotNull
    @SuppressLint("SimpleDateFormat")
    public static File SignApk(@NotNull File Input, InputStream stream, @NotNull String Pass, @NotNull String AliasPass, boolean deleteInput, int signer_type) throws ApkFormatException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException, KeyStoreException, UnrecoverableKeyException, CertificateException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("_HH_mm_ss");
        File out = new File(Input.getAbsolutePath().replace(".apk", simpleDateFormat.format(System.currentTimeMillis()) + "_Sign.apk"));
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(stream, Pass.toCharArray());
        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, AliasPass.toCharArray());
        X509Certificate x509Certificate = (X509Certificate) keyStore.getCertificate(alias);
        ApkSigner.SignerConfig signerConfig =
                new ApkSigner.SignerConfig.Builder(
                        CloudApp.getContext().getString(R.string.app_name), privateKey, ImmutableList.of(x509Certificate))
                        .build();
        ApkSigner.Builder builder = new ApkSigner.Builder(ImmutableList.of(signerConfig))
                .setCreatedBy(CloudApp.getContext().getString(R.string.app_name))
                .setInputApk(Input)
                .setOutputApk(out)
                .setMinSdkVersion(19)
                .setV1SigningEnabled(false)
                .setV2SigningEnabled(false);
        for (SignerEnums flag : SignerEnums.getFlags(signer_type)) {
            switch (flag) {
                case V1:
                    builder.setV1SigningEnabled(true);
                    break;
                case V2:
                    builder.setV2SigningEnabled(true);
                    break;
            }
        }
        ApkSigner ApkSigner = builder.build();
        ApkSigner.sign();
        if (deleteInput)
            Input.delete();
        return out;
    }

    @NotNull
    @SuppressLint("PackageManagerGetSignatures")
    public static String getSignMd5() {
        try {
            PackageInfo packageInfo = CloudApp.getContext().getPackageManager().getPackageInfo(
                    CloudApp.getContext().getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            return MD5Utils.encryptionMD5(sign.toByteArray());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getSignMd5(@NotNull File path) {
        PackageManager pm = CloudApp.getContext().getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path.getAbsolutePath(), PackageManager.GET_SIGNATURES);
        if (info == null)
            return CloudApp.getContext().getString(R.string.unknown);
        info.applicationInfo.sourceDir = path.getAbsolutePath();
        info.applicationInfo.publicSourceDir = path.getAbsolutePath();
        Signature[] signs = info.signatures;
        Signature sign = signs[0];
        return MD5Utils.encryptionMD5(sign.toByteArray());
    }

    @NotNull
    @SuppressLint("PackageManagerGetSignatures")
    public static String getSignMd5(String packagename) {
        try {
            PackageInfo packageInfo = CloudApp.getContext().getPackageManager().getPackageInfo(
                    packagename, PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            return MD5Utils.encryptionMD5(sign.toByteArray());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
