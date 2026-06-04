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
package org.drjekyll.adocfmt.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.drjekyll.adocfmt.AsciidocFormatterTestSupport;
import org.junit.jupiter.api.Test;

class BlankLinesCollapserTest {

  private static String apply(String input) {
    return AsciidocFormatterTestSupport.apply(
        input, builder -> builder.collapseConsecutiveBlankLines(true));
  }

  @Test
  void singleBlankLinePreserved() {
    assertThat(apply("A\n\nB")).isEqualTo("A\n\nB\n");
  }

  @Test
  void twoBlankLinesCollapsedToOne() {
    assertThat(apply("A\n\n\nB")).isEqualTo("A\n\nB\n");
  }

  @Test
  void threeBlankLinesCollapsedToOne() {
    assertThat(apply("A\n\n\n\nB")).isEqualTo("A\n\nB\n");
  }

  @Test
  void noBlankLinesUnchanged() {
    assertThat(apply("A\nB\nC")).isEqualTo("A\nB\nC\n");
  }

  @Test
  void multipleGroupsEachCollapsed() {
    assertThat(apply("A\n\n\nB\n\n\n\nC")).isEqualTo("A\n\nB\n\nC\n");
  }

  @Test
  void leadingBlankLinesCollapsed() {
    assertThat(apply("\n\n\nA")).isEqualTo("\nA\n");
  }

  @Test
  void trailingBlankLinesCollapsed() {
    assertThat(apply("A\n\n\n")).isEqualTo("A\n");
  }

  @Test
  void collapseIsIdempotent() {
    String input = "A\n\n\nB\n\n\n\nC";
    String once = apply(input);
    String twice = apply(once);
    assertThat(twice).isEqualTo(once);
  }
}
