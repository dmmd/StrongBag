package com.xenosync.strongbag;

import org.apache.commons.cli.*;
import java.io.File;

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
            File source = new File(cmd.getArgList().get(0).toString());
            Create create = new Create(source, new File(cmd.getOptionValue("create")));
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
        //String[] args2 = {"-verifyvalid", "/Users/dm/Desktop/my_strongbag"};
        StrongBag sb = new StrongBag(args);
    }
}
