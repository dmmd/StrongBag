package com.xenosync.strongbag;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.UUID;

public class Encryption {

    public String generateKey(KeystoreManager ksm) throws NoSuchProviderException, NoSuchAlgorithmException, CertificateException, SignatureException, KeyStoreException, InvalidKeyException, IOException {
        Security.addProvider(new BouncyCastleProvider());
        KeyGenerator kg = KeyGenerator.getInstance("AES", "BC");
        kg.init(128, new SecureRandom());
        SecretKey secretKey = kg.generateKey();
        String alias = UUID.randomUUID().toString();
        ksm.writeKey(secretKey, alias);
        return alias;
    }
}
