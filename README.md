# Simple Tung Shing 簡易農民曆

A simple Tung Shing (農民曆／黃曆／通書) home-screen widget and companion app for Android.

一個簡單的 Android 農民曆（黃曆／通書）桌面小工具與隨附程式。

## Features 功能

- **Home-screen widget** showing today's Gregorian date and weekday, lunar date (including leap months), the year's sexagenary name and zodiac animal, the day's sexagenary stem-branch, and its clash and sha direction. The widget updates itself shortly after local midnight and when the system time or time zone changes. On smaller sizes, only the date lines are shown.
- **Customizable look**: choose a solid, gradient or glass-style background (following your wallpaper colors on Android 12+), adjust its transparency, margin, padding and text alignment, and show or hide the moon phase. Widgets cannot blur what is behind them, so the glass style is a translucent gradient with a light edge.
- **Solar terms** (節氣), such as 立春 and 霜降, shown on their day.
- **Festivals and observances** (節日) from the Han traditional and religious calendars, such as 春節, 清明節, 端午節, 中秋節 and 天后誕, shown on the widget and in the app.
- **Moon phase**, derived either from the lunar day (初一 as new moon) or by astronomical calculation; you pick the method.
- **Companion app** for browsing any date in the dataset range, by stepping day by day or picking a date from a calendar. It also shows the ROC year and whether the lunar month is long or short.
- **English and Taiwanese Mandarin (台灣漢語)** interfaces, following the system language by default; on Android 13+ you can also pick the language in the app. Almanac content stays in Han script (漢文).
- **Offline and private**: all data is bundled with the app. It requests no permissions and collects nothing.

- **桌面小工具**：顯示今日國曆日期、星期、農曆日期（含閏月）、歲次干支與生肖、日干支，以及沖煞。小工具會在當地午夜過後、系統時間或時區變更時自動更新；尺寸較小時僅顯示日期資訊。
- **自訂外觀**：可選擇純色、漸層或玻璃質感背景（Android 12 以上依桌布配色），調整背景透明度、外距、內距與文字對齊，並可顯示或隱藏月相。桌面小工具無法模糊背後的畫面，玻璃質感以半透明漸層與亮邊呈現。
- **節氣**：在節氣當天顯示名稱，例如立春、霜降。
- **節日**：在小工具與程式中顯示漢族傳統節日與宗教節日，例如春節、清明節、端午節、中秋節、天后誕等。
- **月相**：可選擇依農曆日期（以初一為新月）推估，或以天文公式計算。
- **隨附程式**：可逐日切換或從日曆選擇日期，瀏覽資料範圍內任一天的農民曆資訊，並另外顯示民國年與農曆大小月。
- **介面支援英文與台灣漢語**：預設跟隨系統語言，Android 13 以上也可在程式中自行選擇。農民曆內容維持漢文。
- **離線、保護隱私**：所有資料皆隨程式內建，不需要任何權限，也不收集任何資料。

## Data 資料

Solar–lunar date conversion comes from the Central Weather Administration (CWA) open dataset 「日曆資料-國農曆對照」 ([A-A0087-001](https://opendata.cwa.gov.tw/dataset/astronomy/A-A0087-001)), bundled unmodified. It covers **2022-01-01 to 2050-12-31**; dates outside this range show an out-of-range message.

The day's stem-branch (日干支), zodiac animal (生肖), clash (沖) and sha direction (煞) are **derived by the app** according to traditional rules, and the moon phase (月相) from the lunar day or an astronomical formula. Festival names and dates follow the Wikipedia article [漢族傳統節日](https://zh.wikipedia.org/zh-tw/漢族傳統節日), using the customary Taiwan date where it lists several; solar terms (節氣), and the festivals tied to them (清明, 冬至, 三伏), are computed by the app. They are not provided or endorsed by CWA. These items are presented as folk custom and tradition, not as fact or prediction.

The dataset does not include daily auspicious/inauspicious activities (宜忌), so the app does not show them.

國曆與農曆對照資料來自交通部中央氣象署開放資料「日曆資料-國農曆對照」（[A-A0087-001](https://opendata.cwa.gov.tw/dataset/astronomy/A-A0087-001)），原樣內建於程式中，涵蓋 **2022-01-01 至 2050-12-31**；超出範圍的日期會顯示提示訊息。

日干支、生肖、沖、煞由**本程式依傳統規則推算**，月相則依農曆日期或天文公式推算，皆非中央氣象署提供或認可。節日名稱與日期參考維基百科[「漢族傳統節日」](https://zh.wikipedia.org/zh-tw/漢族傳統節日)條目，列有多個日期者採臺灣習俗；節氣及與節氣相關的節日（清明、冬至、三伏）由本程式計算。這些內容屬於民俗傳統，僅供參考，並非事實陳述或預測。

此資料集未包含宜忌，因此本程式不顯示此項目。

## Building 建置

Requirements: Android SDK (compile SDK 37). The Gradle daemon runs on JDK 25, which the foojay toolchain resolver downloads automatically if needed. The app supports Android 8.0 (API 26) and later.

需求：Android SDK（compile SDK 37）。Gradle daemon 使用 JDK 25，必要時 foojay toolchain resolver 會自動下載。程式支援 Android 8.0（API 26）以上版本。

```bash
./gradlew :app:assembleDebug        # Debug APK → app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest    # JVM unit tests 單元測試
./gradlew :app:lintDebug            # Lint
```

## Attribution 資料來源標示

This app uses open data from the Central Weather Administration, Ministry of Transportation and Communications: 「日曆資料-國農曆對照」 (<https://opendata.cwa.gov.tw/dataset/astronomy/A-A0087-001>), used in accordance with the [CWA Open Data Platform data usage rules](https://opendata.cwa.gov.tw/about/rules). See [NOTICES.md](NOTICES.md) for all third-party notices.

本程式使用交通部中央氣象署開放資料「日曆資料-國農曆對照」（<https://opendata.cwa.gov.tw/dataset/astronomy/A-A0087-001>），依[中央氣象署開放資料平臺資料使用規範](https://opendata.cwa.gov.tw/about/rules)使用。完整第三方聲明請見 [NOTICES.md](NOTICES.md)。
