package org.drjekyll.adocfmt.internal;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.drjekyll.adocfmt.internal.block.BlockDelimiter;
import org.drjekyll.adocfmt.internal.block.BlockTracker;

@RequiredArgsConstructor
public class ListBulletsNormalizer implements Runnable {

  /** A bullet-like line that continues a paragraph is paragraph text, not a list item. */
  private enum Context {
    BLOCK_START,
    PARAGRAPH,
    LIST
  }

  private final List<String> lines;

  public void run() {
    BlockTracker bt = new BlockTracker();
    List<String> result = new ArrayList<>(lines.size());
    List<String> listBuffer = new ArrayList<>();
    boolean listHasAsterisk = false;
    Context context = Context.BLOCK_START;

    for (String line : lines) {
      if (bt.isOpen()) {
        flushListBuffer(result, listBuffer, listHasAsterisk);
        listBuffer.clear();
        listHasAsterisk = false;
        result.add(line);
        if (bt.tryClose(line) != null) {
          context = Context.BLOCK_START;
        }
        continue;
      }
      if (BlockDelimiter.isBlockDelimiter(line)) {
        flushListBuffer(result, listBuffer, listHasAsterisk);
        listBuffer.clear();
        listHasAsterisk = false;
        result.add(line);
        bt.open(line);
        continue;
      }
      if ((line.startsWith("- ") || line.startsWith("* ")) && context != Context.PARAGRAPH) {
        if (line.startsWith("* ")) {
          listHasAsterisk = true;
        }
        listBuffer.add(line);
        context = Context.LIST;
      } else {
        flushListBuffer(result, listBuffer, listHasAsterisk);
        listBuffer.clear();
        listHasAsterisk = false;
        result.add(line);
        context = next(context, line);
      }
    }
    flushListBuffer(result, listBuffer, listHasAsterisk);

    lines.clear();
    lines.addAll(result);
  }

  private static Context next(Context context, String line) {
    if (line.isBlank() || line.matches("=+ .*")) {
      return Context.BLOCK_START;
    }
    if (context == Context.BLOCK_START && isBlockMetadata(line)) {
      return Context.BLOCK_START;
    }
    return context == Context.LIST ? Context.LIST : Context.PARAGRAPH;
  }

  private static boolean isBlockMetadata(String line) {
    return line.startsWith("[")
        || line.startsWith(":")
        || line.startsWith("//")
        || line.matches("\\.[^. ].*");
  }

  private void flushListBuffer(List<String> result, List<String> buffer, boolean hasAsterisk) {
    for (String line : buffer) {
      result.add(!hasAsterisk && line.startsWith("- ") ? "* " + line.substring(2) : line);
    }
  }
}
