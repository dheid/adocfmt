package org.drjekyll.adocfmt.internal;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Collapses runs of more than one consecutive blank line into a single blank line.
 *
 * <p>Operates in-place on the list supplied at construction time.
 */
@RequiredArgsConstructor
public class BlankLinesCollapser implements Runnable {

  private final List<String> lines;

  /** Performs the blank-line collapsing transformation. */
  public void run() {
    int writeIdx = 0;
    int consecutiveBlank = 0;
    for (int readIdx = 0; readIdx < lines.size(); readIdx++) {
      String line = lines.get(readIdx);
      if (line.isBlank()) {
        consecutiveBlank++;
        if (consecutiveBlank <= 1) {
          lines.set(writeIdx++, line);
        }
      } else {
        consecutiveBlank = 0;
        lines.set(writeIdx++, line);
      }
    }
    if (writeIdx < lines.size()) {
      lines.subList(writeIdx, lines.size()).clear();
    }
  }
}
