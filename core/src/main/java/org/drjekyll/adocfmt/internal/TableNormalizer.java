package org.drjekyll.adocfmt.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.drjekyll.adocfmt.AsciidocFormatterConfig;
import org.drjekyll.adocfmt.TableBlankLines;
import org.drjekyll.adocfmt.TableLayout;
import org.drjekyll.adocfmt.internal.block.BlockDelimiter;
import org.drjekyll.adocfmt.internal.block.BlockTracker;

/** Formats AsciiDoc tables. */
@RequiredArgsConstructor
public class TableNormalizer implements Runnable {

  private static final Pattern TITLE_PATTERN = Pattern.compile("^\\..*\\s*$");

  private final List<String> lines;
  private final AsciidocFormatterConfig config;

  @Override
  public void run() {
    BlockTracker bt = new BlockTracker();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (bt.isOpen()) {
        bt.tryClose(line);
        continue;
      }
      if (BlockDelimiter.isBlockDelimiter(line)) {
        if (BlockDelimiter.isTableDelimiter(line)) {
          i = processTable(i);
        } else {
          bt.open(line);
        }
      }
    }
  }

  private int processTable(int startIndex) {
    int start = startIndex;
    int end = start + 1;
    BlockTracker tableTracker = new BlockTracker();
    tableTracker.open(lines.get(start));
    while (end < lines.size()) {
      String line = lines.get(end);
      if (tableTracker.tryClose(line) != null) {
        break;
      }
      end++;
    }

    if (end >= lines.size()) {
      return start; // Unclosed table, skip
    }

    List<String> tableLines = new ArrayList<>(lines.subList(start, end + 1));
    if (shouldBailOut(tableLines)) {
      return end;
    }
    // Header preservation invariant detection
    boolean explicitHeader = false;
    boolean hasCols = false;
    if (start > 0) {
      int p = start - 1;
      if (p >= 0 && TITLE_PATTERN.matcher(lines.get(p)).matches()) {
        p--;
      }
      if (p >= 0) {
        String attrLine = lines.get(p);
        if (attrLine.matches("^\\[.*\\]\\s*$")) {
          if (attrLine.contains("header")) {
            explicitHeader = true;
          }
          if (attrLine.contains("cols=")) {
            hasCols = true;
          }
        }
      }
    }

    List<String> formattedTable = formatTable(tableLines, explicitHeader, hasCols);
    if (formattedTable != null) {
      int originalSize = end - start + 1;
      for (int i = 0; i < Math.min(originalSize, formattedTable.size()); i++) {
        lines.set(start + i, formattedTable.get(i));
      }
      if (formattedTable.size() > originalSize) {
        lines.addAll(
            start + originalSize, formattedTable.subList(originalSize, formattedTable.size()));
      } else if (formattedTable.size() < originalSize) {
        for (int i = 0; i < originalSize - formattedTable.size(); i++) {
          lines.remove(start + formattedTable.size());
        }
      }
      return start + formattedTable.size() - 1;
    }

    return end;
  }

  private boolean shouldBailOut(List<String> tableLines) {
    String openDelim = tableLines.get(0);
    if (openDelim.startsWith(",==") || openDelim.startsWith(":==")) {
      return true;
    }

    Pattern spanOrBlockPattern = Pattern.compile("(?:^|\\|)\\s*(?:\\d*\\.?\\d+\\+|a)\\|");
    for (int i = 1; i < tableLines.size() - 1; i++) {
      String line = tableLines.get(i);
      if (line.contains("!===")) {
        return true;
      }
      if (spanOrBlockPattern.matcher(line).find()) {
        return true;
      }
    }

    return false;
  }

  private List<String> formatTable(
      List<String> tableLines, boolean explicitHeader, boolean hasCols) {
    Table table = parseTable(tableLines);
    if (table == null || table.elements.isEmpty()) {
      return null;
    }

    List<Row> rows = table.getRows();
    int numCols = rows.stream().mapToInt(r -> r.cells.size()).max().orElse(0);
    if (numCols == 0) {
      return null;
    }

    int[] colWidths = new int[numCols];
    int[] specWidths = new int[numCols];
    for (Row row : rows) {
      for (int i = 0; i < row.cells.size(); i++) {
        Cell cell = row.cells.get(i);
        colWidths[i] =
            Math.max(colWidths[i], cell.content.codePointCount(0, cell.content.length()));
        specWidths[i] = Math.max(specWidths[i], cell.specifier.length());
      }
    }

    TableLayout layout = config.getTableLayout();
    if (layout == TableLayout.AUTO) {
      int maxRowWidth = 0;
      for (Row row : rows) {
        int rowWidth = 0;
        for (int i = 0; i < row.cells.size(); i++) {
          Cell cell = row.cells.get(i);
          rowWidth += specWidths[i] + 2 + colWidths[i]; // [spec]| space content
          if (i < row.cells.size() - 1) {
            rowWidth += 1; // gap
          }
        }
        maxRowWidth = Math.max(maxRowWidth, rowWidth);
      }
      if (maxRowWidth > config.getTableMaxLineWidth() && hasCols) {
        layout = TableLayout.EXPANDED;
      } else {
        layout = TableLayout.AUTO;
      }
    }

    if (layout == TableLayout.EXPANDED && !hasCols) {
      layout = TableLayout.AUTO; // Golden Rule: don't change semantics
    }

    List<String> result = new ArrayList<>();
    result.add(tableLines.get(0));

    int rowIndex = 0;
    for (int i = 0; i < table.elements.size(); i++) {
      TableElement element = table.elements.get(i);
      if (element instanceof CommentLine commentLine) {
        result.add(commentLine.content);
        continue;
      }

      Row row = (Row) element;
      if (layout == TableLayout.EXPANDED) {
        for (Cell cell : row.cells) {
          result.add(cell.specifier + "| " + cell.content);
        }
      } else {
        result.add(formatCompactRow(row, colWidths, specWidths));
      }

      if (rowIndex < rows.size() - 1) {
        boolean nextIsRow = false;
        for (int k = i + 1; k < table.elements.size(); k++) {
          if (table.elements.get(k) instanceof Row) {
            nextIsRow = true;
            break;
          }
        }

        if (nextIsRow) {
          boolean shouldAddBlankLine = false;
          if (config.getTableBlankLines() == TableBlankLines.ALL) {
            shouldAddBlankLine = true;
          } else if (config.getTableBlankLines() == TableBlankLines.HEADER && rowIndex == 0) {
            shouldAddBlankLine = true;
          }

          if (!explicitHeader && rowIndex == 0) {
            shouldAddBlankLine = table.hasImplicitHeaderAfterFirstRow;
          }

          if (shouldAddBlankLine) {
            result.add("");
          }
        }
      }
      rowIndex++;
    }

    result.add(tableLines.get(tableLines.size() - 1));
    return result;
  }

  private String formatCompactRow(Row row, int[] colWidths, int[] specWidths) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < row.cells.size(); i++) {
      Cell cell = row.cells.get(i);

      // Reserving space for specifiers to align pipes
      int specPadding = specWidths[i] - cell.specifier.length();
      sb.append(" ".repeat(specPadding)).append(cell.specifier);

      sb.append("| ");

      int currentWidth = cell.content.codePointCount(0, cell.content.length());
      int padding = colWidths[i] - currentWidth;

      if (cell.specifier.contains("^")) {
        int left = padding / 2;
        int right = padding - left;
        sb.append(" ".repeat(left)).append(cell.content).append(" ".repeat(right));
      } else if (cell.specifier.contains(">")) {
        sb.append(" ".repeat(padding)).append(cell.content);
      } else {
        sb.append(cell.content).append(" ".repeat(padding));
      }

      if (i < row.cells.size() - 1) {
        sb.append(" ");
      }
    }
    return sb.toString();
  }

  private Table parseTable(List<String> tableLines) {
    Table table = new Table();
    Row currentRow = null;

    for (int i = 1; i < tableLines.size() - 1; i++) {
      String line = tableLines.get(i);
      String trimmed = line.trim();

      if (trimmed.isBlank()) {
        if (currentRow != null) {
          table.elements.add(currentRow);
          if (table.getRows().size() == 1) {
            table.hasImplicitHeaderAfterFirstRow = true;
          }
          currentRow = null;
        }
        continue;
      }

      if (trimmed.startsWith("//")) {
        if (currentRow != null) {
          table.elements.add(currentRow);
          currentRow = null;
        }
        table.elements.add(new CommentLine(line));
        continue;
      }

      List<Cell> cellsInLine = parseCells(line);
      if (cellsInLine.isEmpty()) continue;

      boolean startsWithCellStart =
          trimmed.startsWith("|")
              || (!trimmed.isEmpty() && isSpecifierChar(trimmed.charAt(0)) && line.contains("|"));

      if (startsWithCellStart || currentRow == null) {
        if (currentRow != null) {
          table.elements.add(currentRow);
        }
        currentRow = new Row();
      }
      currentRow.cells.addAll(cellsInLine);
    }
    if (currentRow != null) {
      table.elements.add(currentRow);
    }
    return table;
  }

  private List<Cell> parseCells(String line) {
    String normalizedLine = line.replace('\t', ' ').stripTrailing();
    List<Cell> cells = new ArrayList<>();
    int pos = 0;
    while (pos < normalizedLine.length()) {
      int pipePos = findNextUnescapedPipe(normalizedLine, pos);
      if (pipePos == -1) break;

      int specStart = pipePos - 1;
      while (specStart >= 0 && isSpecifierChar(normalizedLine.charAt(specStart))) {
        if (specStart > 0 && normalizedLine.charAt(specStart - 1) == '|') break;
        specStart--;
      }
      String specifier = normalizedLine.substring(specStart + 1, pipePos).trim();

      int nextPipePos = findNextUnescapedPipe(normalizedLine, pipePos + 1);
      int contentEnd;
      if (nextPipePos == -1) {
        contentEnd = normalizedLine.length();
        pos = normalizedLine.length();
      } else {
        int nextSpecStart = nextPipePos - 1;
        while (nextSpecStart >= pipePos + 1
            && isSpecifierChar(normalizedLine.charAt(nextSpecStart))) {
          nextSpecStart--;
        }
        contentEnd = nextSpecStart + 1;
        pos = nextPipePos;
      }

      String content = normalizedLine.substring(pipePos + 1, contentEnd).trim();
      cells.add(new Cell(specifier, content));
    }
    return cells;
  }

  private int findNextUnescapedPipe(String line, int start) {
    for (int i = start; i < line.length(); i++) {
      if (line.charAt(i) == '|' && (i == 0 || line.charAt(i - 1) != '\\')) {
        return i;
      }
    }
    return -1;
  }

  private boolean isSpecifierChar(char c) {
    return "<>=^.0123456789,".indexOf(c) >= 0;
  }

  private interface TableElement {}

  private static class Table {
    List<TableElement> elements = new ArrayList<>();
    boolean hasImplicitHeaderAfterFirstRow = false;

    List<Row> getRows() {
      return elements.stream().filter(Row.class::isInstance).map(Row.class::cast).toList();
    }
  }

  private static class Row implements TableElement {
    final List<Cell> cells = new ArrayList<>();
  }

  @RequiredArgsConstructor
  private static class CommentLine implements TableElement {
    final String content;
  }

  @RequiredArgsConstructor
  private static class Cell {
    final String specifier;
    final String content;
  }
}
