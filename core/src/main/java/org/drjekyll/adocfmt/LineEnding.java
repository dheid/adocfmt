package org.drjekyll.adocfmt;

import java.util.Optional;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the line ending styles recognised by the formatter.
 *
 * <p>Use {@link #determineLineEnding(CharSequence)} to detect the style used in a document.
 */
@RequiredArgsConstructor
@Getter
public enum LineEnding {

  /** Unix line ending ({@code \n}). */
  LF("\n", Pattern.compile("\n")),

  /** Windows line ending ({@code \r\n}). */
  CRLF("\r\n", Pattern.compile("\r\n")),

  /** Legacy Mac line ending ({@code \r}). */
  CR("\r", Pattern.compile("\r"));

  private final String lineEnding;

  private final Pattern splitPattern;

  /**
   * Analyses {@code charSequence} and returns the single line ending style it uses.
   *
   * @param charSequence the text to analyse; must not be {@code null}
   * @return an {@link Optional} containing the detected {@link LineEnding}, or {@link
   *     Optional#empty()} if the text contains no line endings
   * @throws UnsupportedLineEndingException if the text mixes more than one line ending style
   */
  public static Optional<LineEnding> determineLineEnding(CharSequence charSequence)
      throws UnsupportedLineEndingException {
    int crlf = 0;
    int lf = 0;
    int cr = 0;
    for (int i = 0; i < charSequence.length(); i++) {
      char ch = charSequence.charAt(i);
      if (ch == '\r') {
        if (i + 1 < charSequence.length() && charSequence.charAt(i + 1) == '\n') {
          crlf++;
          i++;
        } else {
          cr++;
        }
      } else if (ch == '\n') {
        lf++;
      }
    }
    int styles = (crlf > 0 ? 1 : 0) + (lf > 0 ? 1 : 0) + (cr > 0 ? 1 : 0);
    if (styles == 0) {
      return Optional.empty();
    }
    if (styles > 1) {
      throw new UnsupportedLineEndingException("Mixed line endings in file");
    }
    if (crlf > 0) {
      return Optional.of(CRLF);
    }
    if (lf > 0) {
      return Optional.of(LF);
    }
    return Optional.of(CR);
  }
}
