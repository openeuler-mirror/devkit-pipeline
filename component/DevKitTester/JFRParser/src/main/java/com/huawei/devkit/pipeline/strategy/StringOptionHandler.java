package com.huawei.devkit.pipeline.strategy;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OneArgumentOptionHandler;
import org.kohsuke.args4j.spi.Setter;

public class StringOptionHandler extends OneArgumentOptionHandler<String> {
    public StringOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super String> setter) {
        super(parser, option, setter);
    }

    @Override
    protected String parse(String argument) throws NumberFormatException {
        return argument;
    }
}
