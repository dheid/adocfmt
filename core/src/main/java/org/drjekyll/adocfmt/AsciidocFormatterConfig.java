/*
 * Copyright 2026 Daniel Heid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drjekyll.adocfmt;

import lombok.Builder;
import lombok.Value;

/**
 * Immutable configuration controlling which formatting transformations the {@link
 * AsciidocFormatter} applies.
 *
 * <p>Create instances via the generated {@code builder()} method.
 */
@Builder
@Value
public class AsciidocFormatterConfig {

  /**
   * Whether to convert setext-style headings (underline with {@code =} or {@code -}) to ATX-style
   * ({@code =} prefix).
   */
  boolean normalizeSetextHeadings;

  /** Whether to collapse runs of more than one consecutive blank line into a single blank line. */
  boolean collapseConsecutiveBlankLines;

  /** Whether to reflow paragraph text so that each sentence starts on its own line. */
  boolean oneSentencePerLine;

  /** Whether to normalise block delimiter lines to exactly four characters (e.g. {@code ----}). */
  boolean normalizeBlockDelimiters;

  /**
   * Whether to remove trailing {@code =} signs from ATX headings (e.g. {@code == Title ==} → {@code
   * == Title}).
   */
  boolean removeTrailingHeaderEqualsSign;

  /** Whether to apply title-case formatting to section headings and block titles. */
  boolean titleCase;

  /** Whether to strip trailing whitespace characters from every line. */
  boolean removeTrailingWhitespace;

  /** Whether to normalise unordered list bullets from {@code - } to {@code * }. */
  boolean normalizeListBullets;

  /**
   * Whether to replace explicit ordered-list numbers ({@code 1. }, {@code 2. }, …) with the
   * AsciiDoc auto-numbering marker {@code . }.
   */
  boolean normalizeOrderedListMarkers;

  /** Whether to ensure each section heading is surrounded by exactly one blank line. */
  boolean ensureHeadingBlankLines;

  /**
   * Whether to wrap bare {@code [source]} or {@code [listing]} attribute lines in {@code ----}
   * delimiters when they are missing.
   */
  boolean ensureSourceDelimiters;
}
