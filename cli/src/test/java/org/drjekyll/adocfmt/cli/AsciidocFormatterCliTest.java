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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class AsciidocFormatterCliTest {

  // Content that is already fully formatted (idempotent under all default options).
  // No trailing newline — the formatter strips trailing newlines via split(limit=0).
  private static final String FORMATTED = "== Title";

  // Content that needs the default --remove-trailing-header-equals transformation.
  private static final String UNFORMATTED = "== title ==";

  @TempDir Path tempDir;

  // -------------------------------------------------------------------------
  // --check
  // -------------------------------------------------------------------------

  @Test
  void checkUnformattedFileReturnsExitCode1() throws Exception {
    Path file = writeFile("test.adoc", UNFORMATTED);
    assertThat(execute("--check", file.toString())).isEqualTo(1);
  }

  @Test
  void checkFormattedFileReturnsExitCode0() throws Exception {
    Path file = writeFile("test.adoc", FORMATTED);
    assertThat(execute("--check", file.toString())).isEqualTo(0);
  }

  @Test
  void checkShortAliasWorks() throws Exception {
    Path file = writeFile("test.adoc", UNFORMATTED);
    assertThat(execute("-c", file.toString())).isEqualTo(1);
  }

  @Test
  void checkMultipleFilesAllFormattedReturnsExitCode0() throws Exception {
    Path a = writeFile("a.adoc", FORMATTED);
    Path b = writeFile("b.adoc", "== Section");
    assertThat(execute("--check", a.toString(), b.toString())).isEqualTo(0);
  }

  @Test
  void checkMultipleFilesOneUnformattedReturnsExitCode1() throws Exception {
    Path a = writeFile("a.adoc", FORMATTED);
    Path b = writeFile("b.adoc", UNFORMATTED);
    assertThat(execute("--check", a.toString(), b.toString())).isEqualTo(1);
  }

  @Test
  void checkOnReadOnlyFileSucceeds() throws Exception {
    // Bug fix verification: writability check must not block --check
    Path file = writeFile("readonly.adoc", FORMATTED);
    assumeTrue(file.toFile().setWritable(false), "cannot make file read-only on this platform");
    try {
      assertThat(execute("--check", file.toString())).isEqualTo(0);
    } finally {
      file.toFile().setWritable(true);
    }
  }

  // -------------------------------------------------------------------------
  // --write
  // -------------------------------------------------------------------------

  @Test
  void writeFormatsFileInPlace() throws Exception {
    Path file = writeFile("test.adoc", UNFORMATTED);
    assertThat(execute("--write", file.toString())).isEqualTo(0);
    assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("== title");
  }

  @Test
  void writeWithTitleCaseFormatsHeading() throws Exception {
    Path file = writeFile("test.adoc", "== title ==");
    assertThat(execute("--write", "--title-case", file.toString())).isEqualTo(0);
    assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("== Title");
  }

  @Test
  void writeShortAliasWorks() throws Exception {
    Path file = writeFile("test.adoc", "== title ==");
    assertThat(execute("-w", "-tc", file.toString())).isEqualTo(0);
    assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("== Title");
  }

  @Test
  void writeAlreadyFormattedFileReturnsExitCode0AndLeavesFileUnchanged() throws Exception {
    Path file = writeFile("test.adoc", FORMATTED);
    assertThat(execute("--write", file.toString())).isEqualTo(0);
    assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo(FORMATTED);
  }

  @Test
  void writeMultipleFilesFormatsAll() throws Exception {
    Path a = writeFile("a.adoc", "== title ==");
    Path b = writeFile("b.adoc", "== section ==");
    assertThat(execute("--write", a.toString(), b.toString())).isEqualTo(0);
    assertThat(Files.readString(a, StandardCharsets.UTF_8)).isEqualTo("== title");
    assertThat(Files.readString(b, StandardCharsets.UTF_8)).isEqualTo("== section");
  }

  @Test
  void writeOnReadOnlyFileReturnsExitCode2() throws Exception {
    // Bug fix verification: writability check must apply when -w is used
    Path file = writeFile("readonly.adoc", UNFORMATTED);
    assumeTrue(file.toFile().setWritable(false), "cannot make file read-only on this platform");
    try {
      assertThat(execute("--write", file.toString())).isEqualTo(2);
    } finally {
      file.toFile().setWritable(true);
    }
  }

  // -------------------------------------------------------------------------
  // No flags — print-to-stdout mode
  // -------------------------------------------------------------------------

  @Test
  void noFlagsFormattedFileReturnsExitCode0() throws Exception {
    Path file = writeFile("test.adoc", FORMATTED);
    assertThat(execute(file.toString())).isEqualTo(0);
  }

  @Test
  void noFlagsSingleUnformattedFilePrintsFormattedContentToStdout() throws Exception {
    Path file = writeFile("test.adoc", UNFORMATTED);
    ByteArrayOutputStream captured = captureStdout(() -> execute(file.toString()));
    assertThat(captured.toString(StandardCharsets.UTF_8)).isEqualTo("== title");
  }

  @Test
  void noFlagsMultipleFilesOneChangedPrintsWarningToStdout() throws Exception {
    Path a = writeFile("a.adoc", FORMATTED);
    Path b = writeFile("b.adoc", UNFORMATTED);
    ByteArrayOutputStream captured = captureStdout(() -> execute(a.toString(), b.toString()));
    assertThat(captured.toString(StandardCharsets.UTF_8))
        .contains("Would format")
        .contains(b.toString());
  }

  @Test
  void noFlagsMultipleFilesNoneChangedReturnsExitCode0() throws Exception {
    Path a = writeFile("a.adoc", FORMATTED);
    Path b = writeFile("b.adoc", "== Section");
    assertThat(execute(a.toString(), b.toString())).isEqualTo(0);
  }

  // -------------------------------------------------------------------------
  // Error cases — exit code 2
  // -------------------------------------------------------------------------

  @Test
  void nonExistentFileReturnsExitCode2() {
    Path missing = tempDir.resolve("missing.adoc");
    assertThat(execute(missing.toString())).isEqualTo(2);
  }

  @Test
  void directoryInputReturnsExitCode2() throws Exception {
    Path dir = Files.createDirectory(tempDir.resolve("subdir"));
    assertThat(execute(dir.toString())).isEqualTo(2);
  }

  @Test
  void mixedLineEndingsReturnsExitCode2() throws Exception {
    Path file = writeFile("mixed.adoc", "line1\nline2\r\nline3");
    assertThat(execute("--check", file.toString())).isEqualTo(2);
  }

  @Test
  void firstInvalidFileStopsProcessingAndReturnsExitCode2() throws Exception {
    Path missing = tempDir.resolve("missing.adoc");
    Path valid = writeFile("valid.adoc", FORMATTED);
    assertThat(execute(missing.toString(), valid.toString())).isEqualTo(2);
  }

  // -------------------------------------------------------------------------
  // Stdin
  // -------------------------------------------------------------------------

  @Test
  void noFileArgumentsReadFromStdinAndWriteFormattedOutputToStdout() throws Exception {
    byte[] input = "== title ==".getBytes(StandardCharsets.UTF_8);
    InputStream originalIn = System.in;
    ByteArrayOutputStream captured = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    try {
      System.setIn(new ByteArrayInputStream(input));
      System.setOut(new PrintStream(captured));
      assertThat(new CommandLine(new AsciidocFormatterCli()).execute()).isEqualTo(0);
    } finally {
      System.setIn(originalIn);
      System.setOut(originalOut);
    }
    assertThat(captured.toString(StandardCharsets.UTF_8)).isEqualTo("== title");
  }

  // -------------------------------------------------------------------------
  // Option flags
  // -------------------------------------------------------------------------

  @Test
  void disableDefaultOptionPreservesContent() throws Exception {
    // --remove-trailing-header-equals is on by default; disabling it leaves "==" intact
    Path file = writeFile("test.adoc", UNFORMATTED);
    assertThat(execute("--check", "--remove-trailing-header-equals=false", file.toString()))
        .isEqualTo(0);
  }

  @Test
  void enableNonDefaultOptionTransformsContent() throws Exception {
    Path file = writeFile("test.adoc", "== the title");
    assertThat(execute("--write", "--title-case", file.toString())).isEqualTo(0);
    assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("== The Title");
  }

  @Test
  void enableNonDefaultOptionWithShortAlias() throws Exception {
    Path file = writeFile("test.adoc", "- item");
    assertThat(execute("--write", "-nlb", file.toString())).isEqualTo(0);
    assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("* item");
  }

  @Test
  void enableNonDefaultOptionWithLongAlias() throws Exception {
    Path file = writeFile("test.adoc", "- item");
    assertThat(execute("--write", "--normalize-list-bullets", file.toString())).isEqualTo(0);
    assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("* item");
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private Path writeFile(String name, String content) throws Exception {
    Path file = tempDir.resolve(name);
    Files.writeString(file, content, StandardCharsets.UTF_8);
    return file;
  }

  private int execute(String... args) {
    return new CommandLine(new AsciidocFormatterCli()).execute(args);
  }

  private ByteArrayOutputStream captureStdout(Runnable action) {
    PrintStream original = System.out;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    try {
      action.run();
    } finally {
      System.setOut(original);
    }
    return baos;
  }
}
