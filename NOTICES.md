# Third-Party Notices 第三方聲明

Simple Tung Shing includes or depends on the following third-party resources.

Simple Tung Shing 包含或使用下列第三方資源。

## Data 資料

### 日曆資料-國農曆對照 (A-A0087-001)

- **Provider 提供者**: 交通部中央氣象署 (Central Weather Administration, Ministry of Transportation and Communications)
- **Source 來源**: <https://opendata.cwa.gov.tw/dataset/astronomy/A-A0087-001>
- **Terms 使用規範**: [中央氣象署開放資料平臺資料使用規範 (CWA Open Data Platform data usage rules)](https://opendata.cwa.gov.tw/about/rules)
- **Bundled as 內建檔案**: `app/src/main/assets/A-A0087-001.csv` (unmodified 未經修改)

The dataset provides the Gregorian–lunar date conversion, ROC year, and the year's sexagenary name. The day's stem-branch (日干支), zodiac animal (生肖), clash (沖), sha direction (煞) and moon phase (月相) are derived by this app according to traditional rules or an astronomical formula; they are not provided or endorsed by the Central Weather Administration.

此資料集提供國曆與農曆日期對照、民國年及歲次干支。日干支、生肖、沖、煞、月相由本程式依傳統規則或天文公式推算，並非中央氣象署提供或認可。

## Libraries 函式庫

The following libraries are distributed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

下列函式庫以 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 授權釋出。

| Library 函式庫 | Artifact | Version 版本 |
|---|---|---|
| AndroidX Core KTX | `androidx.core:core-ktx` | 1.19.1 |
| AndroidX Activity Compose | `androidx.activity:activity-compose` | 1.13.0 |
| AndroidX Lifecycle Runtime KTX | `androidx.lifecycle:lifecycle-runtime-ktx` | 2.11.0 |
| Jetpack Compose UI | `androidx.compose.ui:ui`, `ui-graphics`, `ui-tooling-preview` | BOM 2026.02.01 |
| Jetpack Compose Material 3 | `androidx.compose.material3:material3` | BOM 2026.02.01 |
| Jetpack Glance App Widget | `androidx.glance:glance-appwidget` | 1.2.0 |
| Jetpack DataStore Preferences | `androidx.datastore:datastore-preferences` | 1.2.1 |
| Kotlin Standard Library | `org.jetbrains.kotlin:kotlin-stdlib` | 2.2.10 |

### Build and test tools 建置與測試工具

These are used only to build or test the app and are not included in the distributed APK.

下列工具僅用於建置或測試，不包含在發佈的 APK 中。

| Tool 工具 | License 授權 |
|---|---|
| Gradle, Android Gradle Plugin, Kotlin Compose compiler plugin, foojay toolchain resolver | Apache License 2.0 |
| AndroidX Test (JUnit ext, Espresso), Compose UI Test / Tooling | Apache License 2.0 |
| JUnit 4 | [Eclipse Public License 1.0](https://www.eclipse.org/legal/epl-v10.html) |
