/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector.entity;

import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import lombok.Data;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * CliOptions
 *
 * @since 2024-07-15
 */
@Data
public class CliOptions {
    /**
     * Width of CLI help option.
     */
    public static final int HELP_WIDTH = 100;

    /**
     * The default number of threads to use for checker and the tree walker.
     */
    private static final int DEFAULT_THREAD_COUNT = 1;

    /**
     * Name for the moduleConfig attribute 'tabWidth'.
     */
    public static final String ATTRIB_TAB_WIDTH_NAME = "tabWidth";

    /**
     * Default output format.
     */
    private static final OutputStyle DEFAULT_OUTPUT_FORMAT = OutputStyle.PLAIN;

    /**
     * Option name for output format.
     */
    private static final String OUTPUT_FORMAT_OPTION = "-f";

    /**
     * The checker threads number.
     * This option has been skipped for CLI options intentionally.
     */
    public static final int CHECKER_THREADS_NUMBER = DEFAULT_THREAD_COUNT;

    /**
     * The tree walker threads number.
     */
    public static final int TREE_WALKER_THREADS_NUMBER = DEFAULT_THREAD_COUNT;

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
    private OutputStyle format = DEFAULT_OUTPUT_FORMAT;

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
    public List<Pattern> getExclusions() {
        final List<Pattern> result = exclude.stream()
                .map(File::getAbsolutePath)
                .map(Pattern::quote)
                .map(pattern -> Pattern.compile("^" + pattern + "$"))
                .collect(Collectors.toCollection(ArrayList::new));
        result.addAll(excludeRegex);
        return result;
    }
}
