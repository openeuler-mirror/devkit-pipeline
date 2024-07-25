/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector.entity;

import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.Command;

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
@Command(name = "code-inspector", version = "1.0.0", mixinStandardHelpOptions = true)
public class CliOptions {
    /**
     * Width of CLI help option.
     */
    public static final int HELP_WIDTH = 120;

    /**
     * The default number of threads to use for checker and the tree walker.
     */
    private static final int DEFAULT_THREAD_COUNT = 1;

    /**
     * Default output format.
     */
    private static final OutputStyle DEFAULT_OUTPUT_FORMAT = OutputStyle.PLAIN;


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
    @CommandLine.Parameters(arity = "1..*", description = "One or more source files to verify.")
    private List<File> files;

    /**
     * Config file location.
     */
    @CommandLine.Option(names = "-c", description = "Specifies the location of the file that defines"
        + " the configuration modules.By default, the devkit_checkstyle.xml in the config directory is used.")
    private String configurationFile;

    /**
     * Output file location.
     */
    @CommandLine.Option(names = "-o", description = "Sets the output file. Defaults to stdout.")
    private Path outputPath;

    /**
     * Properties file location.
     */
    private File propertiesFile;

    /**
     * Output format.
     */
    @CommandLine.Option(names = "-f", description = "Specifies the output format. Valid values: "
        + "${COMPLETION-CANDIDATES}. Defaults to ${DEFAULT-VALUE}.At the same time, it will be recorded to "
        + "the sqlite database in the config directory.")
    private OutputStyle format = DEFAULT_OUTPUT_FORMAT;


    @CommandLine.Option(names = {"-e", "--exclude"},
        description = "Directory/file to exclude from CodeInspector. The path can be the "
            + "full, absolute path, or relative to the current path. Multiple "
            + "excludes are allowed.")
    private List<File> exclude = new ArrayList<>();


    @CommandLine.Option(names = {"-x", "--exclude-regexp"},
        description = "Directory/file pattern to exclude from CodeInspector. Multiple "
            + "excludes are allowed.")
    private List<Pattern> excludeRegex = new ArrayList<>();

    /**
     * 当配置规则的属性severity为ignore时，是否还执行。默认不执行
     */
    private boolean executeIgnoredModules;

    /**
     * 合并exclude和excludeRegex
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
