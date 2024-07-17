package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.entity.CliOptions;
import com.huawei.devkit.code.inspector.perload.DataBasePreLoad;
import com.huawei.devkit.code.inspector.utils.PropertiesUtils;
import com.huawei.devkit.code.inspector.wrappers.CheckStyleWrapper;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Properties;

@Slf4j
public class CodeInspector {
//    private static final Logger logger = LogManager.getLogger(CodeInspector.class);


    public static void main(String[] args) {
        int status = 0;
        try {
            final CliOptions cliOptions = new CliOptions();
            final CommandLine commandLine = new CommandLine(cliOptions);
            commandLine.setUsageHelpWidth(CliOptions.HELP_WIDTH);
            commandLine.setCaseInsensitiveEnumValuesAllowed(true);
            CommandLine.ParseResult parseResult = commandLine.parseArgs(args);
            if (!CommandLine.printHelpIfRequested(parseResult)) {
                log.info("start enter log");
                Properties properties = PropertiesUtils.loadProperties("config.properties");
                PropertiesUtils.configAndUpdate(properties);
                log.info("start enter log");
                if (cliOptions.getConfigurationFile() == null || cliOptions.getConfigurationFile().isEmpty()) {
                    cliOptions.setConfigurationFile(PropertiesUtils.ROOT_DIR + "/config/devkit_checkstyle.xml");
                }
                DataBasePreLoad.preload(properties);
                CheckStyleWrapper.checkStyle(cliOptions);
            }
        } catch (CommandLine.ParameterException ex) {
            status = -1;
            log.error("error", ex);
            System.err.println(ex.getMessage());
            System.err.println("Usage: code-inspector [OPTIONS]... FILES...");
            System.err.println("Try 'code-inspector --help' for more information.");
        } catch (Exception ex) {
            status = -1;
            log.error("error", ex);
        }
        Runtime.getRuntime().exit(status);
    }
}
