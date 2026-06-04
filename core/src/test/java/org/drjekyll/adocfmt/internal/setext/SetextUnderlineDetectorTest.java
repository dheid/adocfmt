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
package org.drjekyll.adocfmt.internal.setext;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SetextUnderlineDetectorTest {

  @Test
  void detectsLevel0WithEqualsUnderline() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("Document Title", "=============="))
        .isEqualTo(0);
  }

  @Test
  void detectsLevel1WithDashUnderline() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("Section Title", "-------------"))
        .isEqualTo(1);
  }

  @Test
  void detectsLevel2WithTildeUnderline() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("Subsection", "~~~~~~~~~~"))
        .isEqualTo(2);
  }

  @Test
  void detectsLevel3WithCaretUnderline() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("Deep Section", "^^^^^^^^^^^^"))
        .isEqualTo(3);
  }

  @Test
  void detectsLevel4WithPlusUnderline() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("Deepest Section", "+++++++++++++++"))
        .isEqualTo(4);
  }

  @Test
  void returnsNullWhenUnderlineTooShort() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("Long Title Here", "---")).isNull();
  }

  @Test
  void returnsNullForEmptyTitle() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("", "====")).isNull();
  }

  @Test
  void returnsNullForEmptyUnderline() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("Title", "")).isNull();
  }

  @Test
  void returnsNullWhenTitleStartsWithEquals() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("== Already ATX", "=============="))
        .isNull();
  }

  @Test
  void returnsNullWhenTitleStartsWithBracket() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("[source,java]", "============="))
        .isNull();
  }

  @Test
  void returnsNullWhenTitleStartsWithDoubleSlash() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("// comment", "==========")).isNull();
  }

  @Test
  void returnsNullWhenTitleStartsWithDot() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline(".Block Title", "============"))
        .isNull();
  }

  @Test
  void returnsNullWhenTitleStartsWithStar() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("* list item", "==========="))
        .isNull();
  }

  @Test
  void returnsNullForMixedUnderlineChars() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("Title", "==-==")).isNull();
  }

  @Test
  void returnsNullForUnrecognizedUnderlineChar() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("Title", "######")).isNull();
  }

  @Test
  void underlineExactlyAsTitleLengthIsAccepted() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("Hi", "==")).isEqualTo(0);
  }

  @Test
  void underlineLongerThanTitleIsAccepted() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("Hi", "======")).isEqualTo(0);
  }

  @Test
  void returnsNullWhenTitleStartsWithPipe() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("| column", "========")).isNull();
  }

  @Test
  void returnsNullWhenTitleStartsWithPlus() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("+item", "=====")).isNull();
  }

  @Test
  void returnsNullWhenTitleStartsWithColon() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline(":attr:", "======")).isNull();
  }

  @Test
  void returnsNullWhenTitleStartsWithDash() {
    assertThat(SetextUnderlineDetector.detectSetextUnderline("-item", "=====")).isNull();
  }

  @Test
  void singleCharTitleWithEqualUnderlineIsLevel0() {
    // Also exercises startsWithDoubleSlash with a sequence shorter than "//"
    assertThat(SetextUnderlineDetector.detectSetextUnderline("A", "=")).isEqualTo(0);
  }
}
