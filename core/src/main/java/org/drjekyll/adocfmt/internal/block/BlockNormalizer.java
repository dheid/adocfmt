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
package org.drjekyll.adocfmt.internal.block;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.drjekyll.adocfmt.internal.setext.SetextUnderlineDetector;

/**
 * Normalises block delimiter lines to the canonical four-character form.
 *
 * <p>Any delimiter line longer than four characters (e.g. {@code --------}) is shortened to exactly
 * four (e.g. {@code ----}). Delimiter lines that form a setext heading underline are left
 * unchanged.
 */
@RequiredArgsConstructor
public class BlockNormalizer implements Runnable {

  private final List<String> lines;

  /** Performs the block delimiter normalisation transformation. */
  public void run() {
    BlockTracker bt = new BlockTracker();

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (bt.isOpen()) {
        String closed = bt.tryClose(line);
        if (closed != null && !"`".equals(closed)) {
          lines.set(i, closed.repeat(4));
        }
      } else if (BlockDelimiter.isBlockDelimiter(line)) {
        if (line.charAt(0) == '`') {
          bt.open(line);
        } else if (line.length() > 4) {
          String prev = i == 0 ? null : lines.get(i - 1);
          boolean notSetextUnderline =
              prev == null
                  || prev.isBlank()
                  || SetextUnderlineDetector.detectSetextUnderline(prev, line) == null;
          if (notSetextUnderline) {
            lines.set(i, String.valueOf(line.charAt(0)).repeat(4));
            bt.open(lines.get(i));
          }
        } else {
          bt.open(line);
        }
      }
    }
  }
}
