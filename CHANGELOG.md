# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2026-06-04

### Added

- Directory arguments are now supported: passing a directory recurses the full tree and processes all `.adoc` and `.asciidoc` files (case-insensitive). Symbolic link directories are not followed. Discovered files are processed in sorted order for reproducible output.
- Explicitly passed file paths bypass extension filtering and are always processed regardless of their extension.

## [0.1.2] - 2026-06-04

### Fixed

- Preserve AsciiDoc hard line breaks: lines ending with ` +` are no longer merged by the one-sentence-per-line transformation.
- Title case transformation now capitalizes the first word after a sentence-ending punctuation mark (`.`, `!`, `?`, `;`) within a heading or block title.
- List bullet normalization no longer converts `-` bullets when the list contains a mix of `-` and `*` bullets.
- Support backticks for code blocks
- Formatter does no longer add blank lines within the document header

## [0.1.1] - 2026-06-04

### Fixed

- Ensure formatted output always ends with exactly one newline.

## [0.1.0] - 2026-06-03

### Added

- Initial release of `adocfmt` core and CLI.
- Opinionated formatting rules for AsciiDoc.
- Idempotence and semantic equivalence testing.
- GitHub Actions CI/CD workflows.

