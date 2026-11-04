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

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the difference between an original and a formatted document as line-based hunks, in the
 * style of {@code rustfmt --check}.
 *
 * <p>Each hunk starts with a {@code Diff in <file> at line <n>:} header, where {@code n} is the
 * line in the original file where the hunk begins. Removed lines are prefixed with {@code -}, added
 * lines with {@code +}, and unchanged context lines with a space.
 */
final class DiffRenderer {

  private static final int CONTEXT = 3;
  private static final String RED = "\u001B[31m";
  private static final String GREEN = "\u001B[32m";
  private static final String RESET = "\u001B[0m";

  private final boolean color;

  /**
   * @param color whether to highlight removed and added lines with ANSI colors
   */
  DiffRenderer(boolean color) {
    this.color = color;
  }

  /**
   * Renders all hunks that differ between {@code original} and {@code formatted}.
   *
   * @param label the file name shown in each hunk header
   * @param original the document as it is on disk
   * @param formatted the document as the formatter would write it
   * @return the rendered hunks, or an empty string if both documents are identical
   */
  String render(String label, String original, String formatted) {
    List<Op> ops = diff(splitLines(original), splitLines(formatted));
    StringBuilder out = new StringBuilder();
    int i = 0;
    while (i < ops.size()) {
      if (ops.get(i).kind() == Kind.EQUAL) {
        i++;
        continue;
      }
      int start = Math.max(0, i - CONTEXT);
      int stop = Math.min(ops.size(), lastChangeOfHunk(ops, i) + 1 + CONTEXT);
      out.append("Diff in ")
          .append(label)
          .append(" at line ")
          .append(ops.get(start).oldPos() + 1)
          .append(":\n");
      for (Op op : ops.subList(start, stop)) {
        appendLine(out, op);
      }
      i = stop;
    }
    return out.toString();
  }

  private static int lastChangeOfHunk(List<Op> ops, int firstChange) {
    int last = firstChange;
    int j = firstChange;
    while (j < ops.size()) {
      if (ops.get(j).kind() != Kind.EQUAL) {
        last = j;
        j++;
        continue;
      }
      int run = 0;
      while (j + run < ops.size() && ops.get(j + run).kind() == Kind.EQUAL) {
        run++;
      }
      if (j + run == ops.size() || run > 2 * CONTEXT) {
        break;
      }
      j += run;
    }
    return last;
  }

  private void appendLine(StringBuilder out, Op op) {
    String text = op.line().display();
    switch (op.kind()) {
      case DELETE -> appendColored(out, RED, "-" + text);
      case INSERT -> appendColored(out, GREEN, "+" + text);
      default -> out.append(' ').append(text);
    }
    out.append('\n');
    if (!op.line().terminated()) {
      out.append("\\ No newline at end of file\n");
    }
  }

  private void appendColored(StringBuilder out, String ansi, String text) {
    if (color) {
      out.append(ansi).append(text).append(RESET);
    } else {
      out.append(text);
    }
  }

  private static List<Line> splitLines(String text) {
    String[] parts = text.split("\n", -1);
    List<Line> lines = new ArrayList<>(parts.length);
    for (int i = 0; i < parts.length - 1; i++) {
      lines.add(new Line(parts[i], true));
    }
    String last = parts[parts.length - 1];
    if (!last.isEmpty()) {
      lines.add(new Line(last, false));
    }
    return lines;
  }

  /**
   * Walks both documents in step. On a mismatch, the lines up to the nearest pair of equal lines
   * are reported as removed and added. Formatter changes are small and local, so in practice this
   * finds the same diff as a minimal diff algorithm, and its cost depends on the size of each
   * change rather than on the total number of changes.
   */
  private static List<Op> diff(List<Line> a, List<Line> b) {
    List<Op> ops = new ArrayList<>();
    int i = 0;
    int j = 0;
    while (i < a.size() || j < b.size()) {
      if (i < a.size() && j < b.size() && a.get(i).equals(b.get(j))) {
        ops.add(new Op(Kind.EQUAL, a.get(i), i));
        i++;
        j++;
        continue;
      }
      int[] next = nextMatch(a, b, i, j);
      for (; i < next[0]; i++) {
        ops.add(new Op(Kind.DELETE, a.get(i), i));
      }
      for (; j < next[1]; j++) {
        ops.add(new Op(Kind.INSERT, b.get(j), i));
      }
    }
    return ops;
  }

  /** Finds the equal pair {@code a[x] == b[y]} closest to {@code (i, j)}, or the ends of both. */
  private static int[] nextMatch(List<Line> a, List<Line> b, int i, int j) {
    int maxDistance = (a.size() - i) + (b.size() - j);
    for (int distance = 1; distance <= maxDistance; distance++) {
      for (int skipA = 0; skipA <= distance; skipA++) {
        int x = i + skipA;
        int y = j + distance - skipA;
        if (x < a.size() && y < b.size() && a.get(x).equals(b.get(y))) {
          return new int[] {x, y};
        }
      }
    }
    return new int[] {a.size(), b.size()};
  }

  private enum Kind {
    EQUAL,
    DELETE,
    INSERT
  }

  /**
   * @param kind the edit type
   * @param line the line the edit applies to
   * @param oldPos the zero-based position in the original document where the edit applies
   */
  private record Op(Kind kind, Line line, int oldPos) {}

  /**
   * @param raw the line content without its {@code \n} terminator
   * @param terminated whether the line ends with {@code \n}
   */
  private record Line(String raw, boolean terminated) {
    String display() {
      return raw.endsWith("\r") ? raw.substring(0, raw.length() - 1) : raw;
    }
  }
}
