---
name: a11y-audit
description: Audit and improve accessibility (a11y) of the app and the home-screen widget — TalkBack semantics, touch targets, contrast, font scaling, and keyboard focus. Use when the user asks to audit, review, fix or test accessibility, screen-reader support, contrast, or large-font behaviour.
argument-hint: file path, screen or component name, or "full" for the whole app
---

# Accessibility Audit

You are auditing and improving the accessibility of Simple Tung Shing: a Jetpack Compose app
(Material 3) and a Jetpack Glance widget. Findings must be concrete (file and line, what a
TalkBack user hears or a large-font user sees, and the fix), and fixes must keep both locales
(`values/strings.xml`, `values-zh-rTW/strings.xml`) in sync.

## Principles for this app

- **Almanac content is text first.** Lunar dates, 干支, 沖煞, festivals and 節氣 are plain
  `Text`; screen readers read them as written. Keep Han script content readable aloud: no
  decorative characters that TalkBack spells out, and no information carried only by colour,
  size or position.
- **The month grid is the hardest surface.** Each day cell must announce its full meaning
  (solar date, lunar day or 節氣, selected state, today) in one description through
  `Modifier.semantics`; only the month-stepping arrows in `MonthCard.kt` carry one so far.
  Check that the description is localised, that the selected day reports `selected`, that
  cells are real buttons (`Role.Button`, `onClick` label) with 48 dp touch targets, and that
  the cell height grows with the font scale (a fixed `height(52.dp)` clips the lunar label).
- **Images need a decision.** Moon bitmaps: the app shows the phase name as text next to the
  image, so the image can be `contentDescription = null`; the widget has no such text, so its
  `Image` keeps `phase.name`. Attribution icons are decorative (`null`).
- **Links must be reachable.** Attribution strings are HTML rendered with
  `AnnotatedString.fromHtml`; confirm the link annotations are focusable and announced as
  links, and that the link text names its target (not "here").
- **Font scaling is a layout feature, not a bug.** The widget fits its lines with
  `widget/WidgetFit` using the system font scale; the app must survive 2.0× without clipped
  or overlapping text. Never use `fontScale`-independent `dp` for text, and never give a
  text container a fixed `dp` height. The widget's scrolling fallback cannot be shown in the
  in-app preview (RemoteViews refuses list adapters outside a widget host), so the preview
  draws it as a clipped column; judge that state on the home screen.
- **Widget limits.** Glance has no `semantics` modifier; only `Image.contentDescription` and
  the text itself reach TalkBack. The widget is one clickable surface that opens the app;
  make sure every line is readable and the clip-free fit holds at large font scales (see the
  scroll fallback in `WidgetFit`).
- **Language and tone.** Follow the project conventions in every new string: 漢文 not 中文,
  台灣漢語 for the UI language, 您 never 你.

## Checklist

### Screen reader (TalkBack)

- Every interactive element has a name: buttons, tabs, sliders, switches, segmented choices.
  Material 3 components take `contentDescription` or a visible label; sliders need
  `Modifier.semantics { stateDescription }` or a visible value text that is part of the same
  semantics node (`mergeDescendants`).
- Decorative images use `contentDescription = null`; informative ones describe content, not
  appearance.
- Grouping: a card that is one logical unit should merge descendants so TalkBack reads it as
  one item, while lists of independent items (day cells) stay separate.
- State: selected tab, selected day, enabled/disabled stepping buttons, the picked moon
  method and background style are announced.
- Live changes: when the date changes by stepping, the day card updates; consider
  `liveRegion = LiveRegionMode.Polite` on the date headline so the change is announced.
- Reading order follows visual order; nothing important is unreachable by swipe navigation.
- Dialogs (date picker, language picker) move focus in and return it on dismiss.

### Touch and input

- Touch targets at least 48 × 48 dp (Material components default to it; custom `clickable`
  boxes such as the day cells and link rows do not).
