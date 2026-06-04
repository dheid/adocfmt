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

class HeadingBlankLinesEnsurerTest {

  private static String apply(String input) {
    return AsciidocFormatterTestSupport.apply(input, cfg -> cfg.ensureHeadingBlankLines(true));
  }

  @Test
  void blankLineAddedAfterHeading() {
    assertThat(apply("== Section\nContent")).isEqualTo("== Section\n\nContent");
  }

  @Test
  void blankLineAddedBeforeHeading() {
    assertThat(apply("Content\n== Section")).isEqualTo("Content\n\n== Section");
  }

  @Test
  void blankLinesAddedBothSides() {
    assertThat(apply("Before\n== Section\nAfter")).isEqualTo("Before\n\n== Section\n\nAfter");
  }

  @Test
  void noDoubleBlankLineWhenAlreadyPresent() {
    String input = "Before\n\n== Section\n\nAfter";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void noBlankLineBeforeFirstHeading() {
    assertThat(apply("= Title\nContent")).isEqualTo("= Title\n\nContent");
  }

  @Test
  void consecutiveHeadingsGetBlankLineBetweenThem() {
    assertThat(apply("== Section A\n=== Subsection")).isEqualTo("== Section A\n\n=== Subsection");
  }

  @Test
  void headingInsideCodeBlockGetsNoBlankLine() {
    String input = "----\n== not a real heading\ncontent\n----";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void ensureHeadingBlankLinesIsIdempotent() {
    String input = "Intro\n== Section\nBody text\n=== Sub\nMore";
    String once = apply(input);
    String twice = apply(once);
    assertThat(twice).isEqualTo(once);
  }
}
