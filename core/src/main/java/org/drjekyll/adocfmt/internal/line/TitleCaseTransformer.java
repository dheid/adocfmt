package org.drjekyll.adocfmt.internal.line;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.drjekyll.adocfmt.AsciidocFormatterConfig;
import org.drjekyll.adocfmt.internal.SectionHeadingMatcher;

/**
 * Applies title-case formatting to ATX section headings and block titles.
 *
 * <p>Common short words (articles, conjunctions, prepositions) are kept in lower case unless they
 * are the first or last word of the title.
 */
public class TitleCaseTransformer implements LineTransformer {

  private static final Pattern MULTIPLE_SPACES = Pattern.compile(" +");

  // Words lowercased in title case (articles, conjunctions, short prepositions)
  private static final Set<String> TITLE_CASE_LOWERCASE =
      Set.of(
          "a", "an", "the", "and", "but", "or", "nor", "for", "yet", "so", "at", "by", "in", "of",
          "on", "to", "up", "as", "off", "out", "per", "via", "from", "with");

  /** {@inheritDoc} */
  @Override
  public boolean applies(AsciidocFormatterConfig config) {
    return config.isTitleCase();
  }

  /** {@inheritDoc} */
  @Override
  public String transform(String line) {
    Matcher matcher = SectionHeadingMatcher.createSectionHeadingMatcher(line);
    if (matcher.matches()) {
      return matcher.group(1) + ' ' + toTitleCase(matcher.group(2));
    }
    if (line.length() > 1
        && line.charAt(0) == '.'
        && line.charAt(1) != '.'
        && line.charAt(1) != ' ') {
      return '.' + toTitleCase(line.substring(1));
    }
    return line;
  }

  private static String toTitleCase(CharSequence text) {
    String[] words = MULTIPLE_SPACES.split(text, -1);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < words.length; i++) {
      if (i > 0) {
        sb.append(' ');
      }
      boolean forceCapitalize =
          i == 0 || i == words.length - 1 || (i > 0 && endsWithSentencePunctuation(words[i - 1]));
      sb.append(capitalizeWordForTitle(words[i], forceCapitalize));
    }
    return sb.toString();
  }

  private static boolean endsWithSentencePunctuation(String word) {
    char last = word.isEmpty() ? 0 : word.charAt(word.length() - 1);
    return last == '.' || last == '!' || last == '?' || last == ';';
  }

  private static String capitalizeWordForTitle(String word, boolean forceCapitalize) {
    if (word.isEmpty()) {
      return word;
    }
    if (word.contains("{") || word.contains("`") || word.contains("[")) {
      return word;
    }
    int colonIdx = word.indexOf(':');
    if (colonIdx > 0 && colonIdx < word.length() - 1) {
      return word;
    }
    int firstLetter =
        IntStream.range(0, word.length())
            .filter(i -> Character.isLetter(word.charAt(i)))
            .findFirst()
            .orElse(-1);
    if (firstLetter < 0) {
      return word;
    }
    StringBuilder coreBuilder = new StringBuilder();
    for (int i = firstLetter; i < word.length(); i++) {
      char c = word.charAt(i);
      if (Character.isLetter(c)) {
        coreBuilder.append(Character.toLowerCase(c));
      }
    }
    String core = coreBuilder.toString();
    if (!forceCapitalize && TITLE_CASE_LOWERCASE.contains(core)) {
      return word.toLowerCase(Locale.ROOT);
    }
    return word.substring(0, firstLetter)
        + Character.toUpperCase(word.charAt(firstLetter))
        + word.substring(firstLetter + 1);
  }
}
