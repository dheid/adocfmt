package org.drjekyll.adocfmt;

import java.util.function.Consumer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AsciidocFormatterTestSupport {

  public String apply(
      String input, Consumer<AsciidocFormatterConfig.AsciidocFormatterConfigBuilder> customizer) {
    AsciidocFormatterConfig.AsciidocFormatterConfigBuilder builder =
        AsciidocFormatterConfig.builder();
    customizer.accept(builder);
    try {
      return new AsciidocFormatter(builder.build()).format(input);
    } catch (UnsupportedLineEndingException e) {
      throw new RuntimeException(e);
    }
  }
}
