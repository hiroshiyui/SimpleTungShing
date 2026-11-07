package org.ghostsinthelab.app.simpletungshing

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Preferences of the app screen itself (not the widget); kept apart from WidgetSettings so saving them does not re-render widgets. */
data class AppSettings(
    /** Decorate 初一、十五、初二、十六、端午、中秋、重陽 in the month grid with a strap. */
    val markSacrificeDays: Boolean = false,
) {
    companion object {
        private val Context.store by preferencesDataStore("app_settings")
        private val MARK_SACRIFICE_DAYS = booleanPreferencesKey("mark_sacrifice_days")

        fun flow(context: Context): Flow<AppSettings> = context.applicationContext.store.data.map {
            AppSettings(markSacrificeDays = it[MARK_SACRIFICE_DAYS] ?: false)
        }

        suspend fun save(context: Context, settings: AppSettings) {
            context.applicationContext.store.edit { it[MARK_SACRIFICE_DAYS] = settings.markSacrificeDays }
        }
    }
}
