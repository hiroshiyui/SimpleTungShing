package org.ghostsinthelab.app.simpletungshing.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ghostsinthelab.app.simpletungshing.MainActivity
import org.ghostsinthelab.app.simpletungshing.data.Almanac
import org.ghostsinthelab.app.simpletungshing.data.CalendarData
import java.time.LocalDate

class TungShingWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(COMPACT, FULL))

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val calendar = withContext(Dispatchers.IO) { CalendarData.get(context) }
        provideContent {
            // Reading the refresh stamp makes a running session recompose on refresh(),
            // so the date below is re-read after midnight.
            currentState<Preferences>()[REFRESHED_AT]
            GlanceTheme {
                Content(LocalDate.now(), calendar)
            }
        }
    }

    @Composable
    private fun Content(today: LocalDate, calendar: CalendarData) {
        val lunar = calendar.lookup(today)
        val full = LocalSize.current.height >= FULL.height
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(16.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Line(
                "${today.year}年${today.monthValue}月 ${Almanac.weekdayName(today.dayOfWeek)}",
                13.sp, GlanceTheme.colors.onSurfaceVariant,
            )
            Text(
                text = today.dayOfMonth.toString(),
                style = TextStyle(
                    fontSize = if (full) 52.sp else 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary,
                ),
            )
            if (lunar == null) {
                Line("超出資料範圍", 13.sp, GlanceTheme.colors.onSurface)
                return@Column
            }
            Line("農曆${Almanac.lunarDateName(lunar)}", 16.sp, GlanceTheme.colors.onSurface, bold = true)
            if (full) {
                Line(
                    "${lunar.yearGanZhi}年（${Almanac.zodiacOf(lunar.yearGanZhi)}）${Almanac.dayGanZhi(today)}日",
                    13.sp, GlanceTheme.colors.onSurfaceVariant,
                )
                Line(
                    "${Almanac.chong(today)} ${Almanac.sha(today)}",
                    13.sp, GlanceTheme.colors.onSurfaceVariant,
                )
            }
        }
    }

    @Composable
    private fun Line(text: String, size: TextUnit, color: ColorProvider, bold: Boolean = false) {
        Text(
            text = text,
            maxLines = 1,
            style = TextStyle(
                fontSize = size,
                fontWeight = if (bold) FontWeight.Medium else FontWeight.Normal,
                color = color,
            ),
        )
    }

    companion object {
        private val COMPACT = DpSize(110.dp, 110.dp)
        private val FULL = DpSize(160.dp, 160.dp)
        private val REFRESHED_AT = longPreferencesKey("refreshed_at")

        /** Re-renders every placed widget so it picks up the current date. */
        suspend fun refreshAll(context: Context) {
            val widget = TungShingWidget()
            GlanceAppWidgetManager(context).getGlanceIds(TungShingWidget::class.java).forEach { id ->
                updateAppWidgetState(context, id) { it[REFRESHED_AT] = System.currentTimeMillis() }
                widget.update(context, id)
            }
        }
    }
}
