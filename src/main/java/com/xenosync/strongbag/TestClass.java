package com.xenosync.strongbag;

public class TestClass {
    public static void main(String[] args) throws Exception {

        String[] testargs = {"-v"};
        StrongBag strongBag = new StrongBag(testargs);
        String[] args2 = {"-create", "src/test/resources/bag", "src/test/resources/files", "src/test/resources/BagItSpec.pdf", "src/test/resources/solr.rb"};
        String[] args3 = {"-verifyvalid", "src/test/resources/bag"};
        String[] args4 = {"-create", "src/test/resources/bag", "-e", "src/test/resources/files", "src/test/resources/BagItSpec.pdf", "src/test/resources/solr.rb"};

        strongBag = new StrongBag(args2);
        strongBag = new StrongBag(args3);
        strongBag = new StrongBag(args4);
        strongBag = new StrongBag(args3);


    }
}
