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

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.drjekyll.adocfmt.internal.BlankLinesCollapser;
import org.drjekyll.adocfmt.internal.HeadingBlankLinesEnsurer;
import org.drjekyll.adocfmt.internal.OneSentencePerLineApplier;
import org.drjekyll.adocfmt.internal.SourceDelimiter;
import org.drjekyll.adocfmt.internal.block.BlockNormalizer;
import org.drjekyll.adocfmt.internal.line.PerLineTransformer;
import org.drjekyll.adocfmt.internal.setext.SetextHeadingsNormalizer;

/**
 * Entry point for formatting AsciiDoc source documents.
 *
 * <p>Construct with an {@link AsciidocFormatterConfig} to control which transformations are
 * applied, then call one of the {@code format} overloads. All overloads are stateless; the same
 * instance may be reused concurrently.
 */
@RequiredArgsConstructor
public class AsciidocFormatter {

  private final AsciidocFormatterConfig config;

  /**
   * Formats AsciiDoc content read from {@code inputStream} and writes the result to {@code
   * outputStream}.
   *
   * @param inputStream the source to read; must not be {@code null}
   * @param outputStream the destination to write; must not be {@code null}
   * @throws IOException if an I/O error occurs
   * @throws UnsupportedLineEndingException if the input contains mixed line endings
   */
  public void format(@NonNull InputStream inputStream, @NonNull OutputStream outputStream)
      throws IOException, UnsupportedLineEndingException {
    outputStream.write(format(inputStream.readAllBytes()));
  }

  /**
   * Formats raw AsciiDoc bytes, preserving the original charset encoding.
   *
   * <p>The charset is auto-detected from the byte content; the returned bytes use the same
   * encoding.
   *
   * @param input the raw AsciiDoc bytes; must not be {@code null}
   * @return the formatted bytes in the detected charset
   * @throws UnsupportedEncodingException if the detected charset is not supported by the JVM
   * @throws UnsupportedLineEndingException if the input contains mixed line endings
   */
  public byte[] format(byte[] input)
      throws UnsupportedEncodingException, UnsupportedLineEndingException {
    Objects.requireNonNull(input, "input must not be null");
    CharsetDetector charsetDetector = new CharsetDetector();
    charsetDetector.setText(input);
    CharsetMatch charsetMatch = charsetDetector.detect();
    return format(new String(input, charsetMatch.getName())).getBytes(charsetMatch.getName());
  }

  /**
   * Formats an AsciiDoc {@link CharSequence}.
   *
   * @param charSequence the source text; must not be {@code null}
   * @return the formatted text
   * @throws UnsupportedLineEndingException if the input contains mixed line endings
   */
  @org.jspecify.annotations.NonNull
  public String format(@NonNull CharSequence charSequence) throws UnsupportedLineEndingException {
    return format(charSequence.toString());
  }

  /**
   * Formats an AsciiDoc string, preserving the original line ending style.
   *
   * @param str the source text; must not be {@code null}
   * @return the formatted text using the same line ending style as the input
   * @throws UnsupportedLineEndingException if the input contains mixed line endings
   */
  @org.jspecify.annotations.NonNull
  public String format(@NonNull String str) throws UnsupportedLineEndingException {
    Optional<LineEnding> lineEndingOpt = LineEnding.determineLineEnding(str);

    String lineSeparator =
        lineEndingOpt.map(LineEnding::getLineEnding).orElse(System.lineSeparator());

    List<String> lines =
        lineEndingOpt
            .map(le -> Arrays.asList(le.getSplitPattern().split(str, -1)))
            .orElseGet(() -> List.of(str));

    String result = String.join(lineSeparator, format(lines));

    if (!result.isEmpty() && !result.endsWith(lineSeparator)) {
      result += lineSeparator;
    }

    return result;
  }

  /**
   * Formats a pre-split sequence of AsciiDoc lines by applying all enabled transformations.
   *
   * <p>Lines must not contain trailing line separators. Transformations are applied in the order
   * defined by the configuration.
   *
   * @param lines the lines to format; must not be {@code null}
   * @return the formatted lines (a new list; the input collection is not modified)
   */
  @org.jspecify.annotations.NonNull
  public List<String> format(@NonNull Collection<String> lines) {
    List<String> result = new ArrayList<>(lines);

    applyFormatting(result, config::isNormalizeSetextHeadings, SetextHeadingsNormalizer::new);
    applyFormatting(result, config::isEnsureSourceDelimiters, SourceDelimiter::new);
    applyFormatting(result, config::isNormalizeBlockDelimiters, BlockNormalizer::new);

    PerLineTransformer perLineTransformer = new PerLineTransformer(result, config);
    perLineTransformer.run();

    applyFormatting(result, config::isEnsureHeadingBlankLines, HeadingBlankLinesEnsurer::new);
    applyFormatting(result, config::isOneSentencePerLine, OneSentencePerLineApplier::new);
    applyFormatting(result, config::isCollapseConsecutiveBlankLines, BlankLinesCollapser::new);

    return result;
  }

  private void applyFormatting(
      List<String> lines,
      Supplier<Boolean> configSupplier,
      Function<List<String>, Runnable> formatterConstructor) {
    if (configSupplier.get()) {
      formatterConstructor.apply(lines).run();
    }
  }
}
