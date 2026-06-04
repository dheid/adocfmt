# Contributing to adocfmt

Thank you for your interest in contributing to `adocfmt`! We welcome all contributions, from bug reports and documentation improvements to new features and formatting rules.

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

## How Can I Contribute?

### Reporting Bugs

- Use the [Bug Report template](https://github.com/dheid/adocfmt/issues/new?template=bug_report.yml).
- Include as much detail as possible, especially the AsciiDoc input and the expected vs. actual output.

### Suggesting Enhancements

- Use the [Feature Request template](https://github.com/dheid/adocfmt/issues/new?template=feature_request.yml).
- Explain the "why" behind the suggestion and how it would improve the tool.

### Pull Requests

1. **Fork the repo** and create your branch from `main`.
2. **Add tests** for your changes. We maintain high standards for idempotence and semantic equivalence.
3. **Run the full suite**: `mvn verify` should pass.
4. **Follow the style**: Adhere to the existing coding style and project architecture (zero runtime dependencies for the core library).
5. **Small is better**: We prefer small, focused PRs.

## Development Environment

- **Java**: Current LTS (21) is recommended for development; the library targets Java 11 bytecode.
- **Maven**: Multi-module setup (`core`, `cli`).

## Correctness Invariants

Any formatting change must satisfy:
1.  **Idempotence**: `format(format(x)) == format(x)`.
2.  **Protected Regions**: Never modify content inside blocks like `----`, `....`, `++++`, `////`, or `[source]`.
3.  **Semantic Equivalence**: The normalized HTML output must remain identical.

Thank you for making `adocfmt` better!
