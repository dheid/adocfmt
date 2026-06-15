package org.drjekyll.adocfmt.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.drjekyll.adocfmt.AsciidocFormatter;
import org.drjekyll.adocfmt.AsciidocFormatterConfig;
import org.drjekyll.adocfmt.TableBlankLines;
import org.drjekyll.adocfmt.TableLayout;
import org.drjekyll.adocfmt.UnsupportedLineEndingException;
import org.junit.jupiter.api.Test;

class TableNormalizerTest {

  @Test
  void alignsSimpleTable() {
    String input = "|===\n| Cell 1 | Cell 2\n| Cell 3 | Cell 4\n|===";
    String expected = "|===\n| Cell 1 | Cell 2\n| Cell 3 | Cell 4\n|===";
    verify(input, expected);
  }

  @Test
  void alignsTableWithDifferentWidths() {
    String input = "|===\n| A | Long content\n| Very long content | B\n|===";
    String expected =
        "|===\n| A                 | Long content\n| Very long content | B           \n|===";
    verify(input, expected);
  }

  @Test
  void keepsComments() {
    String input =
        "|===\n"
            + "|Date| Version |  Author | Description\n"
            + "// some comment\n"
            + "|21.03.2025 | 1.0 | dheid |  Initial commit\n"
            + "// another comment\n"
            + "|===\n";
    String expected =
        "|===\n"
            + "| Date       | Version | Author | Description   \n"
            + "// some comment\n"
            + "| 21.03.2025 | 1.0     | dheid  | Initial commit\n"
            + "// another comment\n"
            + "|===\n";
    verify(input, expected);
  }

  @Test
  void respectsAlignmentMarkers() {
    String input = "|===\n^| Centered | >| Right\n| Left | <| Left explicitly\n|===";
    String expected =
        "|===\n^| Centered |  >|           Right\n | Left     |  <| Left explicitly\n|===";
    verify(input, expected);
  }

  @Test
  void bailsOutOnRowSpan() {
    String input = "|===\n.2+| Rowspan | Cell\n| Cell\n|===";
    verify(input, input);
  }

  @Test
  void bailsOutOnColSpan() {
    String input = "|===\n2+| Colspan\n| Cell | Cell\n|===";
    verify(input, input);
  }

  @Test
  void bailsOutOnBlockCell() {
    String input = "|===\na| Block cell\n|===";
    verify(input, input);
  }

  @Test
  void expandsTableWhenAutoLayoutExceedsWidth() {
    String input = "[cols=\"1,1\"]\n|===\n| Short | Long content\n|===";
    AsciidocFormatterConfig config =
        AsciidocFormatterConfig.builder()
            .tableLayout(TableLayout.AUTO)
            .tableMaxLineWidth(10)
            .build();
    String expected = "[cols=\"1,1\"]\n|===\n| Short\n| Long content\n|===";
    verify(input, expected, config);
  }

  @Test
  void respectsExpandedLayoutOption() {
    String input = "[cols=\"1,1\"]\n|===\n| A | B\n|===";
    AsciidocFormatterConfig config =
        AsciidocFormatterConfig.builder().tableLayout(TableLayout.EXPANDED).build();
    String expected = "[cols=\"1,1\"]\n|===\n| A\n| B\n|===";
    verify(input, expected, config);
  }

  @Test
  void respectsTableBlankLinesAll() {
    String input = "|===\n| A | B\n| C | D\n| E | F\n|===";
    AsciidocFormatterConfig config =
        AsciidocFormatterConfig.builder().tableBlankLines(TableBlankLines.ALL).build();
    // Header invariant: if no explicit header, never insert/remove gap after first row.
    // So for 3 rows, only one blank line will be inserted between row 2 and row 3.
    String expected = "|===\n| A | B\n| C | D\n\n| E | F\n|===";
    verify(input, expected, config);
  }

  @Test
  void preservesImplicitHeader() {
    String input = "|===\n| Row 1\n\n| Row 2\n|===";
    String expected = "|===\n| Row 1\n\n| Row 2\n|===";
    verify(input, expected);
  }

  @Test
  void convertsTabsToSpaces() {
    String input = "|===\n|\tTab\t| Space\n|===";
    String expected = "|===\n| Tab | Space\n|===";
    verify(input, expected);
  }

  @Test
  void tableInsideListingBlockRemainsUntouched() throws UnsupportedLineEndingException {
    String input = "----\n|===\n| Cell\n|===\n----\n";
    AsciidocFormatter formatter =
        new AsciidocFormatter(AsciidocFormatterConfig.builder().formatTables(true).build());
    assertThat(formatter.format(input)).isEqualTo(input);
  }

  @Test
  void bugReproduction() {
    String input = "[cols=\"1,1\"]\n|===\n| A | B\n| Very long content here | C\n|===";
    String expected =
        "[cols=\"1,1\"]\n|===\n| A                      | B\n| Very long content here | C\n|===";
    verify(input, expected);
  }

  private void verify(String input, String expected) {
    verify(input, expected, AsciidocFormatterConfig.builder().build());
  }

  private void verify(String input, String expected, AsciidocFormatterConfig config) {
    String actual = apply(input, config);
    assertThat(actual).as("Formatted output mismatch").isEqualTo(expected);

    // Idempotence
    assertThat(apply(actual, config)).as("Formatting must be idempotent").isEqualTo(actual);

    // Semantic equivalence
    try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
      Options options = Options.builder().safe(SafeMode.SAFE).standalone(false).build();
      String inputHtml = normalizeHtml(asciidoctor.convert(input, options));
      String actualHtml = normalizeHtml(asciidoctor.convert(actual, options));
      assertThat(actualHtml).as("Rendered HTML mismatch").isEqualTo(inputHtml);
    }
  }

  private String apply(String input, AsciidocFormatterConfig config) {
    List<String> lines = new ArrayList<>(List.of(input.split("\n", -1)));
    new TableNormalizer(lines, config).run();
    return String.join("\n", lines);
  }

  private String normalizeHtml(String html) {
    return html.replaceAll("\\s+", " ").trim();
  }
}
