package com.xenosync.strongbag;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;

public class VerifyValid {
    private File bag;
    private KeyPair keyPair;
    private long oxum_count = 0;
    private long oxum_size = 1;
    private KeystoreManager ksm;
    private String alias;
    private boolean encrypted = false;

    VerifyValid(File bag) throws IOException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {


        this.bag = bag;
        parseInfo();
        if(encrypted == true){
            System.out.println("Validating encrypted StrongBag: " + bag.getName());
        } else {
            System.out.println("Validating StrongBag: " + bag.getName());
        }
        ksm = new KeystoreManager();
        keyPair = ksm.getKeypair();
            parseManifest();
    }

    private void parseManifest() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, UnrecoverableKeyException, KeyStoreException, CertificateException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
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

            if(encrypted == false){
                if(!verifyExists(path) || !verifyValid(path, sig)){
                    System.err.println("Strongbag is not valid");
                    System.exit(1);
                }
            } else{
                if(!verifyExists(path) || ! verifyValidEncrypted(path, sig)){
                    System.err.println("Strongbag is not valid");
                    System.exit(1);
                }
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

    private boolean verifyValid(String path, String sig) throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException, IOException, UnrecoverableKeyException, KeyStoreException, CertificateException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
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
        }

        bis.close();
        return signature.verify(signatureBytes);
    }

    private boolean verifyValidEncrypted(String path, String sig) throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException, IOException, UnrecoverableKeyException, KeyStoreException, CertificateException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        File file = new File(bag, path);
        byte[] signatureBytes = Base64.decodeBase64(sig);

        PublicKey key = keyPair.getPublic();
        Signature signature = Signature.getInstance("SHA256withRSA", "BC");
        signature.initVerify(key);
        InputStream is = new BufferedInputStream(new FileInputStream(file));

        byte[] iv = new byte[16];
        is.read(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, ksm.getSecretKey(alias), new IvParameterSpec(iv));
        CipherInputStream cis = new CipherInputStream(is,cipher);

        byte[] buffer = new byte[1024];
        int i;
        while((i = cis.read(buffer)) > 0){
            signature.update(buffer,0,i);
        }

        boolean result = signature.verify(signatureBytes);
        System.out.println(result);
        return result;

    }

    private void parseInfo() throws IOException {
        File infoFile = new File(bag, "strongbag-info.txt");
        BufferedReader br = new BufferedReader(new FileReader(infoFile));
        String line;

        while((line = br.readLine()) != null){
            String[] kv = line.split("\\: ");
            if(kv[0].equals("key-alias")){
                alias = kv[1].trim();
                encrypted = true;
            }
        }
    }
}
