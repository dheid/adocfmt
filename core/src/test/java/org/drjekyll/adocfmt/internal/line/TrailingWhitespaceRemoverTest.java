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
package org.drjekyll.adocfmt.internal.line;

import static org.assertj.core.api.Assertions.assertThat;

import org.drjekyll.adocfmt.AsciidocFormatterTestSupport;
import org.junit.jupiter.api.Test;

class TrailingWhitespaceRemoverTest {

  private static String apply(String input) {
    return AsciidocFormatterTestSupport.apply(input, cfg -> cfg.removeTrailingWhitespace(true));
  }

  @Test
  void trailingSpacesRemovedFromLine() {
    assertThat(apply("line with trailing spaces   ")).isEqualTo("line with trailing spaces");
  }

  @Test
  void trailingTabRemovedFromLine() {
    assertThat(apply("line with tab\t")).isEqualTo("line with tab");
  }

  @Test
  void lineWithoutTrailingWhitespaceUnchanged() {
    String input = "clean line";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void blankLineReducedToEmpty() {
    assertThat(apply("   ")).isEqualTo("");
  }

  @Test
  void trailingWhitespaceRemovedFromMultipleLines() {
    assertThat(apply("first  \nsecond\t\nthird   ")).isEqualTo("first\nsecond\nthird");
  }

  @Test
  void removeTrailingWhitespaceIsIdempotent() {
    String input = "line one   \nline two\t";
    String once = apply(input);
    String twice = apply(once);
    assertThat(twice).isEqualTo(once);
  }

  @Test
  void keepsLineEndings() {
    assertThat(apply("line one\r\nline two")).isEqualTo("line one\r\nline two");
  }
}
