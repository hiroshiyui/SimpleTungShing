package org.ghostsinthelab.app.simpletungshing.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ghostsinthelab.app.simpletungshing.data.MoonMethod

enum class BackgroundStyle { SOLID, GRADIENT, GLASS }

enum class ContentAlignment { START, CENTER, END }

/** Appearance shared by every placed widget; edited in the app. */
data class WidgetSettings(
    val style: BackgroundStyle = BackgroundStyle.SOLID,
    /** Background opacity, 0 (fully transparent) to 1. Text is unaffected. */
    val opacity: Float = 1f,
    val showMoon: Boolean = true,
    val moonMethod: MoonMethod = MoonMethod.LUNAR_DAY,
    /** Space between the widget's bounds and its background, in dp. */
    val marginDp: Int = 0,
    /** Space between the background's edge and the content, in dp. */
    val paddingDp: Int = 12,
    val alignment: ContentAlignment = ContentAlignment.CENTER,
) {
    companion object {
        private val Context.store by preferencesDataStore("widget_settings")

        private val STYLE = stringPreferencesKey("style")
        private val OPACITY = floatPreferencesKey("opacity")
        private val SHOW_MOON = booleanPreferencesKey("show_moon")
        private val MOON_METHOD = stringPreferencesKey("moon_method")
        private val MARGIN = intPreferencesKey("margin_dp")
        private val PADDING = intPreferencesKey("padding_dp")
        private val ALIGNMENT = stringPreferencesKey("alignment")

        val MARGIN_RANGE = 0..24
        val PADDING_RANGE = 0..32

        fun flow(context: Context): Flow<WidgetSettings> =
            context.applicationContext.store.data.map { it.toSettings() }

        /** Saves [settings] and re-renders all widgets with them. */
        suspend fun save(context: Context, settings: WidgetSettings) {
            context.applicationContext.store.edit {
                it[STYLE] = settings.style.name
                it[OPACITY] = settings.opacity
                it[SHOW_MOON] = settings.showMoon
                it[MOON_METHOD] = settings.moonMethod.name
                it[MARGIN] = settings.marginDp
                it[PADDING] = settings.paddingDp
                it[ALIGNMENT] = settings.alignment.name
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
                marginDp = this[MARGIN]?.coerceIn(MARGIN_RANGE) ?: defaults.marginDp,
                paddingDp = this[PADDING]?.coerceIn(PADDING_RANGE) ?: defaults.paddingDp,
                alignment = enumOrNull<ContentAlignment>(this[ALIGNMENT]) ?: defaults.alignment,
            )
        }

        private inline fun <reified E : Enum<E>> enumOrNull(name: String?): E? =
            enumValues<E>().firstOrNull { it.name == name }
    }
}
