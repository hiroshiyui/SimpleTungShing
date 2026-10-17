---
name: code-review-and-security-audit
description: Review code for quality, correctness, and security vulnerabilities. Use when the user asks to review code, audit for security issues, or check for bugs and anti-patterns.
argument-hint: file path, component name, or scope of review
---

# Code Review and Security Audit

You are performing code review and security auditing for Simple Tung Shing.

## Scope

This skill covers two complementary concerns:

1. **Code Review** — correctness, readability, maintainability, and adherence to project conventions.
2. **Security Audit** — identifying vulnerabilities, unsafe patterns, and potential attack surfaces.

## Review Checklist

### Code Quality

- Null safety: proper use of Kotlin null-safe operators; avoid `!!` unless justified.
- Thread safety: correct use of coroutines, synchronized access to shared state (e.g. the `CalendarData` singleton).
- Main thread: asset I/O and CSV parsing (`CalendarData.get`) must stay off the main thread.
- Resource management: no leaked streams or readers.
- Error handling: appropriate use of try/catch; no silently swallowed exceptions.
- Consistency: follows existing patterns in the codebase (Jetpack Compose + Material 3 for the app UI, Jetpack Glance for the widget, `java.time` for dates).
- No dead code, unused imports, or redundant logic.

### Code Smells

- **Long methods**: methods doing too many things; should be broken into smaller, focused functions.
- **Large classes**: classes with too many responsibilities; consider splitting by concern.
- **Duplicated code**: repeated logic that should be extracted into a shared function (e.g. date formatting duplicated between `MainActivity.kt` and `TungShingWidget.kt` instead of living in `Almanac`).
- **Deep nesting**: excessive `if`/`when`/`try` nesting that harms readability; consider early returns or extraction.
- **Magic numbers/strings**: unexplained literal values that should be named constants.
- **Feature envy**: a method that uses another class's data more than its own.
- **Inappropriate intimacy**: classes that depend too heavily on each other's internals.
- **Primitive obsession**: overuse of primitives where a domain type (enum, data class) would be clearer.
- **Long parameter lists**: functions with many parameters; consider grouping into a data class or builder.
- **Mutable shared state**: prefer immutable data and local state; flag unnecessary `var` or global mutable collections.

### Code Refactoring Suggestions

- **Extract method**: identify blocks of code within a method that perform a distinct task and can be extracted.
- **Extract class/interface**: when a class handles multiple responsibilities, suggest splitting into focused classes or introducing an interface.
- **Replace conditional with polymorphism**: complex `when`/`if-else` chains that switch on type can often be replaced with polymorphic dispatch.
- **Introduce sealed class/enum**: when a set of related constants or states is represented loosely (strings, ints), suggest modeling with a sealed class or enum.
- **Use Kotlin idioms**: replace Java-style patterns with idiomatic Kotlin — e.g., `let`/`apply`/`also` scope functions, destructuring, extension functions, `buildList`/`buildString`.
- **Simplify lifecycle management**: leverage `lifecycleScope`, `repeatOnLifecycle`, or `LifecycleObserver` to reduce manual lifecycle bookkeeping.
- **Reduce coupling**: identify tight coupling between components and suggest interface abstractions or moving logic into the `data` layer.
- **Improve testability**: flag code that is hard to unit test (e.g., direct `LocalDate.now()` calls deep in logic, hidden `Context` dependencies) and suggest restructuring, such as passing the date or a `Clock` in.

### Calendar and Almanac Correctness

This app's main risk is showing a wrong date or a wrong almanac item. Check:

