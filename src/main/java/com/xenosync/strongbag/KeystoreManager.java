package com.xenosync.strongbag;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.joda.time.DateTime;

public class KeystoreManager {
    private KeyStore rsakey;
    private Config conf;
    private String passphrase;

    KeystoreManager() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException, IOException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        conf = ConfigFactory.parseFile(new File("application.conf"));
        if(!rsaExists()){
            createRsaStore();
        }

        rsakey = KeyStore.getInstance("JKS");
        System.out.println("Accessing keystore");
        passphrase = requestPassword();
        File store = new File(conf.getString("homedir.home"), conf.getString("rsa.store_name"));
        rsakey.load(new FileInputStream(store), passphrase.toCharArray());
    }

    public PrivateKey getPrivateKey() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return (PrivateKey) rsakey.getKey(conf.getString("rsa.alias"), passphrase.toCharArray());
    }

    public X509Certificate getCertificate() throws KeyStoreException {
        return (X509Certificate) rsakey.getCertificate(conf.getString("rsa.alias"));
    }

    public KeyPair getKeypair() throws KeyStoreException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, IOException {
        return new KeyPair(getCertificate().getPublicKey(), getPrivateKey());
    }

    private boolean rsaExists(){
        File f = new File(conf.getString("homedir.home"), conf.getString("rsa.store_name"));
        return f.exists();
    }

    private void createRsaStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException {
        System.out.println("Creating new RSA keystore");
        String pass = requestPassword();
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(conf.getInt("rsa.key_length"));
        KeyPair pair = gen.genKeyPair();
        PrivateKey key = pair.getPrivate();
        X509Certificate cert = generateCertificate(pair);
        X509Certificate[] chain = {cert};

        File store = new File(conf.getString("homedir.home"), conf.getString("rsa.store_name"));
        store.createNewFile();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null,null);
        ks.setKeyEntry(conf.getString("rsa.alias"), key, pass.toCharArray(), chain);
        ks.store(new FileOutputStream(store), pass.toCharArray());
        System.out.println("The Password entered is: " + pass);
        System.out.println("You should keep the password somewhere safe, it is not recoverable");
    }

    private X509Certificate generateCertificate(KeyPair keyPair) throws NoSuchAlgorithmException, CertificateEncodingException, NoSuchProviderException, InvalidKeyException, SignatureException {
        DateTime dt = new DateTime();

        X509V3CertificateGenerator cert = new X509V3CertificateGenerator();
        cert.setSerialNumber(BigInteger.valueOf(1));
        cert.setSubjectDN(new X509Principal("CN=localhost"));
        cert.setIssuerDN(new X509Principal("CN=localhost"));
        cert.setPublicKey(keyPair.getPublic());
        cert.setNotBefore(dt.toDate());
        cert.setNotAfter(dt.plusYears(10).toDate());
        cert.setSignatureAlgorithm("SHA256WithRSA");
        PrivateKey signingKey = keyPair.getPrivate();
        return cert.generate(signingKey, "BC");
    }

    private String requestPassword() throws IOException {
        System.out.println("Enter a password for the keystore: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String pass = br.readLine();
        return pass;
    }
}
