package org.ghostsinthelab.app.simpletungshing

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ghostsinthelab.app.simpletungshing.data.SacrificeGroup

/** Preferences of the app screen itself (not the widget); kept apart from WidgetSettings so saving them does not re-render widgets. */
data class AppSettings(
    /** Groups of offering days marked with a dot in the month grid; empty means no marks. */
    val sacrificeGroups: Set<SacrificeGroup> = emptySet(),
) {
    companion object {
        private val Context.store by preferencesDataStore("app_settings")
        private val SACRIFICE_GROUPS = stringSetPreferencesKey("sacrifice_groups")

        /** Current settings and every later change; unknown stored group names are ignored. */
        fun flow(context: Context): Flow<AppSettings> = context.applicationContext.store.data.map { prefs ->
            val names = prefs[SACRIFICE_GROUPS].orEmpty()
            AppSettings(sacrificeGroups = SacrificeGroup.entries.filterTo(mutableSetOf()) { it.name in names })
        }

        suspend fun save(context: Context, settings: AppSettings) {
            context.applicationContext.store.edit {
                it[SACRIFICE_GROUPS] = settings.sacrificeGroups.mapTo(mutableSetOf()) { g -> g.name }
            }
        }
    }
}
