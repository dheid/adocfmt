package org.drjekyll.adocfmt.internal.line;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.drjekyll.adocfmt.AsciidocFormatterConfig;
import org.drjekyll.adocfmt.internal.SectionHeadingMatcher;

/**
 * Removes trailing {@code =} signs from ATX section headings.
 *
 * <p>Both symmetric headings ({@code == Title ==}) and headings with a dangling trailing {@code =}
 * are normalised to the asymmetric form ({@code == Title}).
 */
public class TrailingHeaderEqualsSignRemover implements LineTransformer {

  // Heading with trailing = signs: == Title == or === Title ===
  // Captured groups: (1) leading equals, (2) title text (trimmed)
  private static final Pattern SYMMETRIC_HEADING =
      Pattern.compile("^(={1,6})\\s+(.*\\S)\\s+=+\\s*$");

  /** {@inheritDoc} */
  @Override
  public boolean applies(AsciidocFormatterConfig config) {
    return config.isRemoveTrailingHeaderEqualsSign();
  }

  /** {@inheritDoc} */
  @Override
  public String transform(String line) {
    Matcher symmetric = SYMMETRIC_HEADING.matcher(line);
    if (symmetric.matches()) {
      return symmetric.group(1) + ' ' + symmetric.group(2);
    }
    Matcher section = SectionHeadingMatcher.createSectionHeadingMatcher(line);
    if (section.matches()) {
      return section.group(1) + ' ' + section.group(2);
    }
    return line;
  }
}
