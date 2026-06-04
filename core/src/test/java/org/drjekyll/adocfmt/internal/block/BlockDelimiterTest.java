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
package org.drjekyll.adocfmt.internal.block;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BlockDelimiterTest {

  @Test
  void dashSequenceIsDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("----")).isTrue();
  }

  @Test
  void equalsSequenceIsDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("====")).isTrue();
  }

  @Test
  void dotSequenceIsDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("....")).isTrue();
  }

  @Test
  void starSequenceIsDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("****")).isTrue();
  }

  @Test
  void underscoreSequenceIsDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("____")).isTrue();
  }

  @Test
  void plusSequenceIsDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("++++")).isTrue();
  }

  @Test
  void slashSequenceIsDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("////")).isTrue();
  }

  @Test
  void longerSequenceIsDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("--------")).isTrue();
  }

  @Test
  void threeCharsIsTooShort() {
    assertThat(BlockDelimiter.isBlockDelimiter("---")).isFalse();
  }

  @Test
  void emptyStringIsNotDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("")).isFalse();
  }

  @Test
  void mixedCharsIsNotDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("--=-")).isFalse();
  }

  @Test
  void tildeSequenceIsNotDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("~~~~")).isFalse();
  }

  @Test
  void caretSequenceIsNotDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("^^^^")).isFalse();
  }

  @Test
  void alphanumericTextIsNotDelimiter() {
    assertThat(BlockDelimiter.isBlockDelimiter("code")).isFalse();
  }

  @Test
  void singleCharIsTooShort() {
    assertThat(BlockDelimiter.isBlockDelimiter("-")).isFalse();
  }
}
