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
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.drjekyll.adocfmt.internal.block.BlockDelimiter;
import org.drjekyll.adocfmt.internal.block.BlockTracker;

/**
 * Ensures that {@code [source]} and {@code [listing]} attribute blocks are wrapped in {@code ----}
 * delimiters.
 *
 * <p>When a source or listing attribute line is immediately followed by code without block
 * delimiters, an opening {@code ----} is inserted before the code and a closing {@code ----} after
 * the last non-blank line. Lines already inside a delimited block are left untouched.
 */
@RequiredArgsConstructor
public class SourceDelimiter implements Runnable {

  private final List<String> lines;

  // Source / listing block attribute lines: [source], [source,java], [listing],
  // [source%linenums,java], [source#id,java], etc.
  private static final Pattern SOURCE_BLOCK_ATTR =
      Pattern.compile("^\\[(source|listing)[,\\]%#].*");

  /** Performs the source-delimiter insertion transformation. */
  public void run() {
    Collection<String> result = new ArrayList<>(lines.size() + 8);
    BlockTracker bt = new BlockTracker();
    int i = 0;
    while (i < lines.size()) {
      String line = lines.get(i);

      if (bt.isOpen()) {
        result.add(line);
        bt.tryClose(line);
        i++;
        continue;
      }

      if (BlockDelimiter.isBlockDelimiter(line)) {
        result.add(line);
        bt.open(line);
        i++;
        continue;
      }

      if (SOURCE_BLOCK_ATTR.matcher(line).matches()) {
        result.add(line);
        i++;
        if (i < lines.size()) {
          String next = lines.get(i);
          if (BlockDelimiter.isBlockDelimiter(next)) {
            result.add(next);
            bt.open(next);
            i++;
          } else if (!next.isBlank() && !next.startsWith("[")) {
            result.add("----");
            while (i < lines.size() && !lines.get(i).isBlank()) {
              result.add(lines.get(i));
              i++;
            }
            result.add("----");
          }
        }
        continue;
      }

      result.add(line);
      i++;
    }
    lines.clear();
    lines.addAll(result);
  }
}
