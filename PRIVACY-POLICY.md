# Privacy Policy 隱私權政策

**Simple Tung Shing 簡易農民曆** (`org.ghostsinthelab.app.simpletungshing`)

Effective date 生效日期：2026-10-18

## Summary 摘要

Simple Tung Shing does not collect, store on any server, share or sell any personal data. It works fully offline from calendar data bundled with the app, has no internet permission, and contains no analytics, advertising or tracking code.

簡易農民曆不收集、不上傳、不分享也不販售任何個人資料。本程式完全離線運作，使用隨程式內建的日曆資料，沒有網路權限，也不含任何分析、廣告或追蹤程式碼。

## Data collection 資料收集

The app collects no personal or device information: no name, account, contacts, location, photos, identifiers, usage statistics or crash reports. Nothing is transmitted off your device by the app.

本程式不收集任何個人或裝置資訊，包括姓名、帳號、聯絡人、位置、相片、識別碼、使用統計或當機報告。本程式不會將任何資料傳送到您的裝置之外。

## Network access 網路存取

The app does not request the `INTERNET` permission and cannot connect to the internet. All almanac data comes from the Central Weather Administration (交通部中央氣象署) open dataset 「日曆資料-國農曆對照」 bundled inside the app, used in accordance with the [CWA Open Data Platform data usage rules](https://opendata.cwa.gov.tw/about/rules). Links shown in the app (to the data source, its usage rules and Wikipedia) open in your browser only when you tap them.

本程式沒有申請 `INTERNET` 權限，無法連線網際網路。所有農民曆資料皆來自隨程式內建的交通部中央氣象署開放資料「日曆資料-國農曆對照」，依[中央氣象署開放資料平臺資料使用規範](https://opendata.cwa.gov.tw/about/rules)使用。程式中的連結（資料來源、使用規範與維基百科）只有在您點選時，才會由您的瀏覽器開啟。

## Data stored on your device 儲存在您裝置上的資料

The app stores only your widget appearance settings (background style, opacity, margin, padding, text alignment and moon phase options) in its private storage on your device. If you choose a language in the app, Android itself stores that choice. This information never leaves your device through the app.

If Android's device backup is turned on, Android may include these settings in your device backup, as it does for other apps. That backup is handled by Android and your backup provider (for example, your Google account) under their own privacy terms; the developer has no access to it.

本程式只在您裝置上的私有儲存空間保存小工具外觀設定（背景樣式、透明度、外距、內距、文字對齊與月相選項）。若您在程式中選擇語言，該設定由 Android 系統保存。這些資訊不會經由本程式離開您的裝置。

若您開啟了 Android 裝置備份，Android 可能會如同其他程式一樣，將這些設定納入裝置備份。該備份由 Android 與您的備份服務提供者（例如您的 Google 帳戶）依其隱私條款處理，開發者無法存取。

## Permissions 權限

The app asks for no runtime permissions. The following install-time permissions are added by the Android libraries it uses (WorkManager, through Jetpack Glance) to keep the home-screen widget up to date:

本程式不要求任何執行階段權限。以下安裝時權限由本程式使用的 Android 函式庫（透過 Jetpack Glance 使用的 WorkManager）加入，用於讓桌面小工具保持更新：

| Permission 權限 | Purpose 用途 |
|---|---|
| `WAKE_LOCK` | Lets the widget finish redrawing without the device sleeping midway. 讓小工具重繪時裝置不會中途休眠。 |
| `RECEIVE_BOOT_COMPLETED` | Restores scheduled widget updates after the device restarts. 裝置重新開機後恢復排定的小工具更新。 |
| `ACCESS_NETWORK_STATE` | Declared by WorkManager for network conditions; this app has no network access and does not use it. 由 WorkManager 宣告以支援網路條件；本程式沒有網路存取，也不使用此權限。 |
| `FOREGROUND_SERVICE` | Declared by WorkManager for long-running work; this app does not use it. 由 WorkManager 宣告以支援長時間工作；本程式不使用此權限。 |
| `…DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | Private to this app; keeps other apps from triggering its internal broadcasts. 僅限本程式使用，防止其他程式觸發其內部廣播。 |

## Third parties 第三方

The app includes no third-party services or SDKs that collect data. It uses open-source AndroidX and Kotlin libraries that run entirely on your device; see [NOTICES.md](NOTICES.md).

本程式不含任何會收集資料的第三方服務或 SDK。本程式使用的 AndroidX 與 Kotlin 開放原始碼函式庫完全在您的裝置上執行，詳見 [NOTICES.md](NOTICES.md)。

## Children 兒童

The app collects no data from anyone, including children, and is suitable for all ages.

本程式不收集任何人的資料（包括兒童），適合所有年齡層使用。

## Deleting your data 刪除資料

To remove everything the app has stored, clear its storage in Android Settings or uninstall the app.

若要刪除本程式儲存的所有資料，請在 Android 設定中清除本程式的儲存空間，或解除安裝本程式。

## Changes 政策變更

If this policy changes, the updated version will be published in this file in the project repository, with a new effective date.

若本政策有所變更，更新後的版本將發布於專案儲存庫中的本檔案，並標示新的生效日期。

## Contact 聯絡方式

For questions about this policy, please open an issue at <https://github.com/hiroshiyui/SimpleTungShing/issues>.

如對本政策有任何疑問，請您至 <https://github.com/hiroshiyui/SimpleTungShing/issues> 提出問題。
