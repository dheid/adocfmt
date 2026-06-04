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

class TitleCaseTransformerTest {

  private static String apply(String input) {
    return AsciidocFormatterTestSupport.apply(input, cfg -> cfg.titleCase(true));
  }

  @Test
  void titleCasesLevel1SectionHeading() {
    assertThat(apply("= examples of title case")).isEqualTo("= Examples of Title Case\n");
  }

  @Test
  void titleCaseHandlesWordsWithPunctuation() {
    assertThat(apply("== word, and another")).isEqualTo("== Word, and Another\n");
  }

  @Test
  void titleCasesLevel2SectionHeading() {
    assertThat(apply("== the quick brown fox")).isEqualTo("== The Quick Brown Fox\n");
  }

  @Test
  void titleCasesDeepSectionHeading() {
    assertThat(apply("==== art of the deal")).isEqualTo("==== Art of the Deal\n");
  }

  @Test
  void titleCasesBlockTitle() {
    assertThat(apply(".examples of title case")).isEqualTo(".Examples of Title Case\n");
  }

  @Test
  void firstWordAlwaysCapitalizedEvenIfInLowercaseSet() {
    assertThat(apply("== of mice and men")).isEqualTo("== Of Mice and Men\n");
  }

  @Test
  void lastWordAlwaysCapitalized() {
    assertThat(apply("== end of the")).isEqualTo("== End of The\n");
  }

  @Test
  void articlesLowercasedInMiddle() {
    assertThat(apply("== the cat and the hat")).isEqualTo("== The Cat and the Hat\n");
  }

  @Test
  void prepositionLowercasedInMiddle() {
    assertThat(apply("== art of war")).isEqualTo("== Art of War\n");
  }

  @Test
  void coordinatingConjunctionLowercased() {
    assertThat(apply("== black or white")).isEqualTo("== Black or White\n");
  }

  @Test
  void wordWithAttributeReferenceSkipped() {
    assertThat(apply("== {doctitle} overview")).isEqualTo("== {doctitle} Overview\n");
  }

  @Test
  void wordWithCodeSpanSkipped() {
    assertThat(apply("== use `code` here")).isEqualTo("== Use `code` Here\n");
  }

  @Test
  void wordWithMacroSkipped() {
    assertThat(apply("== see link:url[] for details")).isEqualTo("== See link:url[] for Details\n");
  }

  @Test
  void regularParagraphLineUntouched() {
    String input = "this is just regular paragraph text.\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void alreadyTitleCasedHeadingUnchanged() {
    String input = "== Examples of Title Case\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void titleCaseIsIdempotent() {
    String input = "== examples of title case\n\n.a block title with of and the\n\nParagraph.";
    String once = apply(input);
    String twice = apply(once);
    assertThat(twice).isEqualTo(once);
  }

  @Test
  void dotDotLineNotTreatedAsBlockTitle() {
    String input = "..not a block title\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void dotSpaceLineNotTreatedAsBlockTitle() {
    String input = ". list item text\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void capitalizeAfterSentence() {
    assertThat(apply(".Asciidoctor usage example. The listing should contain 5 lines."))
        .isEqualTo(".Asciidoctor Usage Example. The Listing Should Contain 5 Lines.\n");
  }

  @Test
  void capitalizeAfterExclamationMark() {
    assertThat(apply(".Asciidoctor usage example! The listing should contain 5 lines."))
        .isEqualTo(".Asciidoctor Usage Example! The Listing Should Contain 5 Lines.\n");
  }

  @Test
  void capitalizeAfterQuestionMark() {
    assertThat(apply(".Is this working? The listing should contain 5 lines."))
        .isEqualTo(".Is This Working? The Listing Should Contain 5 Lines.\n");
  }

  @Test
  void capitalizeAfterSemicolon() {
    assertThat(apply(".Is this working; The listing should contain 5 lines."))
        .isEqualTo(".Is This Working; The Listing Should Contain 5 Lines.\n");
  }

  @Test
  void emptyWordFromTrailingSpaceSkipped() {
    // Block title with trailing space produces an empty last word — word.isEmpty() must return it
    // as-is
    assertThat(apply(".My Block Title ")).isEqualTo(".My Block Title \n");
  }

  @Test
  void wordWithColonInMiddleNotCapitalized() {
    // colonIdx > 0 && colonIdx < length-1 → return word unchanged
    assertThat(apply("== key:value config")).isEqualTo("== key:value Config\n");
  }

  @Test
  void wordWithColonAtEndCapitalized() {
    // colonIdx == length-1 → condition false → capitalize normally
    assertThat(apply("== http: scheme")).isEqualTo("== Http: Scheme\n");
  }

  @Test
  void headingWithNumericWordPreservesIt() {
    // "42" has no letters → firstLetter == -1 → return word unchanged
    assertThat(apply("== Step 42 Done")).isEqualTo("== Step 42 Done\n");
  }
}
