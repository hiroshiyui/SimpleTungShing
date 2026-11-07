package org.ghostsinthelab.app.simpletungshing

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ghostsinthelab.app.simpletungshing.data.WorshipGroup

/** Preferences of the app screen itself (not the widget); kept apart from WidgetSettings so saving them does not re-render widgets. */
data class AppSettings(
    /** Groups of offering days marked with a dot in the month grid; all by default, empty means no marks. */
    val worshipGroups: Set<WorshipGroup> = WorshipGroup.entries.toSet(),
) {
    companion object {
        private val Context.store by preferencesDataStore("app_settings")
        private val WORSHIP_GROUPS = stringSetPreferencesKey("worship_groups")

        /**
         * Current settings and every later change. An unset key gives the defaults; a stored set,
         * even an empty one, is the user's choice. Unknown stored group names are ignored.
         */
        fun flow(context: Context): Flow<AppSettings> = context.applicationContext.store.data.map { prefs ->
            val names = prefs[WORSHIP_GROUPS]
            if (names == null) AppSettings()
            else AppSettings(worshipGroups = WorshipGroup.entries.filterTo(mutableSetOf()) { it.name in names })
        }

        suspend fun save(context: Context, settings: AppSettings) {
            context.applicationContext.store.edit {
                it[WORSHIP_GROUPS] = settings.worshipGroups.mapTo(mutableSetOf()) { g -> g.name }
            }
        }
    }
}
