package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.entity.CliOptions;
import com.huawei.devkit.code.inspector.perload.DataBasePreLoad;
import com.huawei.devkit.code.inspector.utils.PropertiesUtils;
import com.huawei.devkit.code.inspector.wrappers.CheckStyleWrapper;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

@Slf4j
public class CodeInspector {
    private static final int CODE_ERROR = 1;
    private static final int PARAMETER_ERROR = 2;
    private static final int OTHER_ERROR = 3;

    public static void main(String[] args) {
        final CliOptions cliOptions = new CliOptions();
        final CommandLine commandLine = new CommandLine(cliOptions);
        try {
            int ret = parseArgsAndExecute(cliOptions, commandLine, args);
            if (ret > 0) {
                Runtime.getRuntime().exit(CODE_ERROR);
            }
        } catch (CommandLine.ParameterException ex) {
            log.error("error", ex);
            System.err.println(ex.getMessage());
            commandLine.usage(System.err);
            Runtime.getRuntime().exit(PARAMETER_ERROR);
        } catch (Exception ex) {
            log.error("error", ex);
            System.err.println(ex.getMessage());
            Runtime.getRuntime().exit(OTHER_ERROR);
        }
    }

    public static int parseArgsAndExecute(CliOptions cliOptions, CommandLine commandLine, String[] args)
        throws IOException, CheckstyleException {
        commandLine.setUsageHelpWidth(CliOptions.HELP_WIDTH);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        CommandLine.ParseResult parseResult = commandLine.parseArgs(args);
        if (!CommandLine.printHelpIfRequested(parseResult)) {
            log.info("start enter log");
            Properties properties = PropertiesUtils.loadProperties("config.properties");
            PropertiesUtils.configAndUpdate(properties);
            if (cliOptions.getConfigurationFile() == null || cliOptions.getConfigurationFile().isEmpty()) {
                cliOptions.setConfigurationFile(PropertiesUtils.ROOT_DIR + "/config/devkit_checkstyle.xml");
            }
            DataBasePreLoad.preload(properties);
            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
            return CheckStyleWrapper.checkStyle(cliOptions);
        }
        return 0;

    }
}
