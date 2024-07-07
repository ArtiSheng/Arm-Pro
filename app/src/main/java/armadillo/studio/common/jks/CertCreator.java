/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.jks;

import android.os.Build;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import armadillo.studio.common.utils.AppUtils;


public class CertCreator {
    @SuppressWarnings("serial")
    public static class DistinguishedNameValues extends LinkedHashMap<ASN1ObjectIdentifier, String> {

        public DistinguishedNameValues() {
            put(BCStyle.C, null);
            put(BCStyle.ST, null);
            put(BCStyle.L, null);
            put(BCStyle.STREET, null);
            put(BCStyle.O, null);
            put(BCStyle.OU, null);
            put(BCStyle.CN, null);
        }

        @Override
        public String put(ASN1ObjectIdentifier oid, String value) {
            if (value != null && value.equals(""))
                value = null;
            if (containsKey(oid))
                super.put(oid, value); // preserve original ordering
            else {
                super.put(oid, value);
                // String cn = remove(BCStyle.CN); // CN will always be last.
                // put(BCStyle.CN,cn);
            }
            return value;
        }

        public void setCountry(String country) {
            put(BCStyle.C, country);
        }

        public void setState(String state) {
            put(BCStyle.ST, state);
        }

        public void setLocality(String locality) {
            put(BCStyle.L, locality);
        }

        public void setStreet(String street) {
            put(BCStyle.STREET, street);
        }

        public void setOrganization(String organization) {
            put(BCStyle.O, organization);
        }

        public void setOrganizationalUnit(String organizationalUnit) {
            put(BCStyle.OU, organizationalUnit);
        }

        public void setCommonName(String commonName) {
            put(BCStyle.CN, commonName);
        }

        @Override
        public int size() {
            int result = 0;
            for (String value : values()) {
                if (value != null)
                    result += 1;
            }
            return result;
        }

        X509Principal getPrincipal() {
            Vector<ASN1ObjectIdentifier> oids = new Vector<>();
            Vector<String> values = new Vector<>();
            for (Map.Entry<ASN1ObjectIdentifier, String> entry : entrySet()) {
                if (entry.getValue() != null && !entry.getValue().equals("")) {
                    oids.add(entry.getKey());
                    values.add(entry.getValue());
                }
            }
            return new X509Principal(oids, values);
        }
    }

    /**
     * 创建JKS签名
     *
     * @param outputStream
     * @param PassWord
     * @param Alias
     * @param AliasPass
     * @throws Exception
     */
    public static void CreateJKS(OutputStream outputStream, String PassWord, String Alias, String AliasPass) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        CertCreator.DistinguishedNameValues distinguishedNameValues = new CertCreator.DistinguishedNameValues();
        distinguishedNameValues.setCommonName("Armadillo");
        distinguishedNameValues.setLocality(Locale.CHINA.getCountry());
        distinguishedNameValues.setCountry(Locale.CHINA.getCountry());
        distinguishedNameValues.setOrganization("Armadillo");
        distinguishedNameValues.setOrganizationalUnit("" + AppUtils.GetVer());
        X509V3CertificateGenerator x509V3CertificateGenerator = new X509V3CertificateGenerator();
        x509V3CertificateGenerator.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        x509V3CertificateGenerator.setIssuerDN(distinguishedNameValues.getPrincipal());
        x509V3CertificateGenerator.setSubjectDN(distinguishedNameValues.getPrincipal());
        x509V3CertificateGenerator.setNotAfter(new Date(System.currentTimeMillis() + 1000L * 365 * 24 * 3600));
        x509V3CertificateGenerator.setNotBefore(new Date());
        x509V3CertificateGenerator.setSignatureAlgorithm("SHA1withRSA");
        x509V3CertificateGenerator.setPublicKey(publicKey);
        X509Certificate x509Certificate;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            x509Certificate = x509V3CertificateGenerator.generate(keyPair.getPrivate());
        else
            x509Certificate = x509V3CertificateGenerator.generate(keyPair.getPrivate(), "BC");
        KeyStore keyStore = new JksKeyStore();
        keyStore.load(null, PassWord.toCharArray());
        keyStore.setKeyEntry(Alias, keyPair.getPrivate(), AliasPass.toCharArray(), new Certificate[]{x509Certificate});
        keyStore.store(outputStream, PassWord.toCharArray());
        outputStream.flush();
        outputStream.close();
    }
}