- **Dataset parsing** (`data/CalendarData.kt`): UTF-8 BOM in the header, trailing blank/`,,,,,` rows, CRLF line endings, leap months written as `閏N`, and the contiguity assumption behind index-based lookup.
- **Range handling**: dates before `firstDate` or after `lastDate` must show the out-of-range message, never crash or show a neighbouring day.
- **Derived rules** (`data/Almanac.kt`): 日干支 anchor (1949-10-01 = 甲子), `floorMod` for dates before the anchor, 沖 (branch +6, stem +4), 煞 directions (申子辰南、寅午戌北、巳酉丑東、亥卯未西), month names (正月…十二月, 閏), day names (初一…廿九, 三十), big/small month (大/小) unknown at dataset edges.
- **Time zones and midnight**: "today" is the device's local date; check behaviour across time zone changes, DST, and the date picker's UTC-millis conversion.
- Prefer verifying against `app/src/test/` and adding a test with a known reference date over reasoning alone.

### Widget and Background Work

- **Refresh after midnight** (`widget/TungShingWidgetReceiver.kt`): the inexact `RTC` alarm is rescheduled from `onUpdate`, `onReceive` and app start, and cancelled in `onDisabled`. Look for paths that leave no alarm scheduled (reboot, app update, force-stop).
- **Glance sessions** (`widget/TungShingWidget.kt`): a running session only recomposes when state it reads changes — the `REFRESHED_AT` stamp is what forces the date to be re-read. Keep `LocalDate.now()` inside composition after that read.
- **`goAsync()`**: `finish()` must always be called, and work must fit in the broadcast time limit.
- Battery: no wakeup alarms, exact alarms, or frequent `updatePeriodMillis` without a clear need.

### Android-Specific Security

- **Permissions**: the app should need no runtime permissions; flag any newly added permission and question whether it is required (e.g. `SCHEDULE_EXACT_ALARM`, `INTERNET`).
- **Export controls**: verify that components (activities, services, receivers) are not inadvertently exported. The widget receiver must be exported for `APPWIDGET_UPDATE`; check that its custom midnight action is only triggered through the app's own explicit, immutable `PendingIntent`, and that an external sender spoofing it can do no more than cause a harmless refresh.
- **PendingIntent**: use `FLAG_IMMUTABLE` and explicit intents.
- **Intent handling**: check for intent injection or unvalidated intent extras.
- **Links**: attribution links should be fixed `https://` URLs, not built from data.
- **Backup**: check `backup_rules.xml` / `data_extraction_rules.xml` if any user data is ever stored.
- **R8**: ensure keep rules (`app/src/main/keepRules/`) don't strip classes referenced from the manifest or Glance.

### General Security

- No hardcoded secrets, API keys, or credentials.
- No command injection via `Runtime.exec()` or `ProcessBuilder`.
- No path traversal vulnerabilities in file operations.
- No insecure random number generation for security-sensitive operations.
- Dependencies: check for known vulnerabilities in third-party libraries.

### Data Licensing

- The CWA attribution (https://opendata.cwa.gov.tw/about/rules) must remain visible in the app.
- Derived items must not be presented as CWA data.

## Output Format

Report findings using this structure:

### Critical / High

Issues that must be fixed — security vulnerabilities, crashes, wrong dates or almanac data shown to users.

### Medium

Issues that should be fixed — logic bugs, thread safety concerns, code smell.

### Low / Informational

Suggestions for improvement — style, readability, minor optimizations.

For each finding, include:
- **File and line number** (e.g., `CalendarData.kt:42`)
- **Description** of the issue
- **Impact** — what could go wrong
- **Recommendation** — how to fix it, with code if appropriate

## How to Run

When invoked without arguments, review recently changed files:
```bash
git diff --name-only HEAD~5
```
(Use a smaller range if the history is shorter.)

When invoked with a specific scope (file, directory, or component name), focus the review on that area.

For a full audit, systematically review:
1. Data layer (`data/CalendarData.kt`, `data/Almanac.kt`, `app/src/main/assets/`)
2. Widget (`widget/TungShingWidget.kt`, `widget/TungShingWidgetReceiver.kt`, `res/xml/tung_shing_widget_info.xml`)
3. UI layer (`MainActivity.kt`, `ui/theme/`)
4. Configuration (`AndroidManifest.xml`, keep rules, backup rules)
5. Tests (`app/src/test/`)
6. Dependencies (`gradle/libs.versions.toml`)

## Task: $ARGUMENTS
