package com.xenosync.strongbag;

public class TestClass {
    public static void main(String[] args) throws Exception {

        String[] testargs = {"-v"};
        new StrongBag(testargs);
        String[] args2 = {"-create", "src/test/resources/bag", "-e", "src/test/resources/files", "src/test/resources/BagItSpec.pdf", "src/test/resources/solr.rb"};

        new StrongBag(args2);
        String[] args3 = {"-verifyvalid", "src/test/resources/bag"};
        new StrongBag(args3);
    }
}
