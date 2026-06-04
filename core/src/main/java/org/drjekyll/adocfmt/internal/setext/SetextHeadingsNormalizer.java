package org.drjekyll.adocfmt.internal.setext;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.drjekyll.adocfmt.internal.block.BlockDelimiter;
import org.drjekyll.adocfmt.internal.block.BlockTracker;

/**
 * Converts setext-style headings to ATX-style headings.
 *
 * <p>A setext heading consists of a title line followed by an underline of {@code =} (level 1),
 * {@code -} (level 2), {@code ~} (level 3), {@code ^} (level 4), or {@code +} (level 5). Each such
 * pair is replaced by the corresponding ATX prefix ({@code = }, {@code == }, etc.). Heading pairs
 * inside delimited blocks are left unchanged.
 */
@RequiredArgsConstructor
public class SetextHeadingsNormalizer implements Runnable {

  // ATX heading prefixes for setext -> ATX conversion: ATX_PREFIX[n] = "=".repeat(n+1) + " "
  private static final String[] ATX_PREFIX = {"= ", "== ", "=== ", "==== ", "===== ", "====== "};

  private final List<String> lines;

  /** Performs the setext-to-ATX heading conversion. */
  @Override
  public void run() {
    BlockTracker bt = new BlockTracker();
    int readIdx = 0;
    int writeIdx = 0;
    while (readIdx < lines.size()) {
      String line = lines.get(readIdx);
      if (bt.isOpen()) {
        lines.set(writeIdx++, line);
        bt.tryClose(line);
        readIdx++;
        continue;
      }
      if (BlockDelimiter.isBlockDelimiter(line)) {
        lines.set(writeIdx++, line);
        bt.open(line);
        readIdx++;
        continue;
      }
      if (readIdx + 1 < lines.size()) {
        Integer level = SetextUnderlineDetector.detectSetextUnderline(line, lines.get(readIdx + 1));
        if (level != null) {
          lines.set(writeIdx++, ATX_PREFIX[level] + line);
          readIdx += 2;
          continue;
        }
      }
      lines.set(writeIdx++, line);
      readIdx++;
    }
    if (writeIdx < lines.size()) {
      lines.subList(writeIdx, lines.size()).clear();
    }
  }
}
