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

class OrderedListMarkerNormalizerTest {

  private static String apply(String input) {
    return AsciidocFormatterTestSupport.apply(input, cfg -> cfg.normalizeOrderedListMarkers(true));
  }

  @Test
  void numberedListItemConvertedToAsciidocStyle() {
    assertThat(apply("1. First item")).isEqualTo(". First item\n");
  }

  @Test
  void largeNumberConvertedToAsciidocStyle() {
    assertThat(apply("42. Some item")).isEqualTo(". Some item\n");
  }

  @Test
  void multipleNumberedItemsAllConverted() {
    assertThat(apply("1. First\n2. Second\n3. Third")).isEqualTo(". First\n. Second\n. Third\n");
  }

  @Test
  void asciidocDotStyleUnchanged() {
    String input = ". First\n. Second\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void numberedListInsideCodeBlockUntouched() {
    String input = "----\n1. not a list item\n----\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void decimalNumberNotConvertedToListMarker() {
    String input = "Version 3.14 is released.\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void orderedListNormalizationIsIdempotent() {
    String input = "1. First\n2. Second\n3. Third";
    String once = apply(input);
    String twice = apply(once);
    assertThat(twice).isEqualTo(once);
  }

  @Test
  void numberedListWithTabAfterNumberConverted() {
    assertThat(apply("1.\tFirst item")).isEqualTo(". First item\n");
  }

  @Test
  void digitsWithNoFollowingCharUnchanged() {
    // While loop exits because i reaches end of string (not because of non-digit)
    assertThat(apply("123")).isEqualTo("123\n");
  }

  @Test
  void digitDotAtEndOfLineUnchanged() {
    // i+1 >= line.length() — nothing follows the dot
    assertThat(apply("1.")).isEqualTo("1.\n");
  }

  @Test
  void digitFollowedByNonDotUnchanged() {
    // charAt(i) != '.' — the char after digits is not a dot
    assertThat(apply("1x item")).isEqualTo("1x item\n");
  }

  @Test
  void digitDotNonSeparatorUnchanged() {
    // sep is neither space nor tab
    assertThat(apply("1.x item")).isEqualTo("1.x item\n");
  }
}
