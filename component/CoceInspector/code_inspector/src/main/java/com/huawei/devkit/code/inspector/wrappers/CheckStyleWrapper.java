/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector.wrappers;

import com.puppycrawl.tools.checkstyle.AbstractAutomaticBean;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.Definitions;
import com.puppycrawl.tools.checkstyle.LocalizedMessage;
import com.puppycrawl.tools.checkstyle.Main;
import com.puppycrawl.tools.checkstyle.ModuleFactory;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.SarifLogger;
import com.puppycrawl.tools.checkstyle.ThreadModeSettings;
import com.puppycrawl.tools.checkstyle.XMLLogger;
import com.puppycrawl.tools.checkstyle.XpathFileGeneratorAstFilter;
import com.puppycrawl.tools.checkstyle.XpathFileGeneratorAuditListener;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.RootModule;
import com.puppycrawl.tools.checkstyle.utils.ChainedPropertyUtil;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * CheckStyleWrapper
 *
 * @since 2024-07-11
 */
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

    private static final int EXIT_WITH_INVALID_USER_INPUT_CODE = -1;
    private static final int EXIT_WITH_CHECKSTYLE_EXCEPTION_CODE = -2;


    /**
     * Loops over the files specified checking them for errors. The exit code
     * is the number of errors found in all the files.
     *
     * @param args the command line arguments.
     * @throws IOException if there is a problem with files access
     * @noinspection UseOfSystemOutOrSystemErr, CallToPrintStackTrace, CallToSystemExit
     * @noinspectionreason UseOfSystemOutOrSystemErr - driver class for Checkstyle requires
     * usage of System.out and System.err
     * @noinspectionreason CallToPrintStackTrace - driver class for Checkstyle must be able to
     * show all details in case of failure
     * @noinspectionreason CallToSystemExit - driver class must call exit
     **/
    public static void main(String... args) throws IOException {

        final CliOptions cliOptions = new CliOptions();
        final CommandLine commandLine = new CommandLine(cliOptions);
        commandLine.setUsageHelpWidth(CliOptions.HELP_WIDTH);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);

        int exitStatus = 0;
        int errorCounter = 0;
        try {
            commandLine.parseArgs(args);
            final List<File> filesToProcess = getFilesToProcess(cliOptions);
            errorCounter = runCheckstyle(cliOptions, filesToProcess);
        } catch (CommandLine.ParameterException ex) {
            exitStatus = EXIT_WITH_INVALID_USER_INPUT_CODE;
            System.err.println(ex.getMessage());
            System.err.println("Usage: checkstyle [OPTIONS]... FILES...");
            System.err.println("Try 'checkstyle --help' for more information.");
        } catch (CheckstyleException ex) {
            exitStatus = EXIT_WITH_CHECKSTYLE_EXCEPTION_CODE;
            errorCounter = 1;
            ex.printStackTrace();
        } finally {
            // return exit code base on validation of Checker
            if (errorCounter > 0) {
                final LocalizedMessage errorCounterViolation = new LocalizedMessage(
                        Definitions.CHECKSTYLE_BUNDLE, Main.class,
                        ERROR_COUNTER, String.valueOf(errorCounter));
                // print error count statistic to error output stream,
                // output stream might be used by validation report content
                System.err.println(errorCounterViolation.getMessage());
            }
        }
    }

    /**
     * Determines the files to process.
     *
     * @param options the user-specified options
     * @return list of files to process
     */
    private static List<File> getFilesToProcess(CliOptions options) {
        final List<Pattern> patternsToExclude = options.getExclusions();

        final List<File> result = new LinkedList<>();
        for (File file : options.files) {
            result.addAll(listFiles(file, patternsToExclude));
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
        // setup the properties
        final Properties props;

        if (options.propertiesFile == null) {
            props = System.getProperties();
        } else {
            props = loadProperties(options.propertiesFile);
        }

        // create a configuration
        final ThreadModeSettings multiThreadModeSettings =
                new ThreadModeSettings(CliOptions.CHECKER_THREADS_NUMBER,
                        CliOptions.TREE_WALKER_THREADS_NUMBER);

        final ConfigurationLoader.IgnoredModulesOptions ignoredModulesOptions;
        if (options.executeIgnoredModules) {
            ignoredModulesOptions = ConfigurationLoader.IgnoredModulesOptions.EXECUTE;
        } else {
            ignoredModulesOptions = ConfigurationLoader.IgnoredModulesOptions.OMIT;
        }

        final Configuration config = ConfigurationLoader.loadConfiguration(
                options.configurationFile, new PropertiesExpander(props),
                ignoredModulesOptions, multiThreadModeSettings);

        // create RootModule object and run it
        final int errorCounter;
        final ClassLoader moduleClassLoader = Checker.class.getClassLoader();
        final RootModule rootModule = getRootModule(config.getName(), moduleClassLoader);

        try {
            final AuditListener listener;
            if (options.generateXpathSuppressionsFile) {
                // create filter to print generated xpath suppressions file
                final Configuration treeWalkerConfig = getTreeWalkerConfig(config);
                if (treeWalkerConfig != null) {
                    final DefaultConfiguration moduleConfig =
                            new DefaultConfiguration(
                                    XpathFileGeneratorAstFilter.class.getName());
                    moduleConfig.addProperty(CliOptions.ATTRIB_TAB_WIDTH_NAME,
                            String.valueOf(options.tabWidth));
                    ((DefaultConfiguration) treeWalkerConfig).addChild(moduleConfig);
                }

                listener = new XpathFileGeneratorAuditListener(getOutputStream(options.outputPath),
                        getOutputStreamOptions(options.outputPath));
            } else {
                listener = createListener(options.format, options.outputPath);
            }

            rootModule.setModuleClassLoader(moduleClassLoader);
            rootModule.configure(config);
            rootModule.addListener(listener);

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
     * Returns {@code TreeWalker} module configuration.
     *
     * @param config The configuration object.
     * @return The {@code TreeWalker} module configuration.
     */
    private static Configuration getTreeWalkerConfig(Configuration config) {
        Configuration result = null;

        final Configuration[] children = config.getChildren();
        for (Configuration child : children) {
            if ("TreeWalker".equals(child.getName())) {
                result = child;
                break;
            }
        }
        return result;
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
    private static AuditListener createListener(OutputFormat format, Path outputLocation)
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
     * @noinspection UseOfSystemOutOrSystemErr
     * @noinspectionreason UseOfSystemOutOrSystemErr - driver class for Checkstyle requires
     * usage of System.out and System.err
     */
    @SuppressWarnings("resource")
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

    /**
     * Enumeration over the possible output formats.
     *
     * @noinspection PackageVisibleInnerClass
     * @noinspectionreason PackageVisibleInnerClass - we keep this enum package visible for tests
     */
    enum OutputFormat {
        /**
         * XML output format.
         */
        XML,
        /**
         * SARIF output format.
         */
        SARIF,
        /**
         * Plain output format.
         */
        PLAIN;

        /**
         * Returns a new AuditListener for this OutputFormat.
         *
         * @param out     the output stream
         * @param options the output stream options
         * @return a new AuditListener for this OutputFormat
         * @throws IOException if there is any IO exception during logger initialization
         */
        public AuditListener createListener(
                OutputStream out,
                AbstractAutomaticBean.OutputStreamOptions options) throws IOException {
            final AuditListener result;
            if (this == XML) {
                result = new XMLLogger(out, options);
            } else if (this == SARIF) {
                result = new SarifLogger(out, options);
            } else {
                result = new DefaultLogger(out, options);
            }
            return result;
        }

        /**
         * Returns the name in lowercase.
         *
         * @return the enum name in lowercase
         */
        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Command line options.
     *
     * @noinspection unused, FieldMayBeFinal, CanBeFinal,
     * MismatchedQueryAndUpdateOfCollection, LocalCanBeFinal
     * @noinspectionreason FieldMayBeFinal - usage of picocli requires
     * suppression of above inspections
     * @noinspectionreason CanBeFinal - usage of picocli requires
     * suppression of above inspections
     * @noinspectionreason MismatchedQueryAndUpdateOfCollection - list of files is gathered and used
     * via reflection by picocli library
     * @noinspectionreason LocalCanBeFinal - usage of picocli requires
     * suppression of above inspections
     */
    @CommandLine.Command(name = "checkstyle", description = "Checkstyle verifies that the specified "
            + "source code files adhere to the specified rules. By default, violations are "
            + "reported to standard out in plain format. Checkstyle requires a configuration "
            + "XML file that configures the checks to apply.",
            mixinStandardHelpOptions = true)
    private static final class CliOptions {

        /**
         * Width of CLI help option.
         */
        private static final int HELP_WIDTH = 100;

        /**
         * The default number of threads to use for checker and the tree walker.
         */
        private static final int DEFAULT_THREAD_COUNT = 1;

        /**
         * Name for the moduleConfig attribute 'tabWidth'.
         */
        private static final String ATTRIB_TAB_WIDTH_NAME = "tabWidth";

        /**
         * Default output format.
         */
        private static final OutputFormat DEFAULT_OUTPUT_FORMAT = OutputFormat.PLAIN;

        /**
         * Option name for output format.
         */
        private static final String OUTPUT_FORMAT_OPTION = "-f";

        /**
         * The checker threads number.
         * This option has been skipped for CLI options intentionally.
         */
        private static final int CHECKER_THREADS_NUMBER = DEFAULT_THREAD_COUNT;

        /**
         * The tree walker threads number.
         */
        private static final int TREE_WALKER_THREADS_NUMBER = DEFAULT_THREAD_COUNT;

        /**
         * List of file to validate.
         */
        @CommandLine.Parameters(arity = "1..*", description = "One or more source files to verify")
        private List<File> files;

        /**
         * Config file location.
         */
        @CommandLine.Option(names = "-c", description = "Specifies the location of the file that defines"
                + " the configuration modules. The location can either be a filesystem location"
                + ", or a name passed to the ClassLoader.getResource() method.")
        private String configurationFile;

        /**
         * Output file location.
         */
        @CommandLine.Option(names = "-o", description = "Sets the output file. Defaults to stdout.")
        private Path outputPath;

        /**
         * Properties file location.
         */
        @CommandLine.Option(names = "-p", description = "Sets the property files to load.")
        private File propertiesFile;

        /**
         * Tab character length.
         *
         * @noinspection CanBeFinal
         * @noinspectionreason CanBeFinal - we use picocli, and it uses
         * reflection to manage such fields
         */
        @CommandLine.Option(names = {"-w", "--tabWidth"},
                description = "Sets the length of the tab character. "
                        + "Used only with -s option. Default value is ${DEFAULT-VALUE}.")
        private int tabWidth = CommonUtil.DEFAULT_TAB_WIDTH;

        /**
         * Switch whether to generate suppressions file or not.
         */
        @CommandLine.Option(names = {"-g", "--generate-xpath-suppression"},
                description = "Generates to output a suppression xml to use to suppress all "
                        + "violations from user's config. Instead of printing every violation, "
                        + "all violations will be catched and single suppressions xml file will "
                        + "be printed out. Used only with -c option. Output "
                        + "location can be specified with -o option.")
        private boolean generateXpathSuppressionsFile;

        /**
         * Output format.
         *
         * @noinspection CanBeFinal
         * @noinspectionreason CanBeFinal - we use picocli, and it uses
         * reflection to manage such fields
         */
        @CommandLine.Option(names = "-f",
                description = "Specifies the output format. Valid values: "
                        + "${COMPLETION-CANDIDATES} for XMLLogger, SarifLogger, "
                        + "and DefaultLogger respectively. Defaults to ${DEFAULT-VALUE}.")
        private OutputFormat format = DEFAULT_OUTPUT_FORMAT;

        /**
         * Option that allows users to specify a list of paths to exclude.
         *
         * @noinspection CanBeFinal
         * @noinspectionreason CanBeFinal - we use picocli, and it uses
         * reflection to manage such fields
         */
        @CommandLine.Option(names = {"-e", "--exclude"},
                description = "Directory/file to exclude from CheckStyle. The path can be the "
                        + "full, absolute path, or relative to the current path. Multiple "
                        + "excludes are allowed.")
        private List<File> exclude = new ArrayList<>();

        /**
         * Option that allows users to specify a regex of paths to exclude.
         *
         * @noinspection CanBeFinal
         * @noinspectionreason CanBeFinal - we use picocli, and it uses
         * reflection to manage such fields
         */
        @CommandLine.Option(names = {"-x", "--exclude-regexp"},
                description = "Directory/file pattern to exclude from CheckStyle. Multiple "
                        + "excludes are allowed.")
        private List<Pattern> excludeRegex = new ArrayList<>();

        /**
         * Switch whether to execute ignored modules or not.
         */
        @CommandLine.Option(names = {"-E", "--executeIgnoredModules"},
                description = "Allows ignored modules to be run.")
        private boolean executeIgnoredModules;

        /**
         * Gets the list of exclusions provided through the command line arguments.
         *
         * @return List of exclusion patterns.
         */
        private List<Pattern> getExclusions() {
            final List<Pattern> result = exclude.stream()
                    .map(File::getAbsolutePath)
                    .map(Pattern::quote)
                    .map(pattern -> Pattern.compile("^" + pattern + "$"))
                    .collect(Collectors.toCollection(ArrayList::new));
            result.addAll(excludeRegex);
            return result;
        }
    }
}
