package com.huawei.devkit.pipeline.strategy;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.DelimitedOptionHandler;
import org.kohsuke.args4j.spi.Setter;

public class MultiFieldOptionHandler extends DelimitedOptionHandler<String> {
    private static final String DELIMITER = ",";

    public MultiFieldOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super String> setter) {
        super(parser, option, setter, DELIMITER, new StringOptionHandler(parser, option, setter));
    }
}
