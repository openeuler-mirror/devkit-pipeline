/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector.wrappers;

import com.huawei.devkit.code.inspector.entity.CliOptions;
import com.huawei.devkit.code.inspector.entity.OutputStyle;
import com.huawei.devkit.code.inspector.listener.DataBaseLogger;
import com.puppycrawl.tools.checkstyle.AbstractAutomaticBean;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.Definitions;
import com.puppycrawl.tools.checkstyle.LocalizedMessage;
import com.puppycrawl.tools.checkstyle.Main;
import com.puppycrawl.tools.checkstyle.ModuleFactory;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.ThreadModeSettings;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.RootModule;
import com.puppycrawl.tools.checkstyle.utils.ChainedPropertyUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * CheckStyleWrapper
 *
 * @since 2024-07-11
 */

@Slf4j
public class CheckStyleWrapper {

    /**
     * A key pointing to the error counter
     * message in the "messages.properties" file.
     */
    public static final String ERROR_COUNTER = "Main.errorCounter";
    /**
     * A key pointing to the load properties exception
     * message in the "messages.properties" file.
     */
    public static final String LOAD_PROPERTIES_EXCEPTION = "Main.loadProperties";


    /**
     * Loops over the files specified checking them for errors. The exit code
     * is the number of errors found in all the files.
     *
     * @throws IOException if there is a problem with files access
     **/
    public static int checkStyle(CliOptions cliOptions) throws IOException, CheckstyleException {
        int errorCounter = 0;
        final List<File> filesToProcess = getFilesToProcess(cliOptions);
        errorCounter = runCheckstyle(cliOptions, filesToProcess);
        if (errorCounter > 0) {
            final LocalizedMessage errorCounterViolation = new LocalizedMessage(
                Definitions.CHECKSTYLE_BUNDLE, Main.class,
                ERROR_COUNTER, String.valueOf(errorCounter));
            log.error(errorCounterViolation.getMessage());
        }
        return errorCounter;

    }

    /**
     * Determines the files to process.
     *
     * @param options the user-specified options
     * @return list of files to process
     */
    private static List<File> getFilesToProcess(CliOptions options) throws IOException {
        final List<Pattern> patternsToExclude = options.getExclusions();

        final List<File> result = new LinkedList<>();
        for (File file : options.getFiles()) {
            if (file.exists()) {
                File real = file.getCanonicalFile();
                result.addAll(listFiles(real, patternsToExclude));
            }
        }
        return result;
    }

