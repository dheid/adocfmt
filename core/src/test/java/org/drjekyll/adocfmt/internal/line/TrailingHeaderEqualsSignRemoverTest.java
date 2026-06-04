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

class TrailingHeaderEqualsSignRemoverTest {

  private static String apply(String input) {
    return AsciidocFormatterTestSupport.apply(
        input, cfg -> cfg.removeTrailingHeaderEqualsSign(true));
  }

  @Test
  void removesTrailingEqualsFromH2() {
    assertThat(apply("== Section Title ==")).isEqualTo("== Section Title");
  }

  @Test
  void removesTrailingEqualsFromH3() {
    assertThat(apply("=== Subsection ===")).isEqualTo("=== Subsection");
  }

  @Test
  void removesTrailingEqualsFromH4() {
    assertThat(apply("==== Deep ==== Section ====")).isEqualTo("==== Deep ==== Section");
  }

  @Test
  void removesTrailingEqualsWithTrailingSpaces() {
    assertThat(apply("== Title ==   ")).isEqualTo("== Title");
  }

  @Test
  void leavesAsymmetricHeadingUnchanged() {
    String input = "== Already Asymmetric";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void leavesNonHeadingLinesUnchanged() {
    String input = "Normal paragraph with == signs == inside.";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void removeTrailingEqualsIsIdempotent() {
    String input = "== Title ==\n=== Sub ===";
    String once = apply(input);
    String twice = apply(once);
    assertThat(twice).isEqualTo(once);
  }

  @Test
  void tabAfterHeadingMarkerNormalizedToSpace() {
    assertThat(apply("===\tNginx")).isEqualTo("=== Nginx");
  }

  @Test
  void multipleSpacesAfterHeadingMarkerCollapsed() {
    assertThat(apply("==  Title")).isEqualTo("== Title");
  }

  @Test
  void symmetricAtxHeadingStillStrippedWhenBothEnabled() {
    String output =
        AsciidocFormatterTestSupport.apply(
            "== Version ==",
            cfg -> {
              cfg.normalizeSetextHeadings(true);
              cfg.removeTrailingHeaderEqualsSign(true);
            });
    assertThat(output).isEqualTo("== Version");
  }

  @Test
  void crlfHeadingRecognizedAfterTrailingWhitespaceRemoval() {
    String output =
        AsciidocFormatterTestSupport.apply(
            "== Title\r\n\r\nBody.\r\n",
            cfg -> {
              cfg.removeTrailingWhitespace(true);
              cfg.removeTrailingHeaderEqualsSign(true);
            });
    assertThat(output).isEqualTo("== Title\r\n\r\nBody.");
  }
}
