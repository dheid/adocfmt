# adocfmt

An opinionated AsciiDoc formatter for Java and the command line.

## Features

adocfmt applies a configurable set of transformations to AsciiDoc source files.
The following transformations are available (default state shown):

|          Transformation          | Default |                               Description                                |
|----------------------------------|---------|--------------------------------------------------------------------------|
| Normalize setext headings        | **on**  | Convert underline-style headings to ATX-style (`=` prefix)               |
| Collapse consecutive blank lines | **on**  | Reduce runs of multiple blank lines to a single blank line               |
| One sentence per line            | **on**  | Reflow paragraph text so each sentence starts on its own line            |
| Normalize block delimiters       | **on**  | Shorten delimiter lines to exactly four characters (e.g. `----`)         |
| Remove trailing header `=` signs | **on**  | Strip trailing `=` from headings (e.g. `== Title ==` → `== Title`)       |
| Remove trailing whitespace       | **on**  | Strip trailing whitespace from every line                                |
| Ensure heading blank lines       | **on**  | Surround each section heading with exactly one blank line                |
| Title case                       | off     | Apply title-case formatting to section headings and block titles         |
| Normalize list bullets           | off     | Normalise unordered list bullets from `- ` to `* `                       |
| Normalize ordered list markers   | off     | Replace explicit numbers (`1. `, `2. `) with the auto-number marker `. ` |
| Ensure source delimiters         | off     | Wrap bare `[source]`/`[listing]` blocks in `----` delimiters             |

**Invariants guaranteed by every run:**

- **Idempotent:** `format(format(x)) == format(x)`.
- **Safe:** Delimited blocks (code, literal, passthrough), comments, and directives are never modified.
- **Semantic:** Input and output render to identical normalised HTML (verified via AsciidoctorJ in tests).

## Code Style

This section describes the AsciiDoc style that adocfmt enforces.
The rationale behind each rule is consistent diffs, readable source, and
unambiguous structure -- not aesthetic preference.

### ATX Headings

Use `=`-prefixed headings (ATX style) instead of underline-style (setext) headings.
The level maps directly to the number of `=` signs: one for the document title, two
for the first section level, and so on.

```asciidoc
// Before
My Document Title
=================

Introduction
------------

// After
= My Document Title

== Introduction
```

Trailing `=` signs are also removed:

```asciidoc
// Before
== Installation ==

// After
== Installation
```

### Blank Lines Around Headings

Every section heading is surrounded by exactly one blank line on each side.
This makes section boundaries visible at a glance and avoids parser ambiguity.

```asciidoc
// Before
Some text.
== Next Section
More text.

// After
Some text.

== Next Section

More text.
```

### One Sentence Per Line

Each sentence in a paragraph is placed on its own line.
This produces minimal, meaningful diffs: editing one sentence touches exactly one line,
and adding a sentence does not reflow the paragraph.

```asciidoc
// Before
AsciiDoc is a lightweight markup language. It is used for writing documentation. It can be converted to HTML, PDF, and other formats.

// After
AsciiDoc is a lightweight markup language.
It is used for writing documentation.
It can be converted to HTML, PDF, and other formats.
```

Common abbreviations (e.g. `Dr.`, `etc.`, `ca.`) are recognised and do not trigger a split.

### Consecutive Blank Lines

At most one consecutive blank line is permitted.
Multiple blank lines carry no structural meaning in AsciiDoc and are collapsed to one.

```asciidoc
// Before
First paragraph.



Second paragraph.

// After
First paragraph.

Second paragraph.
```

### Block Delimiters

Block delimiter lines use exactly four characters, regardless of how many were written originally.
This removes visual noise and prevents mismatched delimiter lengths from silently breaking block structure.

```asciidoc
// Before
--------
$ echo hello
--------

// After
----
$ echo hello
----
```

### Source Blocks (optional, off by default)

`[source]` and `[listing]` attribute lines that are not followed by `----` delimiters
have them added automatically, ensuring the code is rendered as a verbatim block.

```asciidoc
// Before
[source,java]
int x = 1;

// After
[source,java]
----
int x = 1;
----
```

### List Bullets (optional, off by default)

Unordered list items use `*` as the bullet character.
The `-` form is converted for consistency.

```asciidoc
// Before
- Item A
- Item B

// After
* Item A
* Item B
```

### Ordered Lists (optional, off by default)

Ordered list items use the AsciiDoc auto-numbering marker `.` instead of explicit numbers.
This prevents numbering from going out of sync when items are reordered.

```asciidoc
// Before
1. First
2. Second
3. Third

// After
. First
. Second
. Third
```

### Title Case (optional, off by default)

Section headings and block titles are formatted in title case.
Short words (articles, conjunctions, short prepositions) are lowercased unless they
appear as the first or last word.

```asciidoc
// Before
== getting started with the cli

// After
== Getting Started with the CLI
```

## Installation

Requires **Java 17+**.

### Debian / Ubuntu

Download the `.deb` from the latest release and run:

```bash
sudo apt install ./adocfmt_<version>_all.deb
```

### Fedora / RHEL / CentOS

Download the `.rpm` from the latest release and run:

```bash
sudo dnf install ./adocfmt-<version>.noarch.rpm
```

### macOS (Homebrew)

```bash
brew install drjekyll-org/tap/adocfmt
```

### Windows

Download `adocfmt.exe` from the latest release. Ensure Java 17+ is on your PATH.

### Universal (Any Platform)

Download `adocfmt.jar` from the latest release and run:

```bash
java -jar adocfmt.jar [args]
```

## Usage

### Java Library

Requires Java 17+. Add the core module to your project:

