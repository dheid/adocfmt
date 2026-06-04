package org.drjekyll.adocfmt.internal.line;

import org.drjekyll.adocfmt.AsciidocFormatterConfig;

/**
 * Replaces explicit ordered-list numbers (e.g. {@code 1. }, {@code 2. }) with the AsciiDoc
 * auto-numbering marker {@code . }.
 */
public class OrderedListMarkerNormalizer implements LineTransformer {

  /** {@inheritDoc} */
  @Override
  public boolean applies(AsciidocFormatterConfig config) {
    return config.isNormalizeOrderedListMarkers();
  }

  /** {@inheritDoc} */
  @Override
  public String transform(String line) {
    if (line.isEmpty() || line.charAt(0) < '0' || line.charAt(0) > '9') {
      return line;
    }
    int i = 1;
    while (i < line.length() && line.charAt(i) >= '0' && line.charAt(i) <= '9') {
      i++;
    }
    if (i + 1 >= line.length() || line.charAt(i) != '.') {
      return line;
    }
    char sep = line.charAt(i + 1);
    if (sep != ' ' && sep != '\t') {
      return line;
    }
    return ". " + line.substring(i + 2);
  }
}
