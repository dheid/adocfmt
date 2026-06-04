package org.drjekyll.adocfmt.internal.line;

import org.drjekyll.adocfmt.AsciidocFormatterConfig;

/** Normalises AsciiDoc unordered list bullets from {@code - } to {@code * }. */
public class ListBulletsNormalizer implements LineTransformer {

  /** {@inheritDoc} */
  @Override
  public boolean applies(AsciidocFormatterConfig config) {
    return config.isNormalizeListBullets();
  }

  /** {@inheritDoc} */
  @Override
  public String transform(String line) {
    if (line.startsWith("- ")) {
      return "* " + line.substring(2);
    }
    return line;
  }
}
