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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.drjekyll.adocfmt.AsciidocFormatterConfig;
import org.junit.jupiter.api.Test;

class PerLineTransformerTest {

  private static List<String> run(AsciidocFormatterConfig config, String... lines) {
    List<String> list = new ArrayList<>(Arrays.asList(lines));
    new PerLineTransformer(list, config).run();
    return list;
  }

  @Test
  void appliesTrailingWhitespaceOutsideBlock() {
    AsciidocFormatterConfig config =
        AsciidocFormatterConfig.builder().removeTrailingWhitespace(true).build();
    assertThat(run(config, "line   ")).containsExactly("line");
  }

  @Test
  void appliesTrailingWhitespaceInsideBlock() {
    AsciidocFormatterConfig config =
        AsciidocFormatterConfig.builder().removeTrailingWhitespace(true).build();
    assertThat(run(config, "----", "code   ", "----")).containsExactly("----", "code", "----");
  }

  @Test
  void doesNotApplyListBulletsInsideBlock() {
    AsciidocFormatterConfig config =
        AsciidocFormatterConfig.builder().normalizeListBullets(true).build();
    assertThat(run(config, "----", "- not a list item", "----"))
        .containsExactly("----", "- not a list item", "----");
  }

  @Test
  void appliesListBulletsOutsideBlock() {
    AsciidocFormatterConfig config =
        AsciidocFormatterConfig.builder().normalizeListBullets(true).build();
    assertThat(run(config, "- item")).containsExactly("* item");
  }

  @Test
  void doesNotApplyTitleCaseInsideBlock() {
    AsciidocFormatterConfig config = AsciidocFormatterConfig.builder().titleCase(true).build();
    assertThat(run(config, "----", "== not a heading inside block", "----"))
        .containsExactly("----", "== not a heading inside block", "----");
  }

  @Test
  void appliesTitleCaseOutsideBlock() {
    AsciidocFormatterConfig config = AsciidocFormatterConfig.builder().titleCase(true).build();
    assertThat(run(config, "== the title")).containsExactly("== The Title");
  }

  @Test
  void doesNotApplyOrderedListInsideBlock() {
    AsciidocFormatterConfig config =
        AsciidocFormatterConfig.builder().normalizeOrderedListMarkers(true).build();
    assertThat(run(config, "----", "1. not converted", "----"))
        .containsExactly("----", "1. not converted", "----");
  }

  @Test
  void appliesMultipleTransformationsToOneLine() {
    AsciidocFormatterConfig config =
        AsciidocFormatterConfig.builder()
            .removeTrailingWhitespace(true)
            .normalizeListBullets(true)
            .build();
    assertThat(run(config, "- item   ")).containsExactly("* item");
  }

  @Test
  void handlesEmptyLineList() {
    AsciidocFormatterConfig config =
        AsciidocFormatterConfig.builder().removeTrailingWhitespace(true).build();
    assertThat(run(config)).isEmpty();
  }

  @Test
  void noOpWhenNoTransformationsEnabled() {
    AsciidocFormatterConfig config = AsciidocFormatterConfig.builder().build();
    assertThat(run(config, "- item   ", "== heading")).containsExactly("- item   ", "== heading");
  }
}
