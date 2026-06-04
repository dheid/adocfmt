package org.drjekyll.adocfmt.internal.line;

import lombok.RequiredArgsConstructor;
import org.drjekyll.adocfmt.AsciidocFormatterConfig;

/** Removes trailing whitespace characters from every line. */
@RequiredArgsConstructor
public class TrailingWhitespaceRemover implements LineTransformer {

  /** {@inheritDoc} */
  @Override
  public boolean applies(AsciidocFormatterConfig config) {
    return config.isRemoveTrailingWhitespace();
  }

  /** {@inheritDoc} */
  @Override
  public String transform(String line) {
    return line.stripTrailing();
  }
}
