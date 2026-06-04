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

class SourceDelimiterTest {

  private static String apply(String input) {
    return AsciidocFormatterTestSupport.apply(
        input, builder -> builder.ensureSourceDelimiters(true));
  }

  @Test
  void sourceBlockWithoutDelimiterGetsWrapped() {
    assertThat(apply("[source,java]\npublic void foo() {}"))
        .isEqualTo("[source,java]\n----\npublic void foo() {}\n----\n");
  }

  @Test
  void sourceBlockAlreadyDelimitedLeftUnchanged() {
    String input = "[source,java]\n----\npublic void foo() {}\n----\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void listingBlockWithoutDelimiterGetsWrapped() {
    assertThat(apply("[listing]\nsome literal text"))
        .isEqualTo("[listing]\n----\nsome literal text\n----\n");
  }

  @Test
  void multiLineSourceBlockWrapped() {
    assertThat(apply("[source,yaml]\nkey: value\nother: data"))
        .isEqualTo("[source,yaml]\n----\nkey: value\nother: data\n----\n");
  }

  @Test
  void sourceBlockFollowedByBlankLineNotWrapped() {
    String input = "[source,java]\n\nsome text\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void sourceBlockFollowedByAnotherAttributeNotWrapped() {
    String input = "[source,java]\n[%linenums]\n----\ncode\n----\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void sourceBlockWithLanguageVariantsWrapped() {
    assertThat(apply("[source, json]\n{\"key\": \"value\"}"))
        .isEqualTo("[source, json]\n----\n{\"key\": \"value\"}\n----\n");
  }

  @Test
  void sourceWithPercentOptionWrapped() {
    assertThat(apply("[source%autofit,java]\npublic class Foo {}"))
        .isEqualTo("[source%autofit,java]\n----\npublic class Foo {}\n----\n");
  }

  @Test
  void sourceBlockInsideExistingDelimitedBlockLeftAlone() {
    String input = "====\n[source,java]\ncode\n====\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void ensureSourceDelimitersIsIdempotent() {
    String input = "[source,java]\npublic void foo() {}\n\n[source,yaml]\nkey: value";
    String once = apply(input);
    String twice = apply(once);
    assertThat(twice).isEqualTo(once);
  }

  @Test
  void overLongDelimiterRecognizedAsExistingDelimiter() {
    String input = "[source,java]\n--------\ncode\n--------\n";
    assertThat(apply(input)).isEqualTo(input);
  }

  @Test
  void sourceBlockWithIdShorthandGetsWrapped() {
    assertThat(apply("[source#intro,java]\npublic void foo() {}"))
        .isEqualTo("[source#intro,java]\n----\npublic void foo() {}\n----\n");
  }
}
