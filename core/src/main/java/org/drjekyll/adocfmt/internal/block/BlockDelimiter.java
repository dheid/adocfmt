package org.drjekyll.adocfmt.internal.block;

import lombok.experimental.UtilityClass;

/**
 * Utility for detecting AsciiDoc block delimiter lines.
 *
 * <p>A block delimiter is a line of four or more identical delimiter characters ({@code -}, {@code
 * =}, {@code .}, {@code *}, {@code _}, {@code +}, or {@code /}).
 */
@UtilityClass
public class BlockDelimiter {

  private final String BLOCK_DELIMITER_CHARS = "-=.*_+/";

  /**
   * Returns {@code true} if {@code line} is a block delimiter.
   *
   * @param line the line to test; must not be {@code null}
   * @return {@code true} if the line consists of four or more identical delimiter characters
   */
  public boolean isBlockDelimiter(CharSequence line) {
    int len = line.length();
    if (len < 3) {
      return false;
    }
    char c = line.charAt(0);
    if (c == '`') {
      return len >= 3 && line.charAt(1) == '`' && line.charAt(2) == '`';
    }
    if (len < 4 || BLOCK_DELIMITER_CHARS.indexOf(c) < 0) {
      return false;
    }
    for (int i = 1; i < len; i++) {
      if (line.charAt(i) != c) {
        return false;
      }
    }
    return true;
  }
}
