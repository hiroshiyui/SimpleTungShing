package org.ghostsinthelab.app.simpletungshing.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ghostsinthelab.app.simpletungshing.data.MoonMethod

enum class BackgroundStyle { SOLID, GRADIENT, GLASS }

/** Appearance shared by every placed widget; edited in the app. */
data class WidgetSettings(
    val style: BackgroundStyle = BackgroundStyle.SOLID,
    /** Background opacity, 0 (fully transparent) to 1. Text is unaffected. */
    val opacity: Float = 1f,
    val showMoon: Boolean = true,
    val moonMethod: MoonMethod = MoonMethod.LUNAR_DAY,
) {
    companion object {
        private val Context.store by preferencesDataStore("widget_settings")

        private val STYLE = stringPreferencesKey("style")
        private val OPACITY = floatPreferencesKey("opacity")
        private val SHOW_MOON = booleanPreferencesKey("show_moon")
        private val MOON_METHOD = stringPreferencesKey("moon_method")

        fun flow(context: Context): Flow<WidgetSettings> =
            context.applicationContext.store.data.map { it.toSettings() }

        /** Saves [settings] and re-renders all widgets with them. */
        suspend fun save(context: Context, settings: WidgetSettings) {
            context.applicationContext.store.edit {
                it[STYLE] = settings.style.name
                it[OPACITY] = settings.opacity
                it[SHOW_MOON] = settings.showMoon
                it[MOON_METHOD] = settings.moonMethod.name
            }
            TungShingWidget.refreshAll(context)
        }

        private fun Preferences.toSettings(): WidgetSettings {
            val defaults = WidgetSettings()
            return WidgetSettings(
                style = enumOrNull<BackgroundStyle>(this[STYLE]) ?: defaults.style,
                opacity = this[OPACITY]?.coerceIn(0f, 1f) ?: defaults.opacity,
                showMoon = this[SHOW_MOON] ?: defaults.showMoon,
                moonMethod = enumOrNull<MoonMethod>(this[MOON_METHOD]) ?: defaults.moonMethod,
            )
        }

        private inline fun <reified E : Enum<E>> enumOrNull(name: String?): E? =
            enumValues<E>().firstOrNull { it.name == name }
    }
}
