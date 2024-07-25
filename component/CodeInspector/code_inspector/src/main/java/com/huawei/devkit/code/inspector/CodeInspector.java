package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.entity.CliOptions;
import com.huawei.devkit.code.inspector.perload.DataBasePreLoad;
import com.huawei.devkit.code.inspector.utils.PropertiesUtils;
import com.huawei.devkit.code.inspector.wrappers.CheckStyleWrapper;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Locale;
import java.util.Properties;

@Slf4j
public class CodeInspector {

    public static void main(String[] args) {
        final CliOptions cliOptions = new CliOptions();
        final CommandLine commandLine = new CommandLine(cliOptions);
        try {
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
                CheckStyleWrapper.checkStyle(cliOptions);
            }
        } catch (CommandLine.ParameterException ex) {
            log.error("error", ex);
            System.err.println(ex.getMessage());
            commandLine.usage(System.err);
        } catch (Exception ex) {
            log.error("error", ex);
        }
    }
}
