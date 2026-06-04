/*
 * Copyright 2026 Daniel Heid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drjekyll.adocfmt.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.drjekyll.adocfmt.AsciidocFormatter;
import org.drjekyll.adocfmt.AsciidocFormatterConfig;
import org.drjekyll.adocfmt.UnsupportedLineEndingException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Command-line interface for the AsciiDoc formatter.
 *
 * <p>Accepts one or more AsciiDoc files as arguments and applies the configured transformations.
 * When no files are given, input is read from {@code stdin} and the result is written to {@code
 * stdout}.
 *
 * <p>Use {@code -c} / {@code --check} to verify formatting without modifying files, or {@code -w} /
 * {@code --write} to write changes back in place.
 */
@Command(
    name = "adocfmt",
    mixinStandardHelpOptions = true,
    version = "0.1.0",
    description = "An opinionated AsciiDoc formatter.")
public class AsciidocFormatterCli implements Callable<Integer> {

  @Option(
      names = {"-c", "--check"},
      description =
          "Check if files are formatted without modifying them. Exit with 1 if any file would change.")
  private boolean check;

  @Option(
      names = {"-w", "--write"},
      description = "Write formatted content back to files.")
  private boolean write;

  @Option(
      names = {"-nsh", "--normalize-setext-headings"},
      defaultValue = "true",
      description = "Normalize setext headings to ATX. Default: ${DEFAULT-VALUE}")
  private boolean normalizeSetextHeadings;

  @Option(
      names = {"-cbl", "--collapse-blank-lines"},
      defaultValue = "true",
      description = "Collapse consecutive blank lines. Default: ${DEFAULT-VALUE}")
  private boolean collapseBlankLines;

  @Option(
      names = {"-ols", "--one-sentence-per-line"},
      defaultValue = "true",
      description = "One sentence per line. Default: ${DEFAULT-VALUE}")
  private boolean oneSentencePerLine;

  @Option(
      names = {"-nbd", "--normalize-block-delimiters"},
      defaultValue = "true",
      description = "Normalize block delimiters. Default: ${DEFAULT-VALUE}")
  private boolean normalizeBlockDelimiters;

  @Option(
      names = {"-rthe", "--remove-trailing-header-equals"},
      defaultValue = "true",
      description = "Remove trailing header equals sign. Default: ${DEFAULT-VALUE}")
  private boolean removeTrailingHeaderEquals;

  @Option(
      names = {"-tc", "--title-case"},
      defaultValue = "false",
      description = "Apply title case to headings. Default: ${DEFAULT-VALUE}")
  private boolean titleCase;

  @Option(
      names = {"-rtrw", "--remove-trailing-whitespace"},
      defaultValue = "true",
      description = "Remove trailing whitespace. Default: ${DEFAULT-VALUE}")
  private boolean removeTrailingWhitespace;

  @Option(
      names = {"-nlb", "--normalize-list-bullets"},
      defaultValue = "false",
      description = "Normalize list bullets. Default: ${DEFAULT-VALUE}")
  private boolean normalizeListBullets;

  @Option(
      names = {"-nolm", "--normalize-ordered-list-markers"},
      defaultValue = "false",
      description = "Normalize ordered list markers. Default: ${DEFAULT-VALUE}")
  private boolean normalizeOrderedListMarkers;

  @Option(
      names = {"-ehlb", "--ensure-heading-blank-lines"},
      defaultValue = "true",
      description = "Ensure blank lines around headings. Default: ${DEFAULT-VALUE}")
  private boolean ensureHeadingBlankLines;

  @Option(
      names = {"-esd", "--ensure-source-delimiters"},
      defaultValue = "false",
      description = "Ensure source delimiters. Default: ${DEFAULT-VALUE}")
  private boolean ensureSourceDelimiters;

  @Parameters(description = "Files to format. If empty, reads from stdin.")
  private List<Path> files = new ArrayList<>();

  /**
   * Main entry point for the CLI.
   *
   * @param args command-line arguments forwarded to picocli
   */
  public static void main(String[] args) {
    int exitCode = new CommandLine(new AsciidocFormatterCli()).execute(args);
    System.exit(exitCode);
  }

  /**
   * Executes the formatter according to the parsed options and file arguments.
   *
   * @return {@code 0} on success, {@code 1} if {@code --check} detected unformatted files, or
   *     {@code 2} if an error occurred while processing a file
   */
  @Override
  public Integer call() {
    AsciidocFormatterConfig config = createConfig();

    if (files.isEmpty()) {
      return handleStdin(config);
    }

    boolean anyChanged = false;
    for (Path file : files) {
      try {
        if (!Files.exists(file)) {
          System.err.println("File not found: " + file);
          return 2;
        }
        if (Files.isDirectory(file)) {
          System.err.println("Not a file: " + file);
          return 2;
        }
        if (!Files.isReadable(file)) {
          System.err.println("Not readable: " + file);
          return 2;
        }
        if (write && !Files.isWritable(file)) {
          System.err.println("Not writable: " + file);
          return 2;
        }
        if (processFile(file, config)) {
          anyChanged = true;
        }
      } catch (IOException | UnsupportedLineEndingException e) {
        System.err.println("Error processing " + file + ": " + e.getMessage());
        return 2;
      }
    }

    if (check && anyChanged) {
      return 1;
    }

    return 0;
  }

  private AsciidocFormatterConfig createConfig() {
    AsciidocFormatterConfig.AsciidocFormatterConfigBuilder builder =
        AsciidocFormatterConfig.builder()
            .normalizeSetextHeadings(normalizeSetextHeadings)
            .collapseConsecutiveBlankLines(collapseBlankLines)
            .oneSentencePerLine(oneSentencePerLine)
            .normalizeBlockDelimiters(normalizeBlockDelimiters)
            .removeTrailingHeaderEqualsSign(removeTrailingHeaderEquals)
            .titleCase(titleCase)
            .removeTrailingWhitespace(removeTrailingWhitespace)
            .normalizeListBullets(normalizeListBullets)
            .normalizeOrderedListMarkers(normalizeOrderedListMarkers)
            .ensureHeadingBlankLines(ensureHeadingBlankLines)
            .ensureSourceDelimiters(ensureSourceDelimiters);
    return builder.build();
  }

  private int handleStdin(AsciidocFormatterConfig config) {
    try {
      new AsciidocFormatter(config).format(System.in, System.out);
    } catch (IOException | UnsupportedLineEndingException e) {
      System.err.println("Error processing input: " + e.getMessage());
      return 2;
    }
    return 0;
  }

  private boolean processFile(Path file, AsciidocFormatterConfig config)
      throws IOException, UnsupportedLineEndingException {
    byte[] original = Files.readAllBytes(file);
    byte[] formatted = new AsciidocFormatter(config).format(original);

    boolean changed = !Arrays.equals(original, formatted);

    if (changed) {
      if (write) {
        Files.write(file, formatted);
        System.out.println("Formatted " + file);
      } else if (check) {
        System.out.println("Would format " + file);
      } else {
        if (files.size() == 1) {
          System.out.write(formatted);
        } else {
          System.out.println("Would format " + file + " (use -w to write)");
        }
      }
    }
    return changed;
  }
}
