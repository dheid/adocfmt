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
package org.drjekyll.adocfmt.internal.block;

import static org.assertj.core.api.Assertions.assertThat;

import org.drjekyll.adocfmt.AsciidocFormatterTestSupport;
import org.junit.jupiter.api.Test;

class BlockNormalizerTest {

  private static String apply(String input) {
    return AsciidocFormatterTestSupport.apply(
        input, builder -> builder.normalizeBlockDelimiters(true));
  }

  @Test
  void shortensLongDashDelimiter() {
    assertThat(apply("--------\ncode\n--------")).isEqualTo("----\ncode\n----");
  }

  @Test
  void shortensLongEqualsDelimiter() {
    assertThat(apply("========\ncontent\n========")).isEqualTo("====\ncontent\n====");
  }

  @Test
  void shortensLongDotDelimiter() {
    assertThat(apply("........\nliteral\n........")).isEqualTo("....\nliteral\n....");
  }

  @Test
  void shortensLongStarDelimiter() {
    assertThat(apply("********\nsidebar\n********")).isEqualTo("****\nsidebar\n****");
  }

  @Test
  void shortensLongUnderscoreDelimiter() {
    assertThat(apply("________\nquote\n________")).isEqualTo("____\nquote\n____");
  }

  @Test
  void shortensLongPlusDelimiter() {
    assertThat(apply("++++++++\npass\n++++++++")).isEqualTo("++++\npass\n++++");
  }

  @Test
  void shortensLongSlashDelimiter() {
    assertThat(apply("////////\ncomment\n////////")).isEqualTo("////\ncomment\n////");
  }

  @Test
  void leavesMinimalDelimiterUnchanged() {
    String input = "----\ncode\n----";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void doesNotShortenSetextHeadingUnderline() {
    String input = "Document Title\n==============";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void doesNotShortenTildeSetextUnderline() {
    String input = "Subsection\n~~~~~~~~~~";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void blockDelimiterNormalizationIsIdempotent() {
    String input = "--------\ncode\n--------\n\n========\nblock\n========";
    String once = apply(input);
    String twice = apply(once);
    assertThat(twice).isEqualTo(once);
  }

  @Test
  void doesNotShortenLongDelimiterThatIsSetextHeadingUnderline() {
    // "======" is >4 chars but follows a text line — it is a setext underline, not a block opener
    String input = "Title\n======";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void shortensLongDelimiterAfterNonBlankLineWhenUnderlineTooShort() {
    // prev is non-null and non-blank, but underline is shorter than the title so
    // detectSetextUnderline returns null → notSetextUnderline is true → shorten
    assertThat(apply("some text\n--------\ncode\n--------"))
        .isEqualTo("some text\n----\ncode\n----");
  }
}
