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
    /** Groups of offering days marked with a dot in the month grid; empty means no marks. */
    val worshipGroups: Set<WorshipGroup> = emptySet(),
) {
    companion object {
        private val Context.store by preferencesDataStore("app_settings")
        // The stored name predates the rename to "worship"; keep it so saved choices survive.
        private val WORSHIP_GROUPS = stringSetPreferencesKey("sacrifice_groups")

        /** Current settings and every later change; unknown stored group names are ignored. */
        fun flow(context: Context): Flow<AppSettings> = context.applicationContext.store.data.map { prefs ->
            val names = prefs[WORSHIP_GROUPS].orEmpty()
            AppSettings(worshipGroups = WorshipGroup.entries.filterTo(mutableSetOf()) { it.name in names })
        }

        suspend fun save(context: Context, settings: AppSettings) {
            context.applicationContext.store.edit {
                it[WORSHIP_GROUPS] = settings.worshipGroups.mapTo(mutableSetOf()) { g -> g.name }
            }
        }
    }
}
