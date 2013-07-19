package com.xenosync.strongbag;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;

import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Create {
    private String rootName, alias;
    private String offset;
    private File bag, data, root, manifest, baginfo, bagTxt, tagManifest;
    private ArrayList<File> files;
    private Map<File, String> payload = new TreeMap();
    private long oxum_count =0;
    private long oxum_sum = 0;
    private final String version = "0.01";
    private KeystoreManager ksm;
    private boolean encrypt;

    Create(ArrayList<File> files, File bag, boolean encrypt) throws IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException {
        System.out.println("Creating StrongBag");
        ksm = new KeystoreManager();
        this.files = files;
        this.bag = bag;
        this.encrypt = encrypt;
        bag.mkdir();
        data = new File(this.bag, "data");
        data.mkdir();
        process();
        alias = new Encryption().generateKey(ksm);
        writeFiles();
    }

    private void process() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException {
        for(File file: files){

            if(file.isFile()){
                payload.put(file, file.getName());
            }
            else if(file.isDirectory()){
                rootName = file.getName();
                root = new File(data, rootName);
                root.mkdir();
                String currentDir = file.getName();
                walk(file, currentDir);
            }

        }
    }

    private void writeFiles() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException {
        System.out.println(payload.size() + " files in payload");
        createManifest();
        createBagInfo();
        createBagText();
        createTagManifest();
    }

    private void walk(File fileIn, String offset){
        if(fileIn.isFile()){
            payload.put(fileIn, new File(offset, fileIn.getName()).toString());

        }
        if(fileIn.isDirectory()){
            for(File child: fileIn.listFiles()){
                walk(child, offset);
            }
        }
    }

    private void createManifest() throws IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException {
        manifest = new File(bag, "manifest-sha256rsa.txt");
        manifest.createNewFile();
        FileWriter manifestWriter = new FileWriter(manifest);
        KeyPair keyPair = ksm.getKeypair();
        for(Map.Entry e : payload.entrySet()){
            File currentFile = (File) e.getKey();
            String targetPath = e.getValue().toString();
            oxum_count++;
            oxum_sum += currentFile.length();
            String[] path = targetPath.split("\\/");
            if(path.length > 1){
                buildDir(path);
            }
            File copyFile = new File(data, targetPath);
            copyFile.createNewFile();
            OutputStream os = new FileOutputStream(copyFile);
            InputStream is = new FileInputStream(currentFile);
            byte[] buffer = new byte[1024];
            Signature rsa = Signature.getInstance("SHA256withRSA", "BC");
            rsa.initSign(keyPair.getPrivate());

            while(true){
                int bytes = is.read(buffer);
                if(bytes < 0) break;
                rsa.update(buffer, 0, bytes);
                os.write(buffer, 0, bytes);
                os.flush();
            }


            is.close();
            os.close();

            manifestWriter.write(new String(Base64.encodeBase64(rsa.sign())) +" data/" + targetPath + "\n");
        }
        manifestWriter.close();

    }

    private void buildDir(String[] path) throws IOException {
        for(int i = path.length -2; i >= 0; --i){
            File f = new File(data, path[i]);
            if(!f.exists()){f.mkdir();}
        }
    }

    private void createBagInfo() throws IOException {
        baginfo = new File(bag, "strongbag-info.txt");
        baginfo.createNewFile();
        OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(baginfo));

        DateTime dt = DateTime.now();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        fw.write("Payload-Oxum: " + oxum_sum + "." + oxum_count +"\n");
        fw.write("Bagging-Date: " + fmt.print(dt) + "\n");
        fw.write("Bag-Size: " + (double) Math.round((oxum_sum / 1024.0) / 1024.0 * 10) / 10 + " MB\n");
        if(encrypt == true) fw.write("key-alias: " + alias);
        fw.flush();
        fw.close();
    }

    private void createBagText() throws IOException{
        bagTxt = new File(bag, "strongbag.txt");
        bagTxt.createNewFile();
        OutputStream os = new FileOutputStream(bagTxt);
        os.write(("StrongBag-Version: " + version + "\n").getBytes());
        os.write(("Tag-File-Character-Encoding: " + System.getProperty("file.encoding")).getBytes());
        os.flush();
        os.close();
    }

    private void createTagManifest() throws IOException, NoSuchAlgorithmException {
        tagManifest = new File(bag, "tagmanifest-md5.txt");
        tagManifest.createNewFile();
        OutputStream os = new FileOutputStream(tagManifest);
        File[] files = {manifest, baginfo, bagTxt};
        for(File file: files){
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(file);
            DigestInputStream dis = new DigestInputStream(is, md);
            byte[] buffer = new byte[128];
            while(true){
                int bytes = is.read(buffer);
                if(bytes < 0){break;}
                md.update(buffer, 0, bytes);
            }
            os.write((Hex.encodeHexString(md.digest()) + ": " + file.getName() + "\n").getBytes());
        }
        os.close();
    }

}