```xml

<dependency>
    <groupId>org.drjekyll</groupId>
    <artifactId>adocfmt</artifactId>
    <version>0.1.0</version>
</dependency>
```

Build an `AsciidocFormatterConfig` with the desired transformations enabled, then create an `AsciidocFormatter` and call
one of its `format` overloads:

```java
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.drjekyll.adocfmt.AsciidocFormatter;
import org.drjekyll.adocfmt.AsciidocFormatterConfig;
import org.drjekyll.adocfmt.UnsupportedLineEndingException;

AsciidocFormatterConfig config = AsciidocFormatterConfig.builder()
        .normalizeSetextHeadings(true)
        .collapseConsecutiveBlankLines(true)
        .oneSentencePerLine(true)
        .normalizeBlockDelimiters(true)
        .removeTrailingHeaderEqualsSign(true)
        .removeTrailingWhitespace(true)
        .ensureHeadingBlankLines(true)
        .titleCase(false)
        .normalizeListBullets(false)
        .normalizeOrderedListMarkers(false)
        .ensureSourceDelimiters(false)
        .build();

AsciidocFormatter formatter = new AsciidocFormatter(config);

// Format a string
String formatted = formatter.format("== my title\n\ncontent");

// Format a file (charset is auto-detected and preserved)
Path path = Path.of("my.adoc");
byte[] formattedBytes = formatter.format(Files.readAllBytes(path));

// Format a stream
try (InputStream in = Files.newInputStream(path);
     OutputStream out = Files.newOutputStream(path)) {
    formatter.format(in, out);
}
```

### CLI

```
Usage: adocfmt [-chVw] [-cbl[=<true|false>]] [-ehlb[=<true|false>]]
               [-esd[=<true|false>]] [-nbd[=<true|false>]] [-nlb[=<true|false>]]
               [-nolm[=<true|false>]] [-nsh[=<true|false>]] [-ols[=<true|false>]]
               [-rthe[=<true|false>]] [-rtrw[=<true|false>]] [-tc[=<true|false>]]
               [<files>...]
```

|                   Option                    | Default |                                Description                                |
|---------------------------------------------|---------|---------------------------------------------------------------------------|
| `-w`, `--write`                             | --      | Write formatted content back to files                                     |
| `-c`, `--check`                             | --      | Check formatting without modifying files; exit 1 if any file would change |
| `-nsh`, `--normalize-setext-headings`       | `true`  | Convert setext headings to ATX                                            |
| `-cbl`, `--collapse-blank-lines`            | `true`  | Collapse consecutive blank lines                                          |
| `-ols`, `--one-sentence-per-line`           | `true`  | One sentence per line                                                     |
| `-nbd`, `--normalize-block-delimiters`      | `true`  | Normalise block delimiters to four characters                             |
| `-rthe`, `--remove-trailing-header-equals`  | `true`  | Remove trailing `=` from headings                                         |
| `-rtrw`, `--remove-trailing-whitespace`     | `true`  | Remove trailing whitespace                                                |
| `-ehlb`, `--ensure-heading-blank-lines`     | `true`  | Ensure blank lines around headings                                        |
| `-tc`, `--title-case`                       | `false` | Apply title case to headings                                              |
| `-nlb`, `--normalize-list-bullets`          | `false` | Normalise list bullets to `*`                                             |
| `-nolm`, `--normalize-ordered-list-markers` | `false` | Replace explicit numbers with `.`                                         |
| `-esd`, `--ensure-source-delimiters`        | `false` | Add missing `----` delimiters around source blocks                        |

#### Exit Codes

| Code |                             Meaning                              |
|------|------------------------------------------------------------------|
| `0`  | Success -- all files processed without error                     |
| `1`  | Unformatted files detected (only when `--check` is active)       |
| `2`  | Processing error (file not found, not readable, I/O error, etc.) |

#### Examples

```bash
# Format a single file in-place
adocfmt -w my.adoc

# Format multiple files in-place
adocfmt -w docs/getting-started.adoc docs/reference.adoc

# Format all .adoc files in a directory tree
find . -name "*.adoc" | xargs adocfmt -w

# Preview formatted output without modifying the file (single file → stdout)
adocfmt my.adoc

# Format stdin to stdout
cat my.adoc | adocfmt

# Check formatting in CI -- exits 1 if any file would change
adocfmt --check docs/*.adoc

# Check all .adoc files in a directory tree in CI
find . -name "*.adoc" | xargs adocfmt --check

# Check only the staged .adoc files (useful in a pre-commit hook)
git diff --cached --name-only --diff-filter=ACM | grep '\.adoc$' | xargs adocfmt --check

# Disable a transformation that is on by default
adocfmt -w --one-sentence-per-line=false my.adoc

# Disable multiple default transformations
adocfmt -w -ols=false -cbl=false my.adoc

# Enable optional transformations
adocfmt -w --title-case --normalize-list-bullets my.adoc

# Enable optional transformations using short aliases
adocfmt -w -tc -nlb -nolm -esd my.adoc

# Enable all optional transformations and disable one default
adocfmt -w -tc -nlb -nolm -esd -ols=false my.adoc
```

## Development

- **Layout:** `adocfmt` (core library) | `adocfmt-cli` (shaded JAR)
- **Build:** `mvn verify`
- **Versioning:** Semantic. Any change to formatting output is at least a minor update as it affects `--check` builds.

## Project Info

- **License:** [Apache License, Version 2.0](LICENSE)
- **Author:** Daniel Heid
- **Contributing:** [CONTRIBUTING.md](CONTRIBUTING.md)
- **Changelog:** [CHANGELOG.md](CHANGELOG.md)
- **Code of Conduct:** [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)

