package org.drjekyll.adocfmt.internal;

import lombok.RequiredArgsConstructor;
import org.drjekyll.adocfmt.internal.block.BlockDelimiter;
import org.drjekyll.adocfmt.internal.block.BlockTracker;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ListBulletsNormalizer implements Runnable {

  private final List<String> lines;

  public void run() {
    BlockTracker bt = new BlockTracker();
    List<String> result = new ArrayList<>(lines.size());
    List<String> listBuffer = new ArrayList<>();
    boolean listHasAsterisk = false;

    for (String line : lines) {
      if (bt.isOpen()) {
        flushListBuffer(result, listBuffer, listHasAsterisk);
        listBuffer.clear();
        listHasAsterisk = false;
        result.add(line);
        bt.tryClose(line);
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
      if (line.startsWith("- ") || line.startsWith("* ")) {
        if (line.startsWith("* ")) {
          listHasAsterisk = true;
        }
        listBuffer.add(line);
      } else {
        flushListBuffer(result, listBuffer, listHasAsterisk);
        listBuffer.clear();
        listHasAsterisk = false;
        result.add(line);
      }
    }
    flushListBuffer(result, listBuffer, listHasAsterisk);

    lines.clear();
    lines.addAll(result);
  }

  private void flushListBuffer(List<String> result, List<String> buffer, boolean hasAsterisk) {
    for (String line : buffer) {
      result.add(!hasAsterisk && line.startsWith("- ") ? "* " + line.substring(2) : line);
    }
  }
}
