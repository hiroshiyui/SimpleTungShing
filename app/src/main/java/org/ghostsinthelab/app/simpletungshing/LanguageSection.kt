package org.ghostsinthelab.app.simpletungshing

import android.app.LocaleManager
import android.os.LocaleList
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/** Language tags offered in the app; an empty tag follows the system language. */
private val LANGUAGES = listOf("" to R.string.language_system, "en-US" to R.string.language_english, "zh-TW" to R.string.language_zh_tw)

/**
 * Per-app language through the platform [LocaleManager] (Android 13+). Changing it
 * recreates the activity and sends LOCALE_CHANGED, on which the widget receiver redraws.
 */
@RequiresApi(33)
@Composable
fun LanguageSection() {
    val context = LocalContext.current
    val localeManager = remember {
        try {
            context.getSystemService(LocaleManager::class.java)
        } catch (_: Throwable) {
            null
        }
    }
    val current = localeManager?.applicationLocales?.toLanguageTags() ?: ""
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                LANGUAGES.forEachIndexed { index, (tag, label) ->
                    SegmentedButton(
                        selected = tag == current,
                        onClick = { localeManager?.applicationLocales = LocaleList.forLanguageTags(tag) },
                        shape = SegmentedButtonDefaults.itemShape(index, LANGUAGES.size),
                    ) { Text(stringResource(label)) }
                }
            }
        }
    }
}
