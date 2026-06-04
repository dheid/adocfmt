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

import org.junit.jupiter.api.Test;

class AsciidocFormatterConfigTest {

  @Test
  void defaultBuilderProducesAllFalseConfig() {
    AsciidocFormatterConfig config = AsciidocFormatterConfig.builder().build();
    assertThat(config.isNormalizeSetextHeadings()).isFalse();
    assertThat(config.isCollapseConsecutiveBlankLines()).isFalse();
    assertThat(config.isOneSentencePerLine()).isFalse();
    assertThat(config.isNormalizeBlockDelimiters()).isFalse();
    assertThat(config.isRemoveTrailingHeaderEqualsSign()).isFalse();
    assertThat(config.isTitleCase()).isFalse();
    assertThat(config.isRemoveTrailingWhitespace()).isFalse();
    assertThat(config.isNormalizeListBullets()).isFalse();
    assertThat(config.isNormalizeOrderedListMarkers()).isFalse();
    assertThat(config.isEnsureHeadingBlankLines()).isFalse();
    assertThat(config.isEnsureSourceDelimiters()).isFalse();
  }

  @Test
  void builderSetsEachFlagIndependently() {
    AsciidocFormatterConfig config =
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
    assertThat(config.isNormalizeSetextHeadings()).isTrue();
    assertThat(config.isCollapseConsecutiveBlankLines()).isTrue();
    assertThat(config.isOneSentencePerLine()).isTrue();
    assertThat(config.isNormalizeBlockDelimiters()).isTrue();
    assertThat(config.isRemoveTrailingHeaderEqualsSign()).isTrue();
    assertThat(config.isTitleCase()).isTrue();
    assertThat(config.isRemoveTrailingWhitespace()).isTrue();
    assertThat(config.isNormalizeListBullets()).isTrue();
    assertThat(config.isNormalizeOrderedListMarkers()).isTrue();
    assertThat(config.isEnsureHeadingBlankLines()).isTrue();
    assertThat(config.isEnsureSourceDelimiters()).isTrue();
  }

  @Test
  void configsWithSameFlagsAreEqual() {
    AsciidocFormatterConfig a =
        AsciidocFormatterConfig.builder()
            .normalizeSetextHeadings(true)
            .oneSentencePerLine(true)
            .build();
    AsciidocFormatterConfig b =
        AsciidocFormatterConfig.builder()
            .normalizeSetextHeadings(true)
            .oneSentencePerLine(true)
            .build();
    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  void configsWithDifferentFlagsAreNotEqual() {
    AsciidocFormatterConfig a = AsciidocFormatterConfig.builder().titleCase(true).build();
    AsciidocFormatterConfig b = AsciidocFormatterConfig.builder().titleCase(false).build();
    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void singleFlagDoesNotAffectOtherFlags() {
    AsciidocFormatterConfig config = AsciidocFormatterConfig.builder().titleCase(true).build();
    assertThat(config.isTitleCase()).isTrue();
    assertThat(config.isNormalizeSetextHeadings()).isFalse();
    assertThat(config.isOneSentencePerLine()).isFalse();
    assertThat(config.isNormalizeBlockDelimiters()).isFalse();
  }
}
