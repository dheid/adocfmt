package org.drjekyll.adocfmt;

/** Defines where blank lines are placed within a table. */
public enum TableBlankLines {
  /** No blank lines between rows. */
  NONE,
  /** One blank line between the header and the body rows. */
  HEADER,
  /** Blank lines between every row. */
  ALL,
  /** Leave existing blank lines unchanged. */
  PRESERVE
}
