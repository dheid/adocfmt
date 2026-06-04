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

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.drjekyll.adocfmt.internal.block.BlockDelimiter;
import org.drjekyll.adocfmt.internal.block.BlockTracker;

/**
 * Ensures that every ATX section heading is surrounded by exactly one blank line.
 *
 * <p>A blank line is inserted before the heading when the preceding line is non-empty, and after
 * the heading when the following line is non-empty. Lines inside delimited blocks are left
 * untouched.
 */
@RequiredArgsConstructor
public class HeadingBlankLinesEnsurer implements Runnable {
  private final List<String> lines;

  /** Performs the heading blank-line insertion transformation. */
  public void run() {
    List<String> result = new ArrayList<>(lines.size() + 8);
    BlockTracker bt = new BlockTracker();

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);

      if (bt.isOpen()) {
        result.add(line);
        bt.tryClose(line);
        continue;
      }
      if (BlockDelimiter.isBlockDelimiter(line)) {
        result.add(line);
        bt.open(line);
        continue;
      }

      if (SectionHeadingMatcher.createSectionHeadingMatcher(line).matches()) {
        if (!result.isEmpty() && !result.get(result.size() - 1).isEmpty()) {
          result.add("");
        }
        result.add(line);
        if (i + 1 < lines.size() && !lines.get(i + 1).isEmpty()) {
          result.add("");
        }
      } else {
        result.add(line);
      }
    }
    lines.clear();
    lines.addAll(result);
  }
}