    /**
     * Traverses a specified node looking for files to check. Found files are added to
     * a specified list. Subdirectories are also traversed.
     *
     * @param node              the node to process
     * @param patternsToExclude The list of patterns to exclude from searching or being added as
     *                          files.
     * @return found files
     */
    private static List<File> listFiles(File node, List<Pattern> patternsToExclude) {
        // could be replaced with org.apache.commons.io.FileUtils.list() method
        // if only we add commons-io library
        final List<File> result = new LinkedList<>();

        if (node.canRead() && !isPathExcluded(node.getAbsolutePath(), patternsToExclude)) {
            if (node.isDirectory()) {
                final File[] files = node.listFiles();
                // listFiles() can return null, so we need to check it
                if (files != null) {
                    for (File element : files) {
                        result.addAll(listFiles(element, patternsToExclude));
                    }
                }
            } else if (node.isFile()) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * Checks if a directory/file {@code path} should be excluded based on if it matches one of the
     * patterns supplied.
     *
     * @param path              The path of the directory/file to check
     * @param patternsToExclude The collection of patterns to exclude from searching
     *                          or being added as files.
     * @return True if the directory/file matches one of the patterns.
     */
    private static boolean isPathExcluded(String path, Iterable<Pattern> patternsToExclude) {
        boolean result = false;

        for (Pattern pattern : patternsToExclude) {
            if (pattern.matcher(path).find()) {
                result = true;
                break;
            }
        }

        return result;
    }


    /**
     * Executes required Checkstyle actions based on passed parameters.
     *
     * @param options        user-specified options
     * @param filesToProcess the list of files whose style to check
     * @return number of violations of ERROR level
     * @throws IOException         when output file could not be found
     * @throws CheckstyleException when properties file could not be loaded
     */
    private static int runCheckstyle(CliOptions options, List<File> filesToProcess)
        throws CheckstyleException, IOException {
        final Properties props;

        if (options.getPropertiesFile() == null) {
            props = System.getProperties();
        } else {
            props = loadProperties(options.getPropertiesFile());
        }

        // create a configuration
        final ThreadModeSettings multiThreadModeSettings =
            new ThreadModeSettings(CliOptions.CHECKER_THREADS_NUMBER,
                CliOptions.TREE_WALKER_THREADS_NUMBER);

        final ConfigurationLoader.IgnoredModulesOptions ignoredModulesOptions;
        if (options.isExecuteIgnoredModules()) {
            ignoredModulesOptions = ConfigurationLoader.IgnoredModulesOptions.EXECUTE;
        } else {
            ignoredModulesOptions = ConfigurationLoader.IgnoredModulesOptions.OMIT;
        }

        final Configuration config = ConfigurationLoader.loadConfiguration(
            options.getConfigurationFile(), new PropertiesExpander(props),
            ignoredModulesOptions, multiThreadModeSettings);

        // create RootModule object and run it
        final int errorCounter;
        final ClassLoader moduleClassLoader = Checker.class.getClassLoader();
        final RootModule rootModule = getRootModule(config.getName(), moduleClassLoader);

        try {
            final AuditListener listener = createListener(options.getFormat(), options.getOutputPath());
            rootModule.setModuleClassLoader(moduleClassLoader);
            rootModule.configure(config);
            DataBaseLogger baseListener = new DataBaseLogger("");
            rootModule.addListener(listener);
            rootModule.addListener(baseListener);

            // run RootModule
            errorCounter = rootModule.process(filesToProcess);
        } finally {
            rootModule.destroy();
        }

        return errorCounter;
    }

    /**
     * Loads properties from a File.
     *
     * @param file the properties file
     * @return the properties in file
     * @throws CheckstyleException when could not load properties file
     */
    private static Properties loadProperties(File file)
        throws CheckstyleException {
        final Properties properties = new Properties();

        try (InputStream stream = Files.newInputStream(file.toPath())) {
            properties.load(stream);
        } catch (final IOException ex) {
            final LocalizedMessage loadPropertiesExceptionMessage = new LocalizedMessage(
                Definitions.CHECKSTYLE_BUNDLE, CheckStyleWrapper.class,
                LOAD_PROPERTIES_EXCEPTION, file.getAbsolutePath());
            throw new CheckstyleException(loadPropertiesExceptionMessage.getMessage(), ex);
        }

        return ChainedPropertyUtil.getResolvedProperties(properties);
    }

    /**
     * Creates a new instance of the root module that will control and run
     * Checkstyle.
     *
     * @param name              The name of the module. This will either be a short name that
     *                          will have to be found or the complete package name.
     * @param moduleClassLoader Class loader used to load the root module.
     * @return The new instance of the root module.
     * @throws CheckstyleException if no module can be instantiated from name
     */
    private static RootModule getRootModule(String name, ClassLoader moduleClassLoader)
        throws CheckstyleException {
        final ModuleFactory factory = new PackageObjectFactory(
            Checker.class.getPackage().getName(), moduleClassLoader);

        return (RootModule) factory.createModule(name);
    }

    /**
     * This method creates in AuditListener an open stream for validation data, it must be
     * closed by {@link RootModule} (default implementation is {@link Checker}) by calling
     * {@link AuditListener#auditFinished(AuditEvent)}.
     *
     * @param format         format of the audit listener
     * @param outputLocation the location of output
     * @return a fresh new {@code AuditListener}
     * @throws IOException when provided output location is not found
     */
    private static AuditListener createListener(OutputStyle format, Path outputLocation)
        throws IOException {
        final OutputStream out = getOutputStream(outputLocation);
        final AbstractAutomaticBean.OutputStreamOptions closeOutputStreamOption =
            getOutputStreamOptions(outputLocation);
        return format.createListener(out, closeOutputStreamOption);
    }

    /**
     * Create output stream or return System.out.
     *
     * @param outputPath output location
     * @return output stream
     * @throws IOException might happen
     */
    private static OutputStream getOutputStream(Path outputPath) throws IOException {
        final OutputStream result;
        if (outputPath == null) {
            result = System.out;
        } else {
            result = Files.newOutputStream(outputPath);
        }
        return result;
    }

    /**
     * Create {@link AbstractAutomaticBean.OutputStreamOptions} for the given location.
     *
     * @param outputPath output location
     * @return output stream options
     */
    private static AbstractAutomaticBean.OutputStreamOptions getOutputStreamOptions(Path outputPath) {
        final AbstractAutomaticBean.OutputStreamOptions result;
        if (outputPath == null) {
            result = AbstractAutomaticBean.OutputStreamOptions.NONE;
        } else {
            result = AbstractAutomaticBean.OutputStreamOptions.CLOSE;
        }
        return result;
    }

}
