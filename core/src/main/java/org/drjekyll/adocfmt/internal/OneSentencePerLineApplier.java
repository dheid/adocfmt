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
package org.drjekyll.adocfmt.internal;

import java.util.*;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.drjekyll.adocfmt.internal.block.BlockDelimiter;
import org.drjekyll.adocfmt.internal.block.BlockTracker;
import org.drjekyll.adocfmt.internal.setext.SetextUnderlineDetector;

/**
 * Reflows paragraph text so that each sentence occupies exactly one line.
 *
 * <p>Consecutive non-blank, non-special lines are joined into a single paragraph string and then
 * split at sentence boundaries. Common English and German abbreviations are recognised to avoid
 * spurious splits. Lines inside delimited blocks, list items, headings, and other structural
 * AsciiDoc elements are passed through unchanged.
 */
@RequiredArgsConstructor
public class OneSentencePerLineApplier implements Runnable {
  private static final Pattern MULTI_WHITESPACE = Pattern.compile("\\s+");

  private final List<String> lines;

  // Known abbreviations that end with a period but do not end a sentence
  private static final Set<String> ABBREVIATIONS =
      Set.of(
          // English -- titles and honorifics
          "mr",
          "mrs",
          "ms",
          "dr",
          "prof",
          "sr",
          "jr",
          "capt",
          "col",
          "gen",
          "gov",
          "hon",
          "maj",
          "sgt",
          "atty",
          "esq",
          // English -- Latin / scholarly
          "eg",
          "ie",
          "etc",
          "cf",
          "viz",
          "ibid",
          "nb",
          "vs",
          "al",
          "ed",
          "eds",
          "pp",
          "et",
          // English -- months
          "jan",
          "feb",
          "mar",
          "apr",
          "jun",
          "jul",
          "aug",
          "sep",
          "sept",
          "oct",
          "nov",
          "dec",
          // English -- organisations and business
          "co",
          "corp",
          "inc",
          "ltd",
          "llc",
          "bros",
          "assn",
          "assoc",
          "mfg",
          "mfr",
          "mgr",
          "dir",
          "admin",
          "govt",
          "natl",
          "intl",
          "univ",
          "dept",
          // English -- document and publishing
          "anon",
          "app",
          "bk",
          "ch",
          "diss",
          "fig",
          "intro",
          "misc",
          "no",
          "orig",
          "proc",
          "pub",
          "ref",
          "repr",
          "rev",
          "sec",
          "ser",
          "supp",
          "trans",
          "vol",
          // English -- addresses and geography
          "apt",
          "ave",
          "bldg",
          "blvd",
          "ft",
          "hwy",
          "ln",
          "mt",
          "pkwy",
          "pl",
          "rd",
          "rte",
          "st",
          "ste",
          "terr",
          // English -- general and technical
          "approx",
          "art",
          "avg",
          "cont",
          "diam",
          "est",
          "ext",
          "freq",
          "hr",
          "hrs",
          "ht",
          "max",
          "min",
          "pt",
          "qty",
          "spec",
          "sq",
          "tech",
          "temp",
          "wt",
          "yr",
          "yrs",
          // German -- general
          "ca",
          "bzw",
          "bspw",
          "bzgl",
          "dh",
          "evtl",
          "ggf",
          "ggü",
          "gem",
          "lt",
          "sog",
          "usw",
          "ua",
          "uvm",
          "va",
          "vgl",
          "zt",
          "zzt",
          "zzgl",
          "allg",
          "betr",
          "einschl",
          "entspr",
          "exkl",
          "inkl",
          "insb",
          "insg",
          "mind",
          // German -- document structure
          "abb",
          "abs",
          "abt",
          "anm",
          "aufl",
          "ebd",
          "hrsg",
          "jg",
          "jh",
          "kap",
          "nr",
          "tab");

  /** Performs the one-sentence-per-line transformation. */
  public void run() {
    Collection<String> result = new ArrayList<>(lines.size());
    Collection<String> paragraphBuffer = new ArrayList<>();
    BlockTracker bt = new BlockTracker();

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);

      if (bt.isOpen()) {
        result.add(line);
        bt.tryClose(line);
        continue;
      }

      if (BlockDelimiter.isBlockDelimiter(line)) {
        flushParagraph(paragraphBuffer, result);
        result.add(line);
        bt.open(line);
        continue;
      }

      if (i + 1 < lines.size()
          && SetextUnderlineDetector.detectSetextUnderline(line, lines.get(i + 1)) != null) {
        flushParagraph(paragraphBuffer, result);
        result.add(line);
        result.add(lines.get(i + 1));
        i++;
        continue;
      }

      if (line.isBlank() || isSpecialLine(line)) {
        flushParagraph(paragraphBuffer, result);
        result.add(line);
        continue;
      }

