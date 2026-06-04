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
package org.drjekyll.adocfmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.Test;

class AsciidocFormatterTest {

  private static final AsciidocFormatterConfig ALL_OPTIONS =
      AsciidocFormatterConfig.builder()
          .normalizeSetextHeadings(true)
          .collapseConsecutiveBlankLines(true)
          .oneSentencePerLine(true)
          .normalizeBlockDelimiters(true)
          .removeTrailingHeaderEqualsSign(true)
          .titleCase(true)
          .removeTrailingWhitespace(true)
          .normalizeListBullets(true)
          .normalizeOrderedListMarkers(true)
          .ensureHeadingBlankLines(true)
          .ensureSourceDelimiters(true)
          .build();

  // -------------------------------------------------------------------------
  // format(String)
  // -------------------------------------------------------------------------

  @Test
  void formatStringIsIdempotent() throws UnsupportedLineEndingException {
    String input = "= My Document Title\n\nFirst sentence. Second sentence here.\n";
    AsciidocFormatter formatter = new AsciidocFormatter(ALL_OPTIONS);
    String firstPass = formatter.format(input);
    assertThat(formatter.format(firstPass)).as("formatter must be idempotent").isEqualTo(firstPass);
  }

  @Test
  void formatStringProtectsDelimitedRegions() throws UnsupportedLineEndingException {
    String input =
        "----\nlisting block\n----\n\n"
            + "....\nliteral block\n....\n\n"
            + "++++\npassthrough block\n++++\n\n"
            + "////\ncomment block\n////\n\n"
            + "[source,java]\n----\npublic void foo() {}\n----\n";
    assertThat(new AsciidocFormatter(ALL_OPTIONS).format(input))
        .as("protected regions must not be altered")
        .isEqualTo(input);
  }

