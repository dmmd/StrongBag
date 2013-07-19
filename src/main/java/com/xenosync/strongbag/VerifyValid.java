package com.xenosync.strongbag;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

public class VerifyValid {
    private File bag;
    //private RSASig rsaSig;
    private KeyPair keyPair;
    private long oxum_count = 0;
    private long oxum_size = 1;
    private KeystoreManager ksm;

    VerifyValid(File bag) throws IOException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException {
        System.out.println("Validating StrongBag: " + bag.getName());
        ksm = new KeystoreManager();
        keyPair = ksm.getKeypair();
        this.bag = bag;
        parseManifest();
    }

    private void parseManifest() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, UnrecoverableKeyException, KeyStoreException, CertificateException {
        File manifest = new File(bag, "manifest-sha256rsa.txt");
        if(!manifest.exists()){
            System.err.println("manifest can not be located");
            System.exit(1);
        }

        BufferedReader reader = new BufferedReader(new FileReader(manifest));
        String line;
        while((line = reader.readLine()) != null){
            String sig = line.substring(0, line.indexOf(' '));
            String path = line.substring(line.indexOf(' ') +1);
            if(!verifyExists(path) || !verifyValid(path, sig)){
                System.err.println("Strongbag is not valid");
                System.exit(1);
            }
        }
        System.out.println("result is true");
    }

    private boolean verifyExists(String path){
        File file = new File(bag, path);
        if(file.exists()){
            oxum_count += 1l;
            oxum_size += file.length();
            return true;
        } else {
            return false;
        }
    }

    private boolean verifyValid(String path, String sig) throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException, IOException, UnrecoverableKeyException, KeyStoreException, CertificateException {
        File file = new File(bag, path);
        byte[] signatureBytes = Base64.decodeBase64(sig);

        PublicKey key = keyPair.getPublic();
        Signature signature = Signature.getInstance("SHA256withRSA", "BC");
        signature.initVerify(key);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

        byte[] buffer = new byte[1024];
        int len;
        while (bis.available() != 0) {
            len = bis.read(buffer);
            signature.update(buffer, 0, len);
        };

        bis.close();
        return signature.verify(signatureBytes);
    }
}