- Keyboard and D-pad: focus is visible and the tab order is sensible (relevant for ChromeOS
  and external keyboards). `clickable` elements are focusable by default; confirm nothing
  uses `pointerInput` alone.
- No gesture-only actions; everything is reachable with a tap.

### Visual

- Contrast: text on the widget backgrounds (`widget_colors.xml`, `-night`, `-v31` dynamic
  colour, plus the user's opacity 0–100 %) must stay readable over any wallpaper; the
  `onSurface` colours are drawn over a translucent background, so test at low opacity on
  light and dark wallpapers. Target WCAG AA (4.5:1 body, 3:1 large text) where the app
  controls both colours; where the wallpaper shows through, recommend a safe minimum opacity
  or a text shadow rather than claiming a ratio.
- Do not convey meaning by colour alone (e.g. 節氣 days in the grid are also labelled by text).
- Font scale: run the app at 1.3× and 2.0× (`adb shell settings put system font_scale 2.0`,
  restore to `1.0` afterwards); check the day card, month grid, settings sliders and the
  widget preview for clipping or overlap.
- Display size: `adb shell wm density` changes should not break layouts.
- Reduce motion: the app has no animations beyond Material defaults; keep it that way.
- Dark theme: both themes are checked, including the widget `-night` colours.

### Content and language

- Strings are complete sentences where read aloud; abbreviations only when standard.
- `android:supportsRtl` is irrelevant (no RTL locale shipped) but layouts must not hardcode
  left/right where start/end exists.
- Per-app language: the picker (`LanguageSection.kt`, Android 13+) must be operable by
  TalkBack and announce the current language.

## How to run

When invoked without arguments, audit the files changed recently:
```bash
git diff --name-only HEAD~5
```
When given a file, screen or component, focus there. For `full`, go through:

1. `MainActivity.kt` (tabs, day card, stepping buttons, date picker, attribution links)
2. `MonthCard.kt` (day cells: semantics, role, touch target, selected/today state)
3. `WidgetSettingsSection.kt` and `WidgetPreview.kt` (sliders, choices, switch, preview)
4. `LanguageSection.kt`
5. `widget/TungShingWidget.kt`, `widget/WidgetFit.kt`, `res/values*/widget_colors.xml`
6. `res/values/strings.xml` and `res/values-zh-rTW/strings.xml`

### Device checks (when a device is attached)

```bash
adb shell settings put system font_scale 2.0      # then restore: 1.0
adb shell settings put secure enabled_accessibility_services com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService
adb shell settings put secure accessibility_enabled 1
adb shell settings put secure enabled_accessibility_services ''   # restore
adb shell settings put secure accessibility_enabled 0             # restore
adb exec-out uiautomator dump /dev/tty   # inspect content-desc, clickable, bounds (48dp targets)
```
Use `uiautomator dump` to confirm `content-desc` on each control and that bounds meet
48 dp at the device density. Always restore any system setting you changed.

### Automated checks

- Lint: `./gradlew :app:lintDebug` and read the `Accessibility` category
  (`ContentDescription`, `TouchTargetSizeCheck`, `ClickableViewAccessibility`,
  `TextContrastCheck`).
- Compose tests may assert semantics with `onNodeWithContentDescription`; if adding
  instrumented tests, put them under `app/src/androidTest/`.

## Output format

### Must fix
Barriers: controls with no name, unreachable content, clipped text at large font, contrast
that makes text unreadable.

### Should fix
Degraded experience: missing state announcements, small targets, verbose or unlocalised
descriptions.

### Nice to have
Polish: live regions, grouping, reading-order tweaks.

For each finding give **file:line**, **what the user experiences**, and **the fix** (with
code when short). When asked to fix rather than audit, apply the fixes, keep both string
files in sync, build with `./gradlew :app:assembleDebug`, and verify on a device if one is
attached.

## Task: $ARGUMENTS
