package com.xenosync.strongbag;

import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;

import java.security.*;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;


public class Create {
    private String rootName;
    private int offset;
    private File bag, data, root, manifest, baginfo, bagTxt, tagManifest;
    private List<File> payload = new ArrayList();
    private int oxum_count =0;
    private int oxum_sum = 0;
    private final String version = "0.01";

    Create(File root, File bag) throws IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException {
        rootName = root.getName();
        this.bag = bag;
        bag.mkdir();
        data = new File(this.bag, "data");
        data.mkdir();
        this.root = new File(data, rootName);
        this.root.mkdir();
        offset = root.getParentFile().getAbsolutePath().length();


        walk(root);
        createManifest();
        createBagInfo();
        createBagText();
        createTagManifest();

    }

    private void walk(File fileIn){
        if(fileIn.isFile()){
            payload.add(fileIn);
        }
        if(fileIn.isDirectory()){
            for(File child: fileIn.listFiles()){
                walk(child);
            }
        }
    }

    private void createManifest() throws IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException {
        manifest = new File(bag, "manifest-sha256rsa.txt");
        manifest.createNewFile();
        FileWriter manifestWriter = new FileWriter(manifest);
        RSASig rsaSig = new RSASig();
        for(File file: payload){
            oxum_count++;
            oxum_sum += file.length();
            buildDir(file);
            File copyFile = new File(root, file.getAbsolutePath().substring(offset + rootName.length() + 2));
            copyFile.createNewFile();
            FileOutputStream fileWriter = new FileOutputStream(copyFile);
            InputStream is = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            while(true){
                int bytes = is.read(buffer);
                if(bytes < 0){break;}
                fileWriter.write(buffer, 0, bytes);
            }
            fileWriter.close();
            is.close();

            manifestWriter.write(rsaSig.getSignature(copyFile, "SHA256withRSA") +" data" + file.getAbsolutePath().substring(offset) + "\n");
        }
        manifestWriter.close();
    }

    private void buildDir(File file) throws IOException {
            ArrayList<String> dirs = new ArrayList();
            while(true){
                if(file.getParentFile().getName().equals(rootName)){
                    break;
                }
                file = file.getParentFile();
                dirs.add(file.getName());
            }

            String[] dirArray = dirs.toArray(new String[dirs.size()]);

            for(int i = dirArray.length - 1; i >=0; i--){
                File dir = new File(root, dirArray[i]);
                if(!dir.exists()){
                    dir.mkdir();
                }
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
        fw.write("Bag-Size: " + (double) Math.round((oxum_sum / 1024.0) / 1024.0 * 10) / 10 + " MB");
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
