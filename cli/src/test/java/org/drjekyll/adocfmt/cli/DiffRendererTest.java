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
package org.drjekyll.adocfmt.cli;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DiffRendererTest {

  private final DiffRenderer renderer = new DiffRenderer(false);

  @Test
  void identicalContentRendersNothing() {
    assertThat(renderer.render("a.adoc", "x\ny\n", "x\ny\n")).isEmpty();
  }

  @Test
  void changedLineIsShownWithSurroundingContextAndStartLine() {
    String original = "1\n2\n3\n4\n5\nold\n7\n8\n9\n10\n";
    String formatted = "1\n2\n3\n4\n5\nnew\n7\n8\n9\n10\n";

    assertThat(renderer.render("a.adoc", original, formatted))
        .isEqualTo(
            "Diff in a.adoc at line 3:\n"
                + " 3\n"
                + " 4\n"
                + " 5\n"
                + "-old\n"
                + "+new\n"
                + " 7\n"
                + " 8\n"
                + " 9\n");
  }

  @Test
  void distantChangesAreShownAsSeparateHunks() {
    String original = "a\n2\n3\n4\n5\n6\n7\n8\n9\nb\n";
    String formatted = "A\n2\n3\n4\n5\n6\n7\n8\n9\nB\n";

    assertThat(renderer.render("a.adoc", original, formatted))
        .isEqualTo(
            "Diff in a.adoc at line 1:\n"
                + "-a\n"
                + "+A\n"
                + " 2\n"
                + " 3\n"
                + " 4\n"
                + "Diff in a.adoc at line 7:\n"
                + " 7\n"
                + " 8\n"
                + " 9\n"
                + "-b\n"
                + "+B\n");
  }

  @Test
  void nearbyChangesAreMergedIntoOneHunk() {
    String original = "a\n2\n3\n4\n5\n6\n7\nb\n";
    String formatted = "A\n2\n3\n4\n5\n6\n7\nB\n";

    assertThat(renderer.render("a.adoc", original, formatted))
        .isEqualTo(
            "Diff in a.adoc at line 1:\n"
                + "-a\n"
                + "+A\n"
                + " 2\n"
                + " 3\n"
                + " 4\n"
                + " 5\n"
                + " 6\n"
                + " 7\n"
                + "-b\n"
                + "+B\n");
  }

  @Test
  void splitLineShowsRemovalsBeforeAdditions() {
    assertThat(renderer.render("a.adoc", "One. Two.\nend\n", "One.\nTwo.\nend\n"))
        .isEqualTo("Diff in a.adoc at line 1:\n-One. Two.\n+One.\n+Two.\n end\n");
  }

  @Test
  void removedLineIsShown() {
    assertThat(renderer.render("a.adoc", "a\nx\nb\n", "a\nb\n"))
        .isEqualTo("Diff in a.adoc at line 1:\n a\n-x\n b\n");
  }

  @Test
  void insertedLineIsShown() {
    assertThat(renderer.render("a.adoc", "a\nb\n", "a\n\nb\n"))
        .isEqualTo("Diff in a.adoc at line 1:\n a\n+\n b\n");
  }

  @Test
  void missingTrailingNewlineIsMarked() {
    assertThat(renderer.render("a.adoc", "x\nlast", "x\nlast\n"))
        .isEqualTo(
            "Diff in a.adoc at line 1:\n"
                + " x\n"
                + "-last\n"
                + "\\ No newline at end of file\n"
                + "+last\n");
  }

  @Test
  void carriageReturnsAreNotPrinted() {
    assertThat(renderer.render("a.adoc", "== t ==\r\n", "== t\r\n"))
        .isEqualTo("Diff in a.adoc at line 1:\n-== t ==\n+== t\n");
  }

  @Test
  void colorHighlightsRemovedAndAddedLines() {
    assertThat(new DiffRenderer(true).render("a.adoc", "old\n", "new\n"))
        .isEqualTo(
            "Diff in a.adoc at line 1:\n"
                + "\u001B[31m-old\u001B[0m\n"
                + "\u001B[32m+new\u001B[0m\n");
  }
}
