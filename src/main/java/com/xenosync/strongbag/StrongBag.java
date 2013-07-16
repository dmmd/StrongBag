package com.xenosync.strongbag;

import org.apache.commons.cli.*;
import java.io.File;
import java.util.ArrayList;

public class StrongBag {

    private Options options;

    StrongBag(String[] args) throws Exception {
        getOptions();
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse( options, args);

        if(cmd.hasOption("create") && cmd.hasOption("verifyvalid")){
            System.err.println("Only 'create' or 'verifyvalid' can be run, not both");
            System.exit(1);
        }

        if(cmd.hasOption("create")){
            ArrayList<File> sources = new ArrayList();
            for(Object obj: cmd.getArgList()){
                sources.add(new File((obj.toString())));
            }
            Create create = new Create(sources, new File(cmd.getOptionValue("create")));
        } else if(cmd.hasOption("verifyvalid")){
            File bag = new File(cmd.getOptionValue("verifyvalid"));
            VerifyValid vv = new VerifyValid(bag);
        }
    }

    private void getOptions(){
        options = new Options();
        options.addOption("create", true, "create strongbag at supplied path");
        options.addOption("verifyvalid", true, "verify strongbag at supplied path");
    }
    public static void main(String[] args) throws Exception{
        String strongBagLoc = "src/test/resources/my_strongbag";
        String[] args2 = {"-create", strongBagLoc, "src/test/resources/er1",  "src/test/resources/sbt.tgz"};
        String[] args3 = {"-verifyvalid", strongBagLoc};
        System.out.println("Creating new strongbag");
        new StrongBag(args2);
        System.out.println("Verifying strongbag");
        new StrongBag(args3);
    }
}
