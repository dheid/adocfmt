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

import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;

class SectionHeadingMatcherTest {

  @Test
  void matchesLevel1Heading() {
    Matcher m = SectionHeadingMatcher.createSectionHeadingMatcher("= Document Title");
    assertThat(m.matches()).isTrue();
    assertThat(m.group(1)).isEqualTo("=");
    assertThat(m.group(2)).isEqualTo("Document Title");
  }

  @Test
  void matchesLevel2Heading() {
    Matcher m = SectionHeadingMatcher.createSectionHeadingMatcher("== Section");
    assertThat(m.matches()).isTrue();
    assertThat(m.group(1)).isEqualTo("==");
    assertThat(m.group(2)).isEqualTo("Section");
  }

  @Test
  void matchesLevel6Heading() {
    Matcher m = SectionHeadingMatcher.createSectionHeadingMatcher("====== Deep Section");
    assertThat(m.matches()).isTrue();
    assertThat(m.group(1)).isEqualTo("======");
    assertThat(m.group(2)).isEqualTo("Deep Section");
  }

  @Test
  void doesNotMatchSevenEqualsigns() {
    assertThat(SectionHeadingMatcher.createSectionHeadingMatcher("======= Too Deep").matches())
        .isFalse();
  }

  @Test
  void doesNotMatchPlainText() {
    assertThat(SectionHeadingMatcher.createSectionHeadingMatcher("Not a heading").matches())
        .isFalse();
  }

  @Test
  void doesNotMatchHeadingWithoutSpace() {
    assertThat(SectionHeadingMatcher.createSectionHeadingMatcher("==NoSpace").matches()).isFalse();
  }

  @Test
  void trailingSpacesStrippedFromGroup2() {
    Matcher m = SectionHeadingMatcher.createSectionHeadingMatcher("== Title   ");
    assertThat(m.matches()).isTrue();
    assertThat(m.group(2)).isEqualTo("Title");
  }

  @Test
  void doesNotMatchEmptyLine() {
    assertThat(SectionHeadingMatcher.createSectionHeadingMatcher("").matches()).isFalse();
  }

  @Test
  void doesNotMatchEqualsSignsWithNoTitle() {
    assertThat(SectionHeadingMatcher.createSectionHeadingMatcher("====").matches()).isFalse();
  }

  @Test
  void headingWithMultipleWordsMatchesCompletely() {
    Matcher m = SectionHeadingMatcher.createSectionHeadingMatcher("=== Getting Started Guide");
    assertThat(m.matches()).isTrue();
    assertThat(m.group(2)).isEqualTo("Getting Started Guide");
  }
}
