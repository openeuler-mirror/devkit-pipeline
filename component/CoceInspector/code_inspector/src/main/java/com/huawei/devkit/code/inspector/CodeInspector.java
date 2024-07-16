package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.entity.CliOptions;
import com.huawei.devkit.code.inspector.perload.DataBasePreLoad;
import com.huawei.devkit.code.inspector.utils.PropertiesUtils;
import com.huawei.devkit.code.inspector.wrappers.CheckStyleWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.Properties;

public class CodeInspector {
    private static final int EXIT_WITH_INVALID_USER_INPUT_CODE = -1;

    private static final Logger logger = LogManager.getLogger(CodeInspector.class);


    public static void main(String[] args) {
        int status = 0;
        try {
            logger.info("start enter log");
            final CliOptions cliOptions = new CliOptions();
            final CommandLine commandLine = new CommandLine(cliOptions);
            commandLine.setUsageHelpWidth(CliOptions.HELP_WIDTH);
            commandLine.setCaseInsensitiveEnumValuesAllowed(true);
            commandLine.parseArgs(args);
            Properties properties = PropertiesUtils.loadProperties("config.properties");
            PropertiesUtils.configAndUpdate(properties);
            DataBasePreLoad.preload(properties);
            CheckStyleWrapper.checkStyle(cliOptions);
        } catch (CommandLine.ParameterException ex) {
            status = -1;
            logger.error("error", ex);
            System.err.println(ex.getMessage());
            System.err.println("Usage: checkstyle [OPTIONS]... FILES...");
            System.err.println("Try 'checkstyle --help' for more information.");
        } catch (Exception ex) {
            status = -1;
            logger.error("error", ex);
        }
//        Runtime.getRuntime().exit(status);
    }
}
