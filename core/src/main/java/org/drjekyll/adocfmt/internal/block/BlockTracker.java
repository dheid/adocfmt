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

/**
 * Tracks whether the formatter is currently inside an open delimited block.
 *
 * <p>Call {@link #open(CharSequence)} when a block-opening delimiter is encountered and {@link
 * #tryClose(CharSequence)} on each subsequent line to detect the matching closing delimiter.
 */
public class BlockTracker {
  private char delimChar = '\0';

  /**
   * Returns {@code true} when a block is open, i.e. an opening delimiter has been seen but no
   * matching closing delimiter has been encountered yet.
   *
   * @return {@code true} if currently inside a delimited block
   */
  public boolean isOpen() {
    return delimChar != '\0';
  }

  /**
   * Records the start of a new delimited block.
   *
   * @param line the opening delimiter line; the first character identifies the delimiter character
   */
  public void open(CharSequence line) {
    delimChar = line.charAt(0);
  }

  /**
   * Attempts to close the currently open block by matching {@code line} against the opening
   * delimiter character.
   *
   * <p>If {@code line} is a delimiter of four or more identical characters matching the opening
   * delimiter, the block is closed and the single-character delimiter string is returned.
   *
   * @param line the candidate closing delimiter line
   * @return the single-character delimiter string if the block was closed, or {@code null} if the
   *     line does not match
   */
  public String tryClose(CharSequence line) {
    int minLen = delimChar == '`' ? 3 : 4;
    if (delimChar != '\0' && line.length() >= minLen && isAllSameChar(line, delimChar)) {
      String closed = String.valueOf(delimChar);
      delimChar = '\0';
      return closed;
    }
    return null;
  }

  private static boolean isAllSameChar(CharSequence line, char c) {
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) != c) {
        return false;
      }
    }
    return true;
  }
}
