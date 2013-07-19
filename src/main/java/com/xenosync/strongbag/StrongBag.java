package com.xenosync.strongbag;

import org.apache.commons.cli.*;
import java.io.File;
import java.util.ArrayList;

public class StrongBag {

    private Options options;
    private final String version = "0.0.1";

    StrongBag(String[] args) throws Exception {
        getOptions();
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse( options, args);

        if(cmd.hasOption("create") && cmd.hasOption("verifyvalid")){
            System.err.println("Only 'create' or 'verifyvalid' can be run, not both");
            System.exit(1);
        }

        if(cmd.hasOption("v")){
            System.out.println("Strongbag version " + version);
        }
        else if(cmd.hasOption("create")){
            ArrayList<File> sources = new ArrayList();
            for(Object obj: cmd.getArgList()){
                sources.add(new File((obj.toString())));
            }
            if(cmd.hasOption("e")){
                new Create(sources, new File(cmd.getOptionValue("create")), true);
            } else {
                new Create(sources, new File(cmd.getOptionValue("create")), false);
            }
        } else if(cmd.hasOption("verifyvalid")){
            File bag = new File(cmd.getOptionValue("verifyvalid"));
            VerifyValid vv = new VerifyValid(bag);
        }
    }

    private void getOptions(){
        options = new Options();
        options.addOption("create", true, "create strongbag at supplied path");
        options.addOption("verifyvalid", true, "verify strongbag at supplied path");
        options.addOption("v", false, "print the version");
        options.addOption("e", false, "encrypt the files");
    }
    public static void main(String[] args) throws Exception{
        new StrongBag(args);
    }
}
