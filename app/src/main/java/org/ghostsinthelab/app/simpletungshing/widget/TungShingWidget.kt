package org.ghostsinthelab.app.simpletungshing.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.glance.appwidget.lazy.LazyColumn
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
import org.ghostsinthelab.app.simpletungshing.data.MoonPhase
import org.ghostsinthelab.app.simpletungshing.data.SolarTerms
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * The home-screen widget: today's solar and lunar date, festivals and 節氣, the moon,
 * and (when the size allows) 干支 and 沖煞, scaled and trimmed to the widget's size by
 * [WidgetFit]. Rendered with Jetpack Glance from the bundled [CalendarData] and the
 * user's [WidgetSettings].
 *
 * The date is read inside composition and tied to the [REFRESHED_AT] state key, so
 * [refreshAll] must bump that key for a running session to pick up a new day; see the
 * comments in [provideGlance] and [refreshAll] for why neither plain `update()` nor
 * `GlanceAppWidgetManager.getGlanceIds` is enough.
 */
class TungShingWidget : GlanceAppWidget() {

    // Exact rather than responsive buckets: the user's margin and padding eat into the
    // real size, from which WidgetFit scales the text and picks the lines.
    override val sizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val calendar = withContext(Dispatchers.IO) { CalendarData.get(context) }
        val settingsFlow = WidgetSettings.flow(context)
        val initialSettings = settingsFlow.first()
        provideContent {
            // Reading the refresh stamp makes a running session recompose on refresh(),
            // so the date below is re-read after midnight.
            val state = currentState<Preferences>()
            state[REFRESHED_AT]
            val previewDate = state[PREVIEW_DATE]?.let(LocalDate::ofEpochDay)
            val settings by settingsFlow.collectAsState(initialSettings)
            GlanceTheme {
                Content(previewDate ?: LocalDate.now(), calendar, settings, preview = previewDate != null)
            }
        }
    }

    /**
     * Applies the outer margin and fits the lines into the widget's real size with
     * [WidgetFit]: font sizes shrink, then lines drop, then the lines scroll.
     */
    @Composable
    private fun Content(today: LocalDate, calendar: CalendarData, settings: WidgetSettings, preview: Boolean) {
        val context = LocalContext.current
        val locale = context.resources.configuration.locales[0]
        val lunar = calendar.lookup(today)
        val inset = (settings.marginDp + settings.paddingDp) * 2f
        val size = LocalSize.current
        val moon = if (settings.showMoon) MoonPhase.of(settings.moonMethod, today, lunar) else null
        val header = WidgetLine(
            today.format(DateTimeFormatter.ofPattern(context.getString(R.string.widget_header_pattern), locale)),
            WidgetFit.HEADER_SP,
        )
        val lines = lunar?.let {
            val festivals = Festivals.on(today, calendar)
            // Skip the term when a festival already carries its name (清明 → 清明節, 冬至).
            val term = SolarTerms.termOn(today)?.takeIf { t -> festivals.none { it.startsWith(t) } }
            val observances = listOfNotNull(term) + festivals
            Lines(
                header = header,
                lunar = WidgetLine(context.getString(R.string.widget_lunar_date, Almanac.lunarDateName(it)), WidgetFit.LUNAR_SP, bold = true),
                observance = observances.takeIf { o -> o.isNotEmpty() }
                    ?.let { o -> WidgetLine(o.joinToString("、"), WidgetFit.OBSERVANCE_SP, bold = true) },
                ganZhi = listOf(
                    WidgetLine("${it.yearGanZhi}年（${Almanac.zodiacOf(it.yearGanZhi)}）${Almanac.dayGanZhi(today)}日", WidgetFit.GANZHI_SP),
                    WidgetLine("${Almanac.chong(today)} ${Almanac.sha(today)}", WidgetFit.GANZHI_SP),
                ),
            )
        } ?: Lines(header, WidgetLine(context.getString(R.string.widget_out_of_range), WidgetFit.HEADER_SP), null, emptyList())
        val layout = WidgetFit.fit(
            widthDp = size.width.value - inset,
            heightDp = size.height.value - inset,
            fontScale = context.resources.configuration.fontScale,
            header = lines.header,
            lunar = lines.lunar,
            observance = lines.observance,
            ganZhi = lines.ganZhi,
            showMoon = moon != null,
        )
        Box(modifier = GlanceModifier.fillMaxSize().padding(settings.marginDp.dp)) {
            // RemoteViews refuses a list adapter outside a real widget host ("setRemoteAdapter
            // can only be used for AppWidgets"), so the preview shows the scroll layout as a
            // plain, clipped column instead of nothing.
            Body(today, calendar, settings, moon, lines, if (preview) layout.copy(scroll = false) else layout)
        }
    }

    /** The texts of one day, before the fit decides which of them show. */
    private class Lines(
        val header: WidgetLine,
        val lunar: WidgetLine,
        val observance: WidgetLine?,
        val ganZhi: List<WidgetLine>,
    )

    /**
     * The background and the text lines. Three states: data run out (notice only),
     * date outside the dataset (solar date and a notice, carried in [lines] with no 干支),
     * or the almanac lines as [layout] allows. When [WidgetLayout.scroll] is set the lines
     * go into a [LazyColumn] the user can scroll, since nothing smaller stays readable.
     */
    @Composable
    private fun Body(
        today: LocalDate,
        calendar: CalendarData,
        settings: WidgetSettings,
        moon: MoonPhase?,
        lines: Lines,
        layout: WidgetLayout,
    ) {
        val context = LocalContext.current
        val horizontal = when (settings.alignment) {
            ContentAlignment.START -> Alignment.Start
            ContentAlignment.CENTER -> Alignment.CenterHorizontally
            ContentAlignment.END -> Alignment.End
        }
        // A wrapped text box spans the full width, so the text inside needs the same
        // alignment as the box, or its lines sit at the start.
        val textAlign = when (settings.alignment) {
            ContentAlignment.START -> TextAlign.Start
            ContentAlignment.CENTER -> TextAlign.Center
            ContentAlignment.END -> TextAlign.End
        }
        val background = GlanceModifier
            .fillMaxSize()
            .background(
                ImageProvider(backgroundOf(settings.style)),
                // Only the glass style is translucent; solid and gradient are always opaque.
                alpha = if (settings.style == BackgroundStyle.GLASS) settings.opacity else 1f,
                contentScale = ContentScale.FillBounds,
            )
            .cornerRadius(16.dp)
            .padding(settings.paddingDp.dp)
        if (calendar.hasRunOut(today)) {
            Column(
                modifier = background.clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = horizontal,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = context.getString(R.string.widget_data_run_out),
                    maxLines = 4,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onSurface,
                        textAlign = textAlign,
                    ),
                )
            }
            return
        }
        val scale = layout.scale
        // In the scrolling list height is unlimited, so lines wrap instead of ellipsizing:
        // a wide line (large font, narrow widget) is read in full by scrolling, not cut.
        val wrap = if (layout.scroll) Int.MAX_VALUE else 1
        val items: List<@Composable () -> Unit> = buildList {
            add { Line(lines.header, scale, GlanceTheme.colors.onSurfaceVariant, textAlign, wrap) }
            add {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = today.dayOfMonth.toString(),
                        style = TextStyle(
                            fontSize = (layout.numberSp * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.primary,
                        ),
                    )
                    if (moon != null) {
                        Spacer(GlanceModifier.width((8 * scale).dp))
                        Moon(moon, (WidgetFit.MOON_DP * scale).toInt())
                    }
                }
            }
            add { Line(lines.lunar, scale, GlanceTheme.colors.onSurface, textAlign, wrap) }
            if (layout.showObservance && lines.observance != null) {
                add { Line(lines.observance, scale, GlanceTheme.colors.primary, textAlign, maxOf(wrap, if (layout.observanceWraps) 2 else 1)) }
            }
            if (layout.showGanZhi) {
                lines.ganZhi.forEach { line -> add { Line(line, scale, GlanceTheme.colors.onSurfaceVariant, textAlign, wrap) } }
            }
        }
        if (layout.scroll) {
            // Each item carries the click, since the list itself does not take one.
            LazyColumn(modifier = background, horizontalAlignment = horizontal) {
                items(items.size) { index ->
                    Box(modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>())) { items[index]() }
                }
            }
        } else {
            Column(
                modifier = background.clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = horizontal,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { it() }
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
    private fun Line(line: WidgetLine, scale: Float, color: ColorProvider, textAlign: TextAlign, maxLines: Int = 1) {
        Text(
            text = line.text,
            maxLines = maxLines,
            style = TextStyle(
                fontSize = (line.sizeSp * scale).sp,
                fontWeight = if (line.bold) FontWeight.Medium else FontWeight.Normal,
                color = color,
                textAlign = textAlign,
            ),
        )
    }

    companion object {
        private val REFRESHED_AT = longPreferencesKey("refreshed_at")

        /**
         * Epoch day to render instead of today. Only the in-app preview sets it (in the state
         * it passes to `compose`), so the widget can be checked on a festival day at any time
         * of year; placed widgets never carry it.
         */
        val PREVIEW_DATE = longPreferencesKey("preview_date")

        /** Re-renders every placed widget so it picks up the current date. */
        suspend fun refreshAll(context: Context) {
            FontScaleWorker.noteRendered(context)
            val widget = TungShingWidget()
            val glance = GlanceAppWidgetManager(context)
            // Ask the platform rather than GlanceAppWidgetManager.getGlanceIds, which looks the
            // widget up by its class name as stored at the last render: R8 renames the class
            // between builds, so after an upgrade that lookup finds nothing until the launcher
            // sends its own update, leaving the widget on its loading layout.
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, TungShingWidgetReceiver::class.java))
            // Every refresh path (app start, midnight, package replaced) also keeps the
            // font-scale check in step with whether any widget exists.
            if (ids.isEmpty()) FontScaleWorker.cancel(context) else FontScaleWorker.schedule(context)
            ids.map(glance::getGlanceIdBy)
                .forEach { id ->
                    updateAppWidgetState(context, id) { it[REFRESHED_AT] = System.currentTimeMillis() }
                    widget.update(context, id)
                }
        }
    }
}
