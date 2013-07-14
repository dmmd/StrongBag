package com.xenosync.strongbag;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

public class RSASig {
    private KeyPair keyPair;

    RSASig() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException, IOException, UnrecoverableKeyException {
        Security.addProvider(new BouncyCastleProvider());
        Config conf = ConfigFactory.parseFile(new File("application.conf"));
        keyPair = new KeystoreManager().getKeypair();
    }

    public String getSignature(File fileIn, String algorithm) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException {
        Signature rsa = Signature.getInstance(algorithm, "BC");
        rsa.initSign(keyPair.getPrivate());

        BufferedInputStream is = new BufferedInputStream(new FileInputStream(fileIn));
        byte[] buffer = new byte[1024];
        int len;

        while ((len = is.read(buffer)) >= 0) {
            rsa.update(buffer, 0, len);
        };
        is.close();
        byte[] sig = rsa.sign();

        return new String(Base64.encodeBase64(sig));
    }

    public boolean validateSignature(byte[] sigBytes, File file, String algorithm) throws InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException, IOException, SignatureException {
        PublicKey key = keyPair.getPublic();
        Signature signature = Signature.getInstance(algorithm, "BC");
        signature.initVerify(key);


        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

        byte[] buffer = new byte[1024];
        int len;
        while (bis.available() != 0) {
            len = bis.read(buffer);
            signature.update(buffer, 0, len);
        };

        bis.close();
        return signature.verify(sigBytes);
    }
}
