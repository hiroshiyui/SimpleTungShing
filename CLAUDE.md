# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Simple Tung Shing (農民曆／黃曆／通書) is an Android home-screen widget plus a small Compose app. It is offline-only: all date data comes from the bundled CWA open data CSV, and it needs no permissions. Package: `org.ghostsinthelab.app.simpletungshing`; minSdk 26, compile/target SDK 37, AGP 9 (built-in Kotlin, no separate kotlin-android plugin), Gradle daemon on JDK 25 via the foojay toolchain.

## Commands

```bash
./gradlew :app:assembleDebug          # build debug APK -> app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest      # JVM unit tests
./gradlew :app:testDebugUnitTest --tests '*.AlmanacTest'                    # one class
./gradlew :app:testDebugUnitTest --tests '*.CalendarDataTest.leapMonthAndLength'  # one test
./gradlew :app:lintDebug              # lint -> app/build/reports/lint-results-debug.html
./gradlew :app:connectedAndroidTest   # instrumented tests (only the template test exists)
```

Unit tests read the real dataset via the relative path `src/main/assets/A-A0087-001.csv`, so they must run with the `app/` module as the working directory (Gradle does this).

## Architecture

**Data source.** `app/src/main/assets/A-A0087-001.csv` is the unmodified CWA dataset 「日曆資料-國農曆對照」 (columns 日期,民國年,農曆年,農曆月,農曆日,星期; UTF-8 BOM, CRLF, a trailing `,,,,,` row; leap months written `閏N`). It covers 2022-01-01 to 2050-12-31. Keep the file as published rather than preprocessing it.

**`data/CalendarData`** parses the CSV once into a process-wide singleton (`CalendarData.get(context)`, blocking I/O — always call it on `Dispatchers.IO`). Rows must be contiguous, so `lookup(date)` is an index offset from `firstDate`; it returns `null` outside the range, and every UI must handle that with an out-of-range message. Lunar month length (大/小) is derived from runs of rows and is `null` for the months cut off at either end of the dataset.

**`data/Almanac`** holds items the app *derives* by traditional rules, not from CWA: 日干支 (anchor 1949-10-01 = 甲子), 生肖, 沖 (branch +6, stem +4, e.g. 甲子日 → 沖馬(戊午)), 煞 direction, and Chinese month/day/weekday names. Formatting for both the app and the widget goes through here.

**`data/MoonPhase`** is also app-derived, by a user-selectable `MoonMethod`: `LUNAR_DAY` (初一 = new moon, mean synodic month; unavailable outside the dataset) or `ASTRONOMICAL` (Meeus low-precision phase angle at local noon). `widget/MoonBitmap` draws it for both the widget and the app.

**`data/SolarTerms`** computes the date (Taiwan time, matching CWA) the sun reaches a longitude (Meeus ch. 25, ~15 min accuracy); `termOn(date)` names the 節氣 on a day (cached per year). The app shows it as its own row; the widget prepends it to the festival line unless a festival already carries the name (清明節, 冬至). **`data/Festivals`** lists the folk and religious tables of zh.wikipedia 「漢族傳統節日」 (CC BY-SA 4.0, credited in-app): lunar dates in non-leap months, with Taiwan-custom dates where the article gives several; 寒食/清明/冬至/三伏 come from `SolarTerms` and 庚 days.

**Widget** (`widget/`) uses Jetpack Glance:
- `TungShingWidget` loads `CalendarData` in `provideGlance` but reads `LocalDate.now()` *inside* composition, after reading the `REFRESHED_AT` Glance state key. A running Glance session only recomposes when state it reads changes, so `refreshAll()` bumps that key and then calls `update()` — refreshing without bumping it can leave a stale date on screen.
- Appearance comes from `WidgetSettings` (a Preferences DataStore edited in the app's settings card, shared by all widgets). The widget collects its flow inside `provideContent`, and `WidgetSettings.save()` also calls `refreshAll()` for widgets without a running session. Backgrounds are shape drawables (`widget_bg_*`) drawn with Glance's `background(ImageProvider, alpha)` for transparency; colors live in `widget_colors.xml` with `-night` and `-v31` (wallpaper dynamic color) variants. Layout settings (margin outside the background, padding inside it, horizontal alignment) are applied in `Content`/`Body`. Widgets cannot blur what is behind them, so the glass style is only a translucent gradient with a sheen and edge.
- `SizeMode.Exact`: the 干支/沖煞 lines show only when `FULL_CONTENT` (136×136dp) fits inside the user's margin and padding; otherwise only the date lines show.
- `TungShingWidgetReceiver` schedules an inexact, non-wakeup `AlarmManager.setWindow(RTC, …)` just after local midnight (no exact-alarm permission). It reschedules from `onUpdate`, from app start (`MainActivity.onCreate`), and on its custom midnight action plus `TIME_SET`, `TIMEZONE_CHANGED` and `MY_PACKAGE_REPLACED` (all declared in the manifest); it cancels in `onDisabled`.

**App** (`MainActivity.kt`): one Compose screen for browsing dates within the dataset range (day stepping plus a Material 3 date picker, which works in UTC-midnight millis — convert with `ZoneOffset.UTC`), the widget settings card (`WidgetSettingsSection.kt`), and the data attribution.

## Conventions

- **Attribution is required.** Any place describing the data must credit 交通部中央氣象署 「日曆資料-國農曆對照」 (https://opendata.cwa.gov.tw/dataset/astronomy/A-A0087-001) and link https://opendata.cwa.gov.tw/about/rules. The app also states that 日干支/沖煞/月相/節氣/節日 are app-derived, and credits the Wikipedia festival list; never present derived items as CWA data.
- UI strings are Traditional Chinese (default `values/strings.xml`); use Traditional characters only.
- The CSV has no 節氣 or 宜忌. Solar terms are computed in `SolarTerms`; 宜忌 needs a new data source, so discuss with the user first rather than inventing rules.
- Commits: plain imperative subject, `--signoff`; default branch `main`. Use the project skills in `.claude/skills/` (`commit-and-push`, `release-engineering`, `docs-engineering`, `code-review-and-security-audit`) for those tasks.
