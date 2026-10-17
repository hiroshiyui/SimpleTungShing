---
name: docs-engineering
description: Writing/updating project documentation (README, PRIVACY-POLICY, NOTICES, changelogs) and maintaining F-Droid metadata. Use when the user asks to update docs, write changelogs, or modify F-Droid store listings.
argument-hint: task description
---

# Document Engineering

You are performing documentation tasks for Simple Tung Shing.

## Bilingual Convention

This project maintains documentation in both **English (en-US)** and **Traditional Chinese (zh-TW)**. When creating or updating user-facing text, always produce both language versions unless instructed otherwise.

- README.md uses interleaved bilingual sections (English heading/paragraph followed by Chinese equivalent).
- F-Droid metadata keeps separate locale directories.
- In-app strings live in `app/src/main/res/values/strings.xml` and are currently Traditional Chinese only, since the almanac content itself is Chinese.

## Project Documentation Files

| File | Purpose |
|------|---------|
| `README.md` | Project overview, usage guide, build instructions (bilingual) |
| `PRIVACY-POLICY.md` | Privacy policy |
| `NOTICES.md` | Third-party license and data attribution notices |
| `CLAUDE.md` | Guidance for Claude Code |

These files may not exist yet. When asked to create one, follow the conventions here; do not create files the user didn't ask for.

When updating `NOTICES.md`, check `gradle/libs.versions.toml` and `app/build.gradle.kts` for current dependencies and their licenses.

## Data Attribution

The app bundles 交通部中央氣象署 (Central Weather Administration, CWA) open data: 「日曆資料-國農曆對照」 (A-A0087-001), https://opendata.cwa.gov.tw/dataset/astronomy/A-A0087-001, at `app/src/main/assets/A-A0087-001.csv`.

- Any document or store listing that describes the data source must attribute it to CWA and link the usage rules at https://opendata.cwa.gov.tw/about/rules. `NOTICES.md` must include this attribution.
- Keep the in-app attribution (the `attribution_*` strings and links in `MainActivity.kt`) consistent with the docs.
- Distinguish data from CWA (solar–lunar date conversion, ROC year, sexagenary year name) from items the app derives itself by traditional rules (日干支, 生肖, 沖, 煞; see `data/Almanac.kt`). Never imply that CWA provides or endorses the derived items.
- The privacy story is simple and should stay accurate: the app works offline from bundled data, requests no permissions for network or personal data, and collects nothing. Re-check `AndroidManifest.xml` before stating this.

## F-Droid Metadata

Metadata lives under `fastlane/metadata/android/` with two locales (create it on first use):

```
fastlane/metadata/android/
  en-US/
    title.txt              # App name (max 50 chars)
    short_description.txt  # Store tagline (max 80 chars)
    full_description.txt   # Full store listing (max 4000 chars)
    changelogs/<versionCode>.txt  # Per-version changelog
    images/                # Screenshots and graphics
  zh-TW/
    (same structure)
```

### Changelogs

- Changelog filenames use **versionCode** (integer), not versionName. Find the current versionCode in `app/build.gradle.kts` (`versionCode = ...`).
- Write changelogs in a friendly, first-person tone consistent with existing entries (see `fastlane/metadata/android/en-US/changelogs/` for examples, once there are some).
- Always create both `en-US` and `zh-TW` versions.
- Max 500 characters per changelog file.

### Store Descriptions

- `full_description.txt` is written in first person from the app's perspective ("Hello, it's me, Simple Tung Shing.").
- Use `*` for bullet points in en-US, `＊` (fullwidth asterisk) in zh-TW.
- Include the CWA data attribution (see Data Attribution).

## Writing Style

- Keep language clear and approachable.
- For zh-TW text, use Traditional Chinese characters only. Do not use Simplified Chinese.
- Preserve the existing tone: the app "speaks" in first person in store listings and changelogs; README and other docs use standard third-person technical writing.
- Use established almanac terms: 農民曆 / 黃曆 / 通書 (Tung Shing), 國曆 (Gregorian calendar), 農曆 (lunar calendar), 民國 (ROC year), 干支 (sexagenary cycle), 生肖 (zodiac animal), 沖煞 (clash and sha direction), 閏月 (leap month).
- Describe traditional almanac content neutrally as folk custom and tradition, not as fact or prediction.
- **Respect external resource providers' efforts.** When describing problems in upstream projects, dependencies, data providers (e.g., CWA), or platforms, use softened wording. Prefer "issue", "behavior", or "limitation" over "bug"; prefer "resolved" or "addressed" over "fixed the bug". Frame workarounds as collaborative rather than corrective. This applies to both en-US and zh-TW text (e.g., use 「問題」 rather than 「錯誤」 or 「bug」 when referring to upstream).

## Task: $ARGUMENTS
