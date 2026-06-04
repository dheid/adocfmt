package org.drjekyll.adocfmt.internal.line;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.drjekyll.adocfmt.AsciidocFormatterConfig;
import org.drjekyll.adocfmt.internal.block.BlockDelimiter;
import org.drjekyll.adocfmt.internal.block.BlockTracker;

/**
 * Orchestrates the per-line transformations defined by {@link LineTransformer} implementations.
 *
 * <p>Lines inside delimited blocks receive only the trailing-whitespace transformation; all other
 * transformations are skipped for block content to preserve code and verbatim text.
 */
@RequiredArgsConstructor
public class PerLineTransformer implements Runnable {

  private static final TrailingWhitespaceRemover TRAILING_WHITESPACE_REMOVER =
      new TrailingWhitespaceRemover();
  private static final TitleCaseTransformer TITLE_CASE_TRANSFORMER = new TitleCaseTransformer();
  private static final OrderedListMarkerNormalizer ORDERED_LIST_MARKER_NORMALIZER =
      new OrderedListMarkerNormalizer();
  private static final TrailingHeaderEqualsSignRemover TRAILING_HEADER_EQUALS_SIGN_REMOVER =
      new TrailingHeaderEqualsSignRemover();

  private final List<String> lines;

  private final AsciidocFormatterConfig config;

  /** Iterates over all lines and applies each enabled {@link LineTransformer}. */
  @Override
  public void run() {
    BlockTracker bt = new BlockTracker();
    for (int i = 0; i < lines.size(); i++) {
      applyLineTransformation(i, TRAILING_WHITESPACE_REMOVER);
      if (bt.isOpen()) {
        bt.tryClose(lines.get(i));
      } else if (BlockDelimiter.isBlockDelimiter(lines.get(i))) {
        bt.open(lines.get(i));
      } else {
        applyLineTransformation(i, TRAILING_HEADER_EQUALS_SIGN_REMOVER);
        applyLineTransformation(i, TITLE_CASE_TRANSFORMER);
        applyLineTransformation(i, ORDERED_LIST_MARKER_NORMALIZER);
      }
    }
  }

  private void applyLineTransformation(int lineNo, LineTransformer lineTransformer) {
    if (lineTransformer.applies(config)) {
      lines.set(lineNo, lineTransformer.transform(lines.get(lineNo)));
    }
  }
}
