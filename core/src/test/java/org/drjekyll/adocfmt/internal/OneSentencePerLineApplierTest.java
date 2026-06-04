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

import org.drjekyll.adocfmt.AsciidocFormatterTestSupport;
import org.junit.jupiter.api.Test;

class OneSentencePerLineApplierTest {

  private static String apply(String input) {
    return AsciidocFormatterTestSupport.apply(input, builder -> builder.oneSentencePerLine(true));
  }

  @Test
  void splitsTwoSentencesOnOneLine() {
    assertThat(apply("First sentence. Second sentence."))
        .isEqualTo("First sentence.\nSecond sentence.\n");
  }

  @Test
  void splitsExclamationAndQuestion() {
    assertThat(apply("Watch out! Are you sure? Proceed anyway."))
        .isEqualTo("Watch out!\nAre you sure?\nProceed anyway.\n");
  }

  @Test
  void doesNotChangeLinesEndingWithAPlusCharacter() {
    String input =
"""
If you want to break a line +
just end it in a {plus} sign +
and continue typing on the next line.
""";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void exclamationSplitsBeforeLowercaseWord() {
    assertThat(apply("Stop! don't move. Please continue."))
        .isEqualTo("Stop!\ndon't move.\nPlease continue.\n");
  }

  @Test
  void questionMarkSplitsBeforeLowercaseWord() {
    assertThat(apply("Really? maybe not. Let's check."))
        .isEqualTo("Really?\nmaybe not.\nLet's check.\n");
  }

  @Test
  void joinsMultiLineParagraphThenSplits() {
    assertThat(apply("This is a long sentence that\nspans multiple lines. Second sentence."))
        .isEqualTo("This is a long sentence that spans multiple lines.\nSecond sentence.\n");
  }

  @Test
  void idempotent() {
    String input = "First sentence. Second sentence.\nThird sentence.";
    String once = apply(input);
    String twice = apply(once);
    assertThat(twice).isEqualTo(once);
  }

  @Test
  void drAbbreviationIsNotASentenceBoundary() {
    assertThat(apply("Consult Dr. Smith before proceeding. Then continue."))
        .isEqualTo("Consult Dr. Smith before proceeding.\nThen continue.\n");
  }

  @Test
  void initialIsNotASentenceBoundary() {
    assertThat(apply("The author is A. Smith. He is famous."))
        .isEqualTo("The author is A. Smith.\nHe is famous.\n");
  }

  @Test
  void abbreviationFollowedByCapitalIsNotASentenceBoundary() {
    assertThat(apply("Item etc. And more. Next sentence."))
        .isEqualTo("Item etc. And more.\nNext sentence.\n");
  }

  @Test
  void blockTitleIsSpecialLine() {
    assertThat(apply(".Block Title\nThis is a sentence. This is another."))
        .isEqualTo(".Block Title\nThis is a sentence.\nThis is another.\n");
  }

  @Test
  void doesNotSplitInsideEgAbbreviation() {
    assertThat(apply("Use a tool (e.g. Spotless) for formatting. It helps."))
        .isEqualTo("Use a tool (e.g. Spotless) for formatting.\nIt helps.\n");
  }

  @Test
  void doesNotSplitDecimalNumber() {
    assertThat(apply("The value is 3.14 approximately. Use it wisely."))
        .isEqualTo("The value is 3.14 approximately.\nUse it wisely.\n");
  }

  @Test
  void doesNotSplitEllipsis() {
    assertThat(apply("Well... that is interesting. Next point."))
        .isEqualTo("Well... that is interesting.\nNext point.\n");
  }

  @Test
  void doesNotTouchHeadings() {
    String input = "== Section Title\n\nParagraph text.\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void doesNotTouchAttributeEntries() {
    String input = ":my-attr: some value\n\nParagraph.\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void doesNotTouchBlockAttributes() {
    String input = "[source,java]\n----\ncode here\n----\n\nText.\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void doesNotTouchListItems() {
    String input = "* First item. Still item.\n* Second item.\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void doesNotReformatInsideListingBlock() {
    String input = "----\nFirst sentence. Second sentence.\n----\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void doesNotReformatInsideExampleBlock() {
    String input = "====\nFirst sentence. Second sentence.\n====\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void pageBreakIsNotJoinedWithAdjacentMacros() {
    String input = "toc::[]\n<<<\ninclude::file.adoc[]\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void pageBreakBetweenParagraphsPassedThrough() {
    String input = "First paragraph.\n<<<\nSecond paragraph.\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void includeDirectiveNotJoinedWithParagraph() {
    String input = "include::chapter.adoc[]\n\nSome text.\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void tocMacroPassedThrough() {
    assertThat(apply("toc::[]")).isEqualTo("toc::[]\n");
  }

  @Test
  void horizontalRulePassedThrough() {
    assertThat(apply("Sentence one.\n'''\nSentence two."))
        .isEqualTo("Sentence one.\n'''\nSentence two.\n");
  }

  @Test
  void dashHorizontalRulePassedThrough() {
    assertThat(apply("Sentence one.\n---\nSentence two."))
        .isEqualTo("Sentence one.\n---\nSentence two.\n");
  }

  @Test
  void asteriskHorizontalRulePassedThrough() {
    assertThat(apply("Sentence one.\n***\nSentence two."))
        .isEqualTo("Sentence one.\n***\nSentence two.\n");
  }

  @Test
  void blankLineSeparatesParagraphs() {
    assertThat(apply("Paragraph one sentence one. Sentence two.\n\nParagraph two."))
        .isEqualTo("Paragraph one sentence one.\nSentence two.\n\nParagraph two.\n");
  }

  @Test
  void doesNotMangleSetextHeading() {
    String input = "My Section\n----------\n\nParagraph text.\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void singleSentenceReturnedAsIs() {
    assertThat(apply("Just one sentence.")).isEqualTo("Just one sentence.\n");
  }

  @Test
  void lowercaseAfterPeriodIsNotASplit() {
    assertThat(apply("lowercase follows. not a new sentence. no split here."))
        .isEqualTo("lowercase follows. not a new sentence. no split here.\n");
  }

  @Test
  void numberedListWithTabNotMangledByOneSentencePerLine() {
    String input = "1.\tFirst item\n2.\tSecond item\n3.\tThird item\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void digitAfterPeriodAndSpaceTriggersSplit() {
    // c=='.', next char after space is a digit — Character.isDigit branch
    assertThat(apply("See item. 2 were found.")).isEqualTo("See item.\n2 were found.\n");
  }

  @Test
  void ellipsisAtEndOfTextNotSplit() {
    // Inner while exits because i reaches end of string (i < text.length() == false)
    assertThat(apply("Fading out...")).isEqualTo("Fading out...\n");
  }

  @Test
  void closingParenthesisAfterTerminatorIsConsumed() {
    assertThat(apply("End.) Next sentence.")).isEqualTo("End.)\nNext sentence.\n");
  }

  @Test
  void closingSquareBracketAfterTerminatorIsConsumed() {
    assertThat(apply("End.] New sentence.")).isEqualTo("End.]\nNew sentence.\n");
  }

  @Test
  void closingDoubleQuoteAfterTerminatorIsConsumed() {
    assertThat(apply("He said \"done.\" She agreed."))
        .isEqualTo("He said \"done.\"\nShe agreed.\n");
  }

  @Test
  void closingSingleQuoteAfterTerminatorIsConsumed() {
    assertThat(apply("He left.' He returned.")).isEqualTo("He left.'\nHe returned.\n");
  }

  @Test
  void closingRightSingleQuoteAfterTerminatorIsConsumed() {
    assertThat(apply("He said ‘ok.’ She replied.")).isEqualTo("He said ‘ok.’\nShe replied.\n");
  }

  @Test
  void closingRightDoubleQuoteAfterTerminatorIsConsumed() {
    assertThat(apply("He said “done.” She agreed.")).isEqualTo("He said “done.”\nShe agreed.\n");
  }

  // -------------------------------------------------------------------------
  // isSpecialLine — direct branch coverage
  // -------------------------------------------------------------------------

  @Test
  void dotAtStartOfParagraphDoesNotSplitOnLowercase() {
    // A lone "." line goes into the paragraph buffer; when joined with following text
    // the resulting string starts with '.', making dotPos==0 — isAbbreviationContext
    // must handle dotPos > 0 being false without reading before position 0
    assertThat(apply(".\nHello world.")).isEqualTo(".\nHello world.\n");
  }

  @Test
  void orderedListItemWithSpaceIsSpecialLine() {
    // Exercises the charAt(i+1) == ' ' branch on line 390 of isSpecialLine
    String input = "1. First item\n2. Second item\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void closingRightSingleQuoteU2019AfterTerminatorIsConsumed() {
    // ’ = right single quotation mark (curly apostrophe)
    assertThat(apply("He said ‘ok.’ She replied.")).isEqualTo("He said ‘ok.’\nShe replied.\n");
  }

  @Test
  void isSpecialLineEmptyStringIsFalse() {
    assertThat(OneSentencePerLineApplier.isSpecialLine("")).isFalse();
  }

  @Test
  void isSpecialLineSpaceIndentedIsTrue() {
    assertThat(OneSentencePerLineApplier.isSpecialLine("  indented line")).isTrue();
  }

  @Test
  void isSpecialLineDigitThenNonDotWithMoreCharsIsFalse() {
    // i+1 < line.length() is true, but charAt(i) != '.' — reaches the '.' check
    assertThat(OneSentencePerLineApplier.isSpecialLine("1x more")).isFalse();
  }

  @Test
  void isSpecialLinePipeIsTrue() {
    assertThat(OneSentencePerLineApplier.isSpecialLine("| column data")).isTrue();
  }

  @Test
  void isSpecialLineTabIndentedIsTrue() {
    assertThat(OneSentencePerLineApplier.isSpecialLine("\tindented line")).isTrue();
  }

  @Test
  void isSpecialLineListContinuationIsTrue() {
    assertThat(OneSentencePerLineApplier.isSpecialLine("+")).isTrue();
  }

  @Test
  void isSpecialLineSingleColonIsFalse() {
    // first==':' but length==1, so length>1 is false
    assertThat(OneSentencePerLineApplier.isSpecialLine(":")).isFalse();
  }

  @Test
  void isSpecialLineDoubleColonIsFalse() {
    // first==':' and charAt(1)==':' — condition charAt(1)!=':' is false
    assertThat(OneSentencePerLineApplier.isSpecialLine("::")).isFalse();
  }

  @Test
  void isSpecialLineSingleStarIsFalse() {
    // length==1 → inner conditions all skip; while never runs; i==1<3 → false
    assertThat(OneSentencePerLineApplier.isSpecialLine("*")).isFalse();
  }

  @Test
  void isSpecialLineSingleDashIsFalse() {
    assertThat(OneSentencePerLineApplier.isSpecialLine("-")).isFalse();
  }

  @Test
  void isSpecialLineNestedBulletsIsTrue() {
    // "** item" — while consumes both '*', then charAt(2)==' ' → true (line 381)
    assertThat(OneSentencePerLineApplier.isSpecialLine("** item")).isTrue();
  }

  @Test
  void isSpecialLineNestedDashBulletsIsTrue() {
    assertThat(OneSentencePerLineApplier.isSpecialLine("-- item")).isTrue();
  }

  @Test
  void isSpecialLineStarFollowedByLetterIsFalse() {
    // first=='*', charAt(1)='b' not '*' or ' ', and first!='.' → falls through → false
    assertThat(OneSentencePerLineApplier.isSpecialLine("*bold*")).isFalse();
  }

  @Test
  void isSpecialLineDashFollowedByLetterIsFalse() {
    assertThat(OneSentencePerLineApplier.isSpecialLine("-option")).isFalse();
  }

  @Test
  void isSpecialLineDigitsOnlyIsFalse() {
    // While loop exits because i reaches end; i+1 >= length → false
    assertThat(OneSentencePerLineApplier.isSpecialLine("123")).isFalse();
  }

  @Test
  void isSpecialLineSingleDigitIsFalse() {
    assertThat(OneSentencePerLineApplier.isSpecialLine("1")).isFalse();
  }

  @Test
  void isSpecialLineDigitThenNonDotIsFalse() {
    assertThat(OneSentencePerLineApplier.isSpecialLine("1x")).isFalse();
  }

  @Test
  void isSpecialLineDigitDotNonSeparatorIsFalse() {
    assertThat(OneSentencePerLineApplier.isSpecialLine("1.x")).isFalse();
  }

  @Test
  void isSpecialLineRegularWordIsFalse() {
    // isBlockMacroOrTerm loop exits when i reaches end — i > 0 but i+1 >= len → false
    assertThat(OneSentencePerLineApplier.isSpecialLine("regularword")).isFalse();
  }

  @Test
  void isSpecialLineNonIdentifierStartIsFalse() {
    // '~' is not a valid identifier char — loop breaks immediately, i==0, i>0 is false
    assertThat(OneSentencePerLineApplier.isSpecialLine("~wave")).isFalse();
  }

  @Test
  void isSpecialLineSingleColonMacroIsFalse() {
    // "foo:bar" — block macro requires "::" not ":"
    assertThat(OneSentencePerLineApplier.isSpecialLine("foo:bar")).isFalse();
  }

  @Test
  void isSpecialLineUnderscoreIdentifierMacroIsTrue() {
    // c=='_' branch in isBlockMacroOrTerm
    assertThat(OneSentencePerLineApplier.isSpecialLine("foo_bar::")).isTrue();
  }

  @Test
  void isSpecialLineDigitInIdentifierMacroIsTrue() {
    // c>='0'&&c<='9' branch in isBlockMacroOrTerm
    assertThat(OneSentencePerLineApplier.isSpecialLine("macro2::")).isTrue();
  }
}
