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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LineEndingTest {

  @Test
  void detectsLf() throws UnsupportedLineEndingException {
    assertThat(LineEnding.determineLineEnding("line1\nline2\nline3")).contains(LineEnding.LF);
  }

  @Test
  void detectsCrlf() throws UnsupportedLineEndingException {
    assertThat(LineEnding.determineLineEnding("line1\r\nline2\r\nline3")).contains(LineEnding.CRLF);
  }

  @Test
  void detectsCr() throws UnsupportedLineEndingException {
    assertThat(LineEnding.determineLineEnding("line1\rline2\rline3")).contains(LineEnding.CR);
  }

  @Test
  void returnsEmptyForTextWithNoLineEndings() throws UnsupportedLineEndingException {
    assertThat(LineEnding.determineLineEnding("no line endings here")).isEmpty();
  }

  @Test
  void returnsEmptyForEmptyString() throws UnsupportedLineEndingException {
    assertThat(LineEnding.determineLineEnding("")).isEmpty();
  }

  @Test
  void singleLfDetected() throws UnsupportedLineEndingException {
    assertThat(LineEnding.determineLineEnding("a\nb")).contains(LineEnding.LF);
  }

  @Test
  void singleCrlfDetected() throws UnsupportedLineEndingException {
    assertThat(LineEnding.determineLineEnding("a\r\nb")).contains(LineEnding.CRLF);
  }

  @Test
  void crNotConfusedWithCrlfWhenFollowedByNonLf() throws UnsupportedLineEndingException {
    assertThat(LineEnding.determineLineEnding("a\rb")).contains(LineEnding.CR);
  }

  @Test
  void throwsForMixedLfAndCrlf() {
    assertThatThrownBy(() -> LineEnding.determineLineEnding("line1\nline2\r\nline3"))
        .isInstanceOf(UnsupportedLineEndingException.class);
  }

  @Test
  void throwsForMixedLfAndCr() {
    assertThatThrownBy(() -> LineEnding.determineLineEnding("line1\nline2\rline3"))
        .isInstanceOf(UnsupportedLineEndingException.class);
  }

  @Test
  void throwsForMixedCrlfAndCr() {
    assertThatThrownBy(() -> LineEnding.determineLineEnding("line1\r\nline2\rline3"))
        .isInstanceOf(UnsupportedLineEndingException.class);
  }

  @Test
  void lfEnumValueHasCorrectString() {
    assertThat(LineEnding.LF.getLineEnding()).isEqualTo("\n");
  }

  @Test
  void crlfEnumValueHasCorrectString() {
    assertThat(LineEnding.CRLF.getLineEnding()).isEqualTo("\r\n");
  }

  @Test
  void crEnumValueHasCorrectString() {
    assertThat(LineEnding.CR.getLineEnding()).isEqualTo("\r");
  }

  @Test
  void crAtVeryEndWithoutFollowingLfDetectedAsCr() throws UnsupportedLineEndingException {
    // \r is the last char — the i+1 < length guard must not read past the end
    assertThat(LineEnding.determineLineEnding("line1\rline2\r")).contains(LineEnding.CR);
  }
}
