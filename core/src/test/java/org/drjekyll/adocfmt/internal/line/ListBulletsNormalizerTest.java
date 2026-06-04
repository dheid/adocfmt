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

class ListBulletsNormalizerTest {

  private static String apply(String input) {
    return AsciidocFormatterTestSupport.apply(input, cfg -> cfg.normalizeListBullets(true));
  }

  @Test
  void dashListItemConvertedToAsterisk() {
    assertThat(apply("- first item")).isEqualTo("* first item");
  }

  @Test
  void multipleDashItemsAllConverted() {
    assertThat(apply("- one\n- two\n- three")).isEqualTo("* one\n* two\n* three");
  }

  @Test
  void asteriskListItemUnchanged() {
    String input = "* existing asterisk item";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void nestedAsteriskItemsUnchanged() {
    String input = "* level one\n** level two\n*** level three";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void dashInsideCodeBlockUntouched() {
    String input = "----\n- not a list item\n----";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void blockDelimiterDashesNotConvertedToAsterisk() {
    String input = "----\ncode\n----";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void listBulletsNormalizationIsIdempotent() {
    String input = "- one\n- two";
    String once = apply(input);
    String twice = apply(once);
    assertThat(twice).isEqualTo(once);
  }
}
