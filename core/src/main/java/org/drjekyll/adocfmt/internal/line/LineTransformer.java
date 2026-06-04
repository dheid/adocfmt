package org.drjekyll.adocfmt.internal.line;

import org.drjekyll.adocfmt.AsciidocFormatterConfig;

/**
 * Strategy interface for per-line AsciiDoc transformations.
 *
 * <p>Implementations declare whether they are enabled via {@link #applies(AsciidocFormatterConfig)}
 * and perform the actual text change in {@link #transform(String)}.
 */
public interface LineTransformer {

  /**
   * Returns {@code true} if this transformation should be applied for the given configuration.
   *
   * @param config the active formatter configuration; must not be {@code null}
   * @return {@code true} if this transformer is enabled
   */
  boolean applies(AsciidocFormatterConfig config);

  /**
   * Applies this transformation to a single line.
   *
   * <p>The line does not contain a trailing line separator.
   *
   * @param line the input line; must not be {@code null}
   * @return the transformed line, or the original line unchanged if no transformation is needed
   */
  String transform(String line);
}
