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

import org.drjekyll.adocfmt.AsciidocFormatterTestSupport;
import org.junit.jupiter.api.Test;

class SetextHeadingsNormalizerTest {

  private static String applySetext(String input) {
    return AsciidocFormatterTestSupport.apply(input, cfg -> cfg.normalizeSetextHeadings(true));
  }

  @Test
  void convertsLevel0SetextHeading() {
    assertThat(applySetext("Document Title\n==============")).isEqualTo("= Document Title");
  }

  @Test
  void convertsLevel1SetextHeading() {
    assertThat(applySetext("Section Title\n-------------")).isEqualTo("== Section Title");
  }

  @Test
  void convertsLevel2SetextHeading() {
    assertThat(applySetext("Subsection Title\n~~~~~~~~~~~~~~~~")).isEqualTo("=== Subsection Title");
  }

  @Test
  void convertsLevel3SetextHeading() {
    assertThat(applySetext("Deep Section\n^^^^^^^^^^^^")).isEqualTo("==== Deep Section");
  }

  @Test
  void convertsLevel4SetextHeading() {
    assertThat(applySetext("Deepest Section\n+++++++++++++++")).isEqualTo("===== Deepest Section");
  }

  @Test
  void convertsAllSetextLevelsInDocument() {
    String input = "Document\n========\n\nSection\n-------\n\nSubsection\n~~~~~~~~~~";
    assertThat(applySetext(input)).isEqualTo("= Document\n\n== Section\n\n=== Subsection");
  }

  @Test
  void doesNotConvertWhenUnderlineTooShort() {
    assertThat(applySetext("Long Title Here\n---")).isEqualTo("Long Title Here\n---");
  }

  @Test
  void doesNotConvertBlockDelimiterAsHeadingUnderline() {
    assertThat(applySetext("----\ncode line\n----")).isEqualTo("----\ncode line\n----");
  }

  @Test
  void doesNotConvertLineStartingWithEquals() {
    assertThat(applySetext("== Already Atx\n==============="))
        .isEqualTo("== Already Atx\n===============");
  }

  @Test
  void doesNotConvertLineStartingWithBracket() {
    assertThat(applySetext("[source,java]\n============="))
        .isEqualTo("[source,java]\n=============");
  }

  @Test
  void doesNotConvertLineStartingWithSlash() {
    assertThat(applySetext("// comment\n===========")).isEqualTo("// comment\n===========");
  }

  @Test
  void doesNotConvertClosingBracketBeforeBlockDelimiter() {
    String input = "[source, json]\n----\nusers: [\n  {\n    \"id\": \"abc\"\n  }\n]\n----";
    assertThat(applySetext(input)).isEqualTo(input);
  }

  @Test
  void setextNormalizationIsIdempotent() {
    String input = "My Title\n========\n\nA Section\n---------";
    String once = applySetext(input);
    String twice = applySetext(once);
    assertThat(twice).isEqualTo(once);
  }

  @Test
  void setextNormalizationThenHeadingBlankLinesThenTitleCase() {
    String input = "some text\nmy cool section\n---------------\nsome body";
    String output =
        AsciidocFormatterTestSupport.apply(
            input,
            cfg -> {
              cfg.normalizeSetextHeadings(true);
              cfg.ensureHeadingBlankLines(true);
              cfg.titleCase(true);
            });
    assertThat(output).isEqualTo("some text\n\n== My Cool Section\n\nsome body");
  }
}
