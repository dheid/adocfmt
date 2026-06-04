package org.drjekyll.adocfmt.internal.setext;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

/**
 * Detects setext-style heading underline lines and returns the corresponding heading level.
 *
 * <p>Setext underlines use a single repeated character: {@code =} for level 0, {@code -} for level
 * 1, {@code ~} for level 2, {@code ^} for level 3, and {@code +} for level 4. The underline must be
 * at least as long as the title line.
 */
@UtilityClass
public class SetextUnderlineDetector {

  /**
   * Tests whether {@code underlineLine} is a setext underline for {@code titleCandidate}.
   *
   * @param titleCandidate the line that may be a heading title; must not be {@code null}
   * @param underlineLine the immediately following line to test; must not be {@code null}
   * @return the zero-based heading level (0 = {@code =}, 1 = {@code -}, 2 = {@code ~}, 3 = {@code
   *     ^}, 4 = {@code +}) if the pair forms a setext heading, or {@code null} if it does not
   */
  @Nullable
  public Integer detectSetextUnderline(CharSequence titleCandidate, CharSequence underlineLine) {
    if (titleCandidate.isEmpty()) {
      return null;
    }
    char first = titleCandidate.charAt(0);
    if (first == '='
        || first == '['
        || first == '.'
        || first == ':'
        || first == '*'
        || first == '-'
        || first == '|'
        || first == '+'
        || startsWithDoubleSlash(titleCandidate)) {
      return null;
    }
    if (underlineLine.isEmpty()) {
      return null;
    }
    char underlineChar = underlineLine.charAt(0);
    int level;
    switch (underlineChar) {
      case '=':
        level = 0;
        break;
      case '-':
        level = 1;
        break;
      case '~':
        level = 2;
        break;
      case '^':
        level = 3;
        break;
      case '+':
        level = 4;
        break;
      default:
        return null;
    }
    if (underlineLine.length() < titleCandidate.length()) {
      return null;
    }
    for (int j = 1; j < underlineLine.length(); j++) {
      if (underlineLine.charAt(j) != underlineChar) {
        return null;
      }
    }
    return level;
  }

  private boolean startsWithDoubleSlash(CharSequence seq) {
    if (seq.length() < "//".length()) {
      return false;
    }
    for (int i = 0; i < "//".length(); i++) {
      if (seq.charAt(i) != "//".charAt(i)) {
        return false;
      }
    }
    return true;
  }
}
