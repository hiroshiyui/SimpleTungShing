package org.ghostsinthelab.app.simpletungshing.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
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
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.ghostsinthelab.app.simpletungshing.MainActivity
import org.ghostsinthelab.app.simpletungshing.R
import org.ghostsinthelab.app.simpletungshing.data.Almanac
import org.ghostsinthelab.app.simpletungshing.data.CalendarData
import org.ghostsinthelab.app.simpletungshing.data.Festivals
import org.ghostsinthelab.app.simpletungshing.data.LunarDay
import org.ghostsinthelab.app.simpletungshing.data.MoonPhase
import org.ghostsinthelab.app.simpletungshing.data.SolarTerms
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TungShingWidget : GlanceAppWidget() {

    // Exact rather than responsive buckets: the user's margin and padding eat into the
    // real size, which decides whether the full layout fits.
    override val sizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val calendar = withContext(Dispatchers.IO) { CalendarData.get(context) }
        val settingsFlow = WidgetSettings.flow(context)
        val initialSettings = settingsFlow.first()
        provideContent {
            // Reading the refresh stamp makes a running session recompose on refresh(),
            // so the date below is re-read after midnight.
            currentState<Preferences>()[REFRESHED_AT]
            val settings by settingsFlow.collectAsState(initialSettings)
            GlanceTheme {
                Content(LocalDate.now(), calendar, settings)
            }
        }
    }

    @Composable
    private fun Content(today: LocalDate, calendar: CalendarData, settings: WidgetSettings) {
        val lunar = calendar.lookup(today)
        val inset = ((settings.marginDp + settings.paddingDp) * 2).dp
        val size = LocalSize.current
        val full = size.width - inset >= FULL_CONTENT.width && size.height - inset >= FULL_CONTENT.height
        val moon = if (settings.showMoon) MoonPhase.of(settings.moonMethod, today, lunar) else null
        Box(modifier = GlanceModifier.fillMaxSize().padding(settings.marginDp.dp)) {
            Body(today, lunar, calendar, settings, moon, full)
        }
    }

    @Composable
    private fun Body(
        today: LocalDate,
        lunar: LunarDay?,
        calendar: CalendarData,
        settings: WidgetSettings,
        moon: MoonPhase?,
        full: Boolean,
    ) {
        val context = LocalContext.current
        val locale = context.resources.configuration.locales[0]
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(
                    ImageProvider(backgroundOf(settings.style)),
                    alpha = settings.opacity,
                    contentScale = ContentScale.FillBounds,
                )
                .cornerRadius(16.dp)
                .padding(settings.paddingDp.dp)
                .clickable(actionStartActivity<MainActivity>()),
            horizontalAlignment = when (settings.alignment) {
                ContentAlignment.START -> Alignment.Start
                ContentAlignment.CENTER -> Alignment.CenterHorizontally
                ContentAlignment.END -> Alignment.End
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (calendar.hasRunOut(today)) {
                Text(
                    text = context.getString(R.string.widget_data_run_out),
                    maxLines = 4,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onSurface,
                        textAlign = when (settings.alignment) {
                            ContentAlignment.START -> TextAlign.Start
                            ContentAlignment.CENTER -> TextAlign.Center
                            ContentAlignment.END -> TextAlign.End
                        },
                    ),
                )
                return@Column
            }
            Line(
                today.format(DateTimeFormatter.ofPattern(context.getString(R.string.widget_header_pattern), locale)),
                13.sp, GlanceTheme.colors.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = today.dayOfMonth.toString(),
                    style = TextStyle(
                        fontSize = if (full) 52.sp else 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary,
                    ),
                )
                if (moon != null) {
                    Spacer(GlanceModifier.width(8.dp))
                    Moon(moon, if (full) 32 else 24)
                }
            }
            if (lunar == null) {
                Line(context.getString(R.string.widget_out_of_range), 13.sp, GlanceTheme.colors.onSurface)
                return@Column
            }
            Line(context.getString(R.string.widget_lunar_date, Almanac.lunarDateName(lunar)), 16.sp, GlanceTheme.colors.onSurface, bold = true)
            val festivals = Festivals.on(today, calendar)
            // Skip the term when a festival already carries its name (清明 → 清明節, 冬至).
            val term = SolarTerms.termOn(today)?.takeIf { t -> festivals.none { it.startsWith(t) } }
            val observances = listOfNotNull(term) + festivals
            if (observances.isNotEmpty()) {
                Line(observances.joinToString("、"), 13.sp, GlanceTheme.colors.primary, bold = true)
            }
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
    private fun Moon(phase: MoonPhase, sizeDp: Int) {
        val density = LocalContext.current.resources.displayMetrics.density
        Image(
            provider = ImageProvider(moonBitmap(phase, (sizeDp * density).toInt())),
            contentDescription = phase.name,
            modifier = GlanceModifier.size(sizeDp.dp),
        )
    }

    private fun backgroundOf(style: BackgroundStyle): Int = when (style) {
        BackgroundStyle.SOLID -> R.drawable.widget_bg_solid
        BackgroundStyle.GRADIENT -> R.drawable.widget_bg_gradient
        BackgroundStyle.GLASS -> R.drawable.widget_bg_glass
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
        /** Room the full layout needs inside margin and padding (a 160dp widget at the default 12dp padding). */
        private val FULL_CONTENT = DpSize(136.dp, 136.dp)
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
