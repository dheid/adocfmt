# AGENTS.md

Instructions for any AI coding agent working in this repository.
This file governs agent behavior. It does not override explicit, in-session instructions from
the human operator, but absent such instructions, these rules are mandatory.

---

## 1. Mission & Core Directives (Spec-Driven Development)

This repository follows **Spec-Driven Development (SDD)**. adocfmt formats other people's
source-controlled documents and gates CI pipelines (`--check` mode); an unreviewed behavioral
change silently breaks every downstream `--check` build. Specs exist to prevent that.

- **Primary Rule:** WRITING CODE WITHOUT AN APPROVED SPECIFICATION IS STRICTLY FORBIDDEN.
  This applies to new transformations, CLI flags, config options, and any change to formatting
  output — not to pure refactors, typo fixes, or dependency bumps with no behavioral change.
- **Workflow Sequence:** Every non-trivial task MUST follow:
  `Draft/Update Spec` -> `User Review & Approval` -> `Write Tests (TDD)` -> `Implement` -> `Verify`.
- **No Speculation:** Do not guess at ambiguous requirements (e.g., "should this rule apply
  inside tables?", "does this option interact with `oneSentencePerLine`?"). Ask a clarifying
  question instead of assuming.

## 2. Strict Constraints & Guardrails

- **Git & Commits:** DO NOT commit, stage, or push under any circumstances. `git add`,
  `git commit`, `git push`, and `git tag` are reserved for the human developer. If asked to
  "commit this," perform the work and stop — do not run the commit yourself unless explicitly
  instructed per single action in that message.
- **Scope Limits:** Modify only files directly relevant to the current spec. Do not opportunistically
  refactor unrelated code, reformat unrelated files, or "clean up" code outside the task's scope.
- **Breaking Changes:** Per `README.md` ("Versioning"), **any change to formatting output is a
  breaking change** for downstream `--check` consumers, even if it looks like a bug fix. Any
  change to public API (`core` package classes, `AsciidocFormatterConfig` fields, CLI flags,
  exit codes) requires explicit prior user approval and must be called out as breaking.
- **Zero Runtime Dependencies:** `core` intentionally ships with a minimal dependency footprint
  (`icu4j` for text/locale handling, `jspecify` for nullability annotations, both consumed at
  runtime; `lombok` is compile-time only). Do not add new runtime dependencies to `core` without
  explicit approval — this is a deliberate architectural constraint, not an oversight.
- **No New Root Files:** Do not create planning documents, scratch notes, or summary `.md` files
  in the repo root unless the user asks for them.

## 3. Specification Standards & Hierarchy

This repository has **no existing `/specs/` directory, ADRs, or OpenAPI/Protobuf definitions** —
it's a plain Java library/CLI, not a networked service. The Single Source of Truth for behavior
is, in order of authority:

1. **`README.md` → "Code Style" section** — the canonical description of every transformation,
   with Before/After AsciiDoc examples and its default on/off state. Any new or changed
   transformation MUST be documented here in the same format.
2. **`README.md` → "Invariants guaranteed by every run"** and **`CONTRIBUTING.md` → "Correctness
   Invariants"** — the non-negotiable contract every transformation must satisfy:
   - **Idempotent:** `format(format(x)) == format(x)`.
   - **Safe / Protected Regions:** content inside delimited blocks (`----`, `....`, `++++`,
     `////`), comments, and directives is never modified.
   - **Semantic Equivalence:** input and output render to identical AsciidoctorJ HTML.
   - **Trailing Newline:** output always ends with exactly one newline.
3. **`AsciidocFormatterConfig` (core)** — the executable data contract for which transformations
   exist and their defaults. Treat its builder fields as the "schema."
4. **`core/src/test/resources/fixtures/**Before.adoc` / `**After.adoc` pairs** — the closest thing
   this project has to acceptance-test specs. Each transformation/option area has its own
   subdirectory (e.g. `fixtures/table/`, `fixtures/options/`).

**For any new feature, draft a `spec_draft.md`** (scratch, not committed) containing:
- **User Story:** who wants this and why.
- **Given/When/Then acceptance criteria**, expressed in terms of Before/After AsciiDoc snippets
  (matching the README's existing style) — this project's natural "Given/When/Then" is literal
  input/output text.
- **Data Contract:** exact new/changed field(s) on `AsciidocFormatterConfig` and/or new CLI
  flag(s) (long + short alias, default value), matching the existing table conventions in
  `README.md`.
- **Invariant impact:** explicitly confirm the change preserves idempotence, protected regions,
  and semantic equivalence, or explain why an exception is justified.

Pause after drafting and wait for human review before writing any implementation code.

## 4. Execution Workflow for Agents

1. **Spec Verification:** Check `README.md`'s Code Style section and existing fixtures for
   coverage of the requested behavior. If no spec exists, draft `spec_draft.md` as described
   above and pause for human review — do not proceed to implementation.
2. **Impact Analysis:** Identify affected classes. Transformations live in
   `core/src/main/java/org/drjekyll/adocfmt/internal/` (grouped into `block/`, `line/`,
   `setext/`, and top-level per-transformer classes), are wired together in
   `AsciidocFormatter`, configured via `AsciidocFormatterConfig`, and exposed as CLI flags in
   `cli/.../AsciidocFormatterCli.java`. Check whether a change to one transformer interacts with
   others (e.g., blank-line handling around tables and around headings).
3. **Test-First Implementation:** Add fixture pairs under
   `core/src/test/resources/fixtures/<area>/<name>Before.adoc` /
   `<name>After.adoc`, and/or a focused unit test in the matching
   `core/src/test/java/.../internal/**/*Test.java` class, before writing implementation code.
   Confirm the new test fails first.
4. **Minimal Implementation:** Implement the smallest change that satisfies the spec's
   acceptance criteria in the appropriate `internal` transformer class. Follow existing patterns
   (small, single-responsibility transformer classes composed in `AsciidocFormatter`). No
   speculative generalization.
5. **Quality Verification:** Run `mvn -B verify` from the repo root (see §5) and confirm all
   tests pass, coverage thresholds hold, and Spotless formatting is clean before reporting the
   task done.

## 5. Repository-Specific Rules

**Stack:** Java 17+ (compiled at `--release 17`; README states runtime targets Java 17+, dev
guidance recommends Java 21), multi-module Maven (`adocfmt-parent` → `core`, `cli`), Lombok
(compile-time codegen), JSpecify (nullability annotations), ICU4J (locale-aware text
processing in `core`), Picocli (CLI parsing in `cli`), AsciidoctorJ (test-only, for semantic
HTML-equivalence assertions), JUnit 5 + AssertJ (testing).

**Commands:**
- Build everything and run the full test suite: `mvn clean install` (or `mvn -B verify`, as CI
  does — this is the command that must pass before any task is considered done).
- Run a single module's tests: `mvn -pl core test` / `mvn -pl cli test`.
- Run the built CLI: `java -jar cli/target/adocfmt.jar --help`.
- Formatting (Java via Google Java Format, `pom.xml` via sortPom, Markdown via flexmark) is
  auto-applied by the Spotless plugin during `process-sources` — running `mvn verify` both
  formats and builds. There is no separate "check" formatting step to invoke manually; just run
  the build.
- Coverage: JaCoCo instruments `core` during the build; CI enforces **80% minimum coverage**
  overall and on changed files (`.github/workflows/ci.yml`). New code must not drop coverage
  below this threshold.

**Architectural patterns:**
- Each transformation is its own small, single-responsibility class under
  `core/.../internal/` (e.g. `TrailingWhitespaceRemover`, `TitleCaseTransformer`,
  `BlankLinesCollapser`), composed together in `AsciidocFormatter`. Follow this pattern for new
  transformations rather than adding branches to an existing class.
- Configuration is immutable and built via the Lombok `@Builder` pattern on
  `AsciidocFormatterConfig`; new options are added as new builder fields with an explicit
  default, mirrored as a new CLI flag (long name + short alias) in `AsciidocFormatterCli`.
- Tests favor **fixture-file pairs** (Before/After `.adoc` files) exercised through
  `AsciidocFormatterTestSupport`, plus AsciidoctorJ-based semantic-equivalence checks, over
  large inline string literals.

**Prohibited patterns:**
- No new runtime dependencies added to `core` without explicit user approval (zero-dependency
  philosophy).
- No transformation that mutates content inside `----`, `....`, `++++`, `////`, or comment/
  directive regions.
- No non-idempotent transformation (`format(format(x))` must equal `format(x)`).
- No breaking change to `AsciidocFormatterConfig`, CLI flags, or default formatting output
  without explicit prior approval and a documented rationale (this is a SemVer-significant
  change per `README.md`).
- No hardcoded secrets, credentials, or machine-specific paths.
- No unhandled checked exceptions swallowed silently — follow the existing pattern of narrow,
  well-named exception types (e.g. `UnsupportedLineEndingException`).

## 6. Definition of Done (DoD)

A task is considered complete ONLY when:

- [ ] A specification (`spec_draft.md`, or an update to `README.md`'s Code Style section) was
      drafted and approved by the user before implementation began, for any behavior-affecting
      change.
- [ ] Implementation covers 100% of the spec's acceptance criteria, expressed as passing
      Before/After fixtures and/or unit tests.
- [ ] `mvn -B verify` passes locally with zero test failures, zero Spotless violations, and
      coverage at or above 80% overall and on changed files.
- [ ] Idempotence, protected-region safety, semantic HTML equivalence, and trailing-newline
      invariants are preserved (verified by tests, not assumed).
- [ ] `README.md` (Code Style / CLI options tables) and `CHANGELOG.md` are updated if the change
      is user-visible.
- [ ] No git commits, stages, or pushes were executed by the agent.
- [ ] No temporary files, debug output, commented-out code, or scratch spec drafts remain in
      the codebase (scratch `spec_draft.md` files are working documents, not deliverables —
      remove them once the corresponding spec has been folded into `README.md`/fixtures).
