package org.drjekyll.adocfmt;

/** Defines how table cells are laid out in the source. */
public enum TableLayout {
  /** If every row fits within the line width, keep it compact. Otherwise, use one line per cell. */
  AUTO,
  /** Always one source line per cell. */
  EXPANDED,
  /** Keep the table's current line layout. */
  PRESERVE
}
