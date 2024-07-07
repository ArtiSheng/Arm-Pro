/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.jks;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyStore;

public class JksKeyStore extends KeyStore {
    public JksKeyStore() {
        super(new JKS(), new BouncyCastleProvider(), "jks");
    }
}