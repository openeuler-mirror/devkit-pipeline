package com.huawei.devkit.pipeline.strategy;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.DelimitedOptionHandler;
import org.kohsuke.args4j.spi.IntOptionHandler;
import org.kohsuke.args4j.spi.Setter;

public class MultiHandlerFactory {
    private static final String DELIMITER = ",";

    public static class MultiFieldOptionHandler extends DelimitedOptionHandler<String> {

        public MultiFieldOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super String> setter) {
            super(parser, option, setter, DELIMITER, new StringOptionHandler(parser, option, setter));
        }

    }

    public static class MultiIntegerOptionHandler extends DelimitedOptionHandler<Integer> {

        public MultiIntegerOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Integer> setter) {
            super(parser, option, setter, DELIMITER, new IntOptionHandler(parser, option, setter));
        }
    }

}