      paragraphBuffer.add(line);
    }

    flushParagraph(paragraphBuffer, result);
    lines.clear();
    lines.addAll(result);
  }

  private static void flushParagraph(Collection<String> buffer, Collection<String> result) {
    if (buffer.isEmpty()) {
      return;
    }
    String joined = MULTI_WHITESPACE.matcher(String.join(" ", buffer)).replaceAll(" ").trim();
    result.addAll(splitIntoSentences(joined));
    buffer.clear();
  }

  private static List<String> splitIntoSentences(String text) {
    if (text.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> sentences = new ArrayList<>();
    int start = 0;
    int i = 0;

    while (i < text.length()) {
      char c = text.charAt(i);

      if (c == '.' || c == '!' || c == '?') {

        if (c == '.' && i + 1 < text.length() && text.charAt(i + 1) == '.') {
          i++;
          while (i < text.length() && text.charAt(i) == '.') {
            i++;
          }
          continue;
        }

        if (c == '.' && isAbbreviationContext(text, i)) {
          i++;
          continue;
        }

        int j = i + 1;
        while (j < text.length() && isSentenceClosingChar(text.charAt(j))) {
          j++;
        }

        if (j >= text.length()) {
          i = j;
          continue;
        }

        if (Character.isWhitespace(text.charAt(j))) {
          int k = j;
          while (k < text.length() && Character.isWhitespace(text.charAt(k))) {
            k++;
          }
          // For '.' require uppercase/digit to avoid splitting on abbreviations.
          // For '!' and '?' always split -- they are unambiguous sentence terminators.
          if (c != '.'
              || k >= text.length()
              || Character.isUpperCase(text.charAt(k))
              || Character.isDigit(text.charAt(k))) {
            String sentence = text.substring(start, j).trim();
            if (!sentence.isEmpty()) {
              sentences.add(sentence);
            }
            start = k;
            i = k;
            continue;
          }
        }
      }

      i++;
    }

    String remaining = text.substring(start).trim();
    if (!remaining.isEmpty()) {
      sentences.add(remaining);
    }
    return sentences;
  }

  private static boolean isAbbreviationContext(String text, int dotPos) {
    if (dotPos > 0 && Character.isDigit(text.charAt(dotPos - 1))) {
      return true;
    }
    int wordStart = dotPos - 1;
    while (wordStart >= 0 && Character.isLetter(text.charAt(wordStart))) {
      wordStart--;
    }
    wordStart++;
    if (wordStart >= dotPos) {
      return false;
    }
    String word = text.substring(wordStart, dotPos);
    return word.length() == 1
        || ABBREVIATIONS.contains(word.toLowerCase(Locale.ROOT)); // Initials (e.g., A. Smith)
  }

  private static boolean isSentenceClosingChar(char c) {
    return c == ')' || c == ']' || c == '"' || c == '\'' || c == '\u2019' || c == '\u201D';
  }

  static boolean isSpecialLine(String line) {
    if (line.isEmpty()) {
      return false;
    }
    char first = line.charAt(0);
    if (first == '=' || first == '[' || first == '|' || first == ' ' || first == '\t') {
      return true;
    }
    if (line.startsWith("//")
        || line.startsWith("<<<")
        || "'''".equals(line)
        || "+".equals(line)
        || line.endsWith(" +")) {
      return true;
    }
    if (first == ':' && line.length() > 1 && line.charAt(1) != ':') {
      return true;
    }
    if (first == '.' || first == '*' || first == '-') {
      if (line.length() > 1 && line.charAt(1) != first && line.charAt(1) != ' ') {
        if (first == '.') {
          return true; // Block title (.Title)
        }
      }
      // Treat list items as special lines
      if (line.length() > 1 && line.charAt(1) == ' ') {
        return true;
      }
      int i = 1;
      while (i < line.length() && line.charAt(i) == first) {
        i++;
      }
      return i == line.length() && i >= 3
          || i < line.length() && line.charAt(i) == ' '; // Horizontal rule (--- or ***)
    }
    if (Character.isDigit(first)) {
      int i = 1;
      while (i < line.length() && Character.isDigit(line.charAt(i))) {
        i++;
      }
      return i + 1 < line.length()
          && line.charAt(i) == '.'
          && (line.charAt(i + 1) == ' ' || line.charAt(i + 1) == '\t');
    }
    return isBlockMacroOrTerm(line);
  }

  private static boolean isBlockMacroOrTerm(CharSequence line) {
    int len = line.length();
    int i = 0;
    while (i < len) {
      char c = line.charAt(i);
      if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c >= '0' && c <= '9') {
        i++;
      } else {
        break;
      }
    }
    return i > 0 && i + 1 < len && line.charAt(i) == ':' && line.charAt(i + 1) == ':';
  }
}
