package com.huawei.devkit.pipeline.parse;

import com.huawei.devkit.pipeline.bo.CommandLineParams;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class ParamsParser {
    public static CommandLineParams parse(String[] args) throws CmdLineException {
        CommandLineParams params = new CommandLineParams();
        CmdLineParser parser = new CmdLineParser(params);
        try {
            parser.parseArgument(args);
            return params;
        } catch (CmdLineException ex) {
            System.err.println(ex.getMessage());
            new CmdLineParser(new CommandLineParams()).printUsage(System.err);
            throw ex;
        }
    }
}