  @Test
  void formatStringPreservesSemantics() throws UnsupportedLineEndingException {
    String input =
        "my document title\n=================\n\nFirst sentence. Second sentence here.\n";
    String output = new AsciidocFormatter(ALL_OPTIONS).format(input);
    try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
      Options options = Options.builder().safe(SafeMode.SAFE).standalone(false).build();
      assertThat(normalizeHtml(asciidoctor.convert(output, options)))
          .as("formatted HTML must be semantically equivalent")
          .isEqualTo(normalizeHtml(asciidoctor.convert(input, options)));
    }
  }

  @Test
  void formatStringPreservesCrlfLineEndings() throws UnsupportedLineEndingException {
    AsciidocFormatter formatter =
        new AsciidocFormatter(
            AsciidocFormatterConfig.builder().collapseConsecutiveBlankLines(true).build());
    assertThat(formatter.format("line1\r\n\r\n\r\nline2")).isEqualTo("line1\r\n\r\nline2\r\n");
  }

  @Test
  void formatStringPreservesCrLineEndings() throws UnsupportedLineEndingException {
    AsciidocFormatter formatter =
        new AsciidocFormatter(
            AsciidocFormatterConfig.builder().collapseConsecutiveBlankLines(true).build());
    assertThat(formatter.format("line1\r\r\rline2")).isEqualTo("line1\r\rline2\r");
  }

  @Test
  void formatStringWithNoLineEndingsFormatsText() throws UnsupportedLineEndingException {
    AsciidocFormatter formatter =
        new AsciidocFormatter(
            AsciidocFormatterConfig.builder().removeTrailingHeaderEqualsSign(true).build());
    assertThat(formatter.format("== Title ==")).isEqualTo("== Title\n");
  }

  @Test
  void formatStringWithMixedLineEndingsThrows() {
    AsciidocFormatter formatter = new AsciidocFormatter(AsciidocFormatterConfig.builder().build());
    assertThatThrownBy(() -> formatter.format("line1\nline2\r\nline3"))
        .isInstanceOf(UnsupportedLineEndingException.class);
  }

  @Test
  void formatStringNullThrows() {
    AsciidocFormatter formatter = new AsciidocFormatter(AsciidocFormatterConfig.builder().build());
    assertThatThrownBy(() -> formatter.format((String) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void someOptionsEnabled() throws Exception {
    testResource(
        "fixtures/asciidocBefore.adoc",
        "fixtures/asciidocAfter.adoc",
        AsciidocFormatterConfig.builder()
            .normalizeSetextHeadings(true)
            .oneSentencePerLine(true)
            .normalizeBlockDelimiters(true)
            .removeTrailingHeaderEqualsSign(true)
            .build());
  }

  @Test
  void allOptionsEnabled() throws Exception {
    testResource(
        "fixtures/asciidocAllOptionsBefore.adoc",
        "fixtures/asciidocAllOptionsAfter.adoc",
        ALL_OPTIONS);
  }

  // -------------------------------------------------------------------------
  // format(CharSequence)
  // -------------------------------------------------------------------------

  @Test
  void formatCharSequenceAcceptsStringBuilder() throws UnsupportedLineEndingException {
    AsciidocFormatter formatter =
        new AsciidocFormatter(
            AsciidocFormatterConfig.builder().removeTrailingHeaderEqualsSign(true).build());
    assertThat(formatter.format((CharSequence) new StringBuilder("== Title ==")))
        .isEqualTo("== Title\n");
  }

  @Test
  void formatCharSequenceProducesSameResultAsFormatString() throws UnsupportedLineEndingException {
    String input = "== Title ==\n\nSome text.";
    AsciidocFormatter formatter =
        new AsciidocFormatter(
            AsciidocFormatterConfig.builder().removeTrailingHeaderEqualsSign(true).build());
    assertThat(formatter.format((CharSequence) input)).isEqualTo(formatter.format(input));
  }

  @Test
  void formatCharSequenceNullThrows() {
    AsciidocFormatter formatter = new AsciidocFormatter(AsciidocFormatterConfig.builder().build());
    assertThatThrownBy(() -> formatter.format((CharSequence) null))
        .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // format(byte[])
  // -------------------------------------------------------------------------

  @Test
  void formatBytesAppliesTransformationsAndPreservesUtf8() throws Exception {
    AsciidocFormatter formatter =
        new AsciidocFormatter(
            AsciidocFormatterConfig.builder().removeTrailingHeaderEqualsSign(true).build());
    byte[] input = "== Title ==\n".getBytes(StandardCharsets.UTF_8);
    byte[] output = formatter.format(input);
    assertThat(new String(output, StandardCharsets.UTF_8)).isEqualTo("== Title\n");
  }

  @Test
  void formatBytesRoundTripsUtf8Content() throws Exception {
    AsciidocFormatter formatter =
        new AsciidocFormatter(
            AsciidocFormatterConfig.builder().removeTrailingWhitespace(true).build());
    String text = "= Ünïcödé Héading\n\nContent with spëcial chars.   \n";
    byte[] input = text.getBytes(StandardCharsets.UTF_8);
    byte[] output = formatter.format(input);
    assertThat(new String(output, StandardCharsets.UTF_8))
        .isEqualTo("= Ünïcödé Héading\n\nContent with spëcial chars.\n");
  }

  @Test
  void formatBytesWithMixedLineEndingsThrows() {
    AsciidocFormatter formatter = new AsciidocFormatter(AsciidocFormatterConfig.builder().build());
    byte[] input = "line1\nline2\r\nline3".getBytes(StandardCharsets.UTF_8);
    assertThatThrownBy(() -> formatter.format(input))
        .isInstanceOf(UnsupportedLineEndingException.class);
  }

  @Test
  void formatBytesNullThrows() {
    AsciidocFormatter formatter = new AsciidocFormatter(AsciidocFormatterConfig.builder().build());
    assertThatThrownBy(() -> formatter.format((byte[]) null))
        .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // format(InputStream, OutputStream)
  // -------------------------------------------------------------------------

  @Test
  void formatStreamWritesFormattedOutput() throws IOException, UnsupportedLineEndingException {
    AsciidocFormatter formatter =
        new AsciidocFormatter(
            AsciidocFormatterConfig.builder().removeTrailingHeaderEqualsSign(true).build());
    byte[] input = "== Title ==\n".getBytes(StandardCharsets.UTF_8);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    formatter.format(new ByteArrayInputStream(input), out);
    assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo("== Title\n");
  }

  @Test
  void formatStreamPreservesLineEndings() throws IOException, UnsupportedLineEndingException {
    AsciidocFormatter formatter =
        new AsciidocFormatter(
            AsciidocFormatterConfig.builder().collapseConsecutiveBlankLines(true).build());
    byte[] input = "line1\r\n\r\n\r\nline2".getBytes(StandardCharsets.UTF_8);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    formatter.format(new ByteArrayInputStream(input), out);
    assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo("line1\r\n\r\nline2\r\n");
  }

  @Test
  void formatStreamNullInputThrows() {
    AsciidocFormatter formatter = new AsciidocFormatter(AsciidocFormatterConfig.builder().build());
    assertThatThrownBy(() -> formatter.format((InputStream) null, new ByteArrayOutputStream()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void formatStreamNullOutputThrows() {
    AsciidocFormatter formatter = new AsciidocFormatter(AsciidocFormatterConfig.builder().build());
    assertThatThrownBy(
            () -> formatter.format(new ByteArrayInputStream(new byte[0]), (OutputStream) null))
        .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // format(Collection<String>)
  // -------------------------------------------------------------------------

  @Test
  void formatCollectionAppliesTransformations() {
    AsciidocFormatter formatter =
        new AsciidocFormatter(
            AsciidocFormatterConfig.builder().removeTrailingHeaderEqualsSign(true).build());
    assertThat(formatter.format(List.of("== Title =="))).containsExactly("== Title");
  }

  @Test
  void formatCollectionReturnsNewList() {
    AsciidocFormatter formatter = new AsciidocFormatter(AsciidocFormatterConfig.builder().build());
    List<String> input = List.of("line1", "line2");
    List<String> result = formatter.format(input);
    assertThat(result).isNotSameAs(input).containsExactlyElementsOf(input);
  }

  @Test
  void formatCollectionEmptyInputReturnsEmptyList() {
    AsciidocFormatter formatter = new AsciidocFormatter(AsciidocFormatterConfig.builder().build());
    assertThat(formatter.format(List.of())).isEmpty();
  }

  @Test
  void formatCollectionMultipleLinesFormatted() {
    AsciidocFormatter formatter =
        new AsciidocFormatter(
            AsciidocFormatterConfig.builder()
                .normalizeListBullets(true)
                .removeTrailingWhitespace(true)
                .build());
    assertThat(formatter.format(List.of("- item one   ", "- item two   ")))
        .containsExactly("* item one", "* item two");
  }

  @Test
  void formatCollectionNullThrows() {
    AsciidocFormatter formatter = new AsciidocFormatter(AsciidocFormatterConfig.builder().build());
    assertThatThrownBy(() -> formatter.format((Collection<String>) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void reproductionOfTrailingNewlineIssue() throws UnsupportedLineEndingException {
    AsciidocFormatterConfig config = AsciidocFormatterConfig.builder().build();
    AsciidocFormatter formatter = new AsciidocFormatter(config);

    String input = "line1\n";
    String output = formatter.format(input);

    assertThat(output).isEqualTo("line1\n");
  }

  @Test
  void ensuresTrailingNewlineAddedIfMissing() throws UnsupportedLineEndingException {
    AsciidocFormatterConfig config = AsciidocFormatterConfig.builder().build();
    AsciidocFormatter formatter = new AsciidocFormatter(config);

    String input = "line1";
    String output = formatter.format(input);

    assertThat(output).isEqualTo("line1\n");
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private void testResource(String beforeRes, String afterRes, AsciidocFormatterConfig config)
      throws IOException, URISyntaxException, UnsupportedLineEndingException {
    String before = readResource(beforeRes);
    String after = readResource(afterRes);
    assertThat(new AsciidocFormatter(config).format(before)).isEqualTo(after);
  }

  private String readResource(String path) throws IOException, URISyntaxException {
    Path p = Paths.get(ClassLoader.getSystemResource(path).toURI());
    return Files.readString(p, StandardCharsets.UTF_8);
  }

  private String normalizeHtml(String html) {
    return html.replaceAll("\\s+", " ").trim();
  }
}
