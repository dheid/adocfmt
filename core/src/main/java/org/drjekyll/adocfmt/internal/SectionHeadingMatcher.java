package org.drjekyll.adocfmt.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;

/**
 * Utility that creates {@link Matcher} instances for ATX-style section headings.
 *
 * <p>An ATX heading is a line that starts with one to six {@code =} signs followed by a space and
 * the heading text, for example {@code == Introduction}.
 */
@UtilityClass
public class SectionHeadingMatcher {

  // Section heading: = Title or == Title, etc.
  // Captured groups: (1) leading equals, (2) trimmed title text
  private final Pattern SECTION_HEADING = Pattern.compile("^(={1,6})\\s+(\\S.*?)\\s*$");

  /**
   * Returns a {@link Matcher} for {@code line} pre-applied against the ATX heading pattern.
   *
   * <p>Call {@link Matcher#matches()} on the result to test whether the line is an ATX heading.
   * When it matches, group 1 contains the leading {@code =} signs and group 2 contains the heading
   * text.
   *
   * @param line the line to test; must not be {@code null}
   * @return a configured {@link Matcher}; never {@code null}
   */
  @NonNull
  public Matcher createSectionHeadingMatcher(String line) {
    return SECTION_HEADING.matcher(line);
  }
}
