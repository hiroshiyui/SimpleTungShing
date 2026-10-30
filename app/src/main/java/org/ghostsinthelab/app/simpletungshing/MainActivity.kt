package org.ghostsinthelab.app.simpletungshing

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ghostsinthelab.app.simpletungshing.data.Almanac
import org.ghostsinthelab.app.simpletungshing.data.CalendarData
import org.ghostsinthelab.app.simpletungshing.data.Auspicious
import org.ghostsinthelab.app.simpletungshing.data.Festivals
import org.ghostsinthelab.app.simpletungshing.data.MoonPhase
import org.ghostsinthelab.app.simpletungshing.data.SolarTerms
import org.ghostsinthelab.app.simpletungshing.ui.theme.SimpleTungShingTheme
import org.ghostsinthelab.app.simpletungshing.widget.TungShingWidget
import org.ghostsinthelab.app.simpletungshing.widget.TungShingWidgetReceiver
import org.ghostsinthelab.app.simpletungshing.widget.WidgetSettings
import org.ghostsinthelab.app.simpletungshing.widget.moonBitmap
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import kotlin.math.roundToInt


/**
 * The single activity. Loads [CalendarData] off the main thread, then shows either
 * [DataRunOutScreen] (dataset ended) or [AlmanacScreen]. Starting the app also
 * reschedules the widget's midnight alarm and redraws all widgets, so a date or
 * language change made while the app was closed reaches the home screen.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TungShingWidgetReceiver.scheduleMidnightRefresh(this)
        // Also catches a language chosen while the app was not running.
        if (savedInstanceState == null) lifecycleScope.launch { TungShingWidget.refreshAll(applicationContext) }
        enableEdgeToEdge()
        setContent {
            SimpleTungShingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val calendar by produceState<CalendarData?>(null) {
                        value = withContext(Dispatchers.IO) { CalendarData.get(applicationContext) }
                    }
                    val settings by remember { WidgetSettings.flow(applicationContext) }
                        .collectAsState(null)
                    val scope = rememberCoroutineScope()
                    val currentCalendar = calendar
                    val currentSettings = settings
                    if (currentCalendar != null && currentCalendar.hasRunOut(LocalDate.now())) {
                        DataRunOutScreen(currentCalendar, Modifier.padding(innerPadding))
                    } else if (currentCalendar != null && currentSettings != null) {
                        AlmanacScreen(
                            calendar = currentCalendar,
                            settings = currentSettings,
                            onSettingsChange = { scope.launch { WidgetSettings.save(applicationContext, it) } },
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }
}

/** Shown instead of everything else once the bundled CWA data has ended. */
@Composable
fun DataRunOutScreen(calendar: CalendarData, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(stringResource(R.string.data_run_out_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.data_run_out_message, calendar.lastDate),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(8.dp))
        Attribution()
    }
}

/**
 * The main screen: tabs for the almanac (month grid, day card, date picker and
 * attribution), the widget settings, and on Android 13+ the language picker.
 * The browsed date is kept as an epoch day so it survives configuration changes.
 */
@Composable
fun AlmanacScreen(
    calendar: CalendarData,
    settings: WidgetSettings,
    onSettingsChange: (WidgetSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var epochDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var picking by rememberSaveable { mutableStateOf(false) }
    // Settings holds only the language picker, which needs Android 13+.
    val tabs = listOfNotNull(
        R.string.tab_almanac,
        R.string.tab_widget,
        R.string.tab_settings.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU },
    )
    var tab by rememberSaveable { mutableIntStateOf(0) }
    // Hoisted so that each tab keeps its scroll position across switches.
    val almanacScroll = rememberScrollState()
    val widgetScroll = rememberScrollState()
    val settingsScroll = rememberScrollState()
    val date = LocalDate.ofEpochDay(epochDay)

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = tab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = tab == index, onClick = { tab = index }, text = { Text(stringResource(title)) })
            }
        }
        if (tab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(almanacScroll)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MonthCard(date, calendar, onSelect = { epochDay = it.toEpochDay() })
                DayCard(date, calendar, settings)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    OutlinedButton(
                        onClick = { epochDay-- },
                        enabled = date > calendar.firstDate,
                    ) { Text(stringResource(R.string.previous_day)) }
                    OutlinedButton(onClick = { epochDay = LocalDate.now().toEpochDay() }) {
                        Text(stringResource(R.string.today))
                    }
                    OutlinedButton(
                        onClick = { epochDay++ },
                        enabled = date < calendar.lastDate,
                    ) { Text(stringResource(R.string.next_day)) }
                }
                TextButton(
                    onClick = { picking = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) { Text(stringResource(R.string.pick_date)) }
                Spacer(Modifier.height(8.dp))
                Attribution()
            }
        } else if (tab == 1) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(widgetScroll)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                WidgetSettingsSection(settings, date, onSettingsChange)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(settingsScroll)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LanguageSection()
            }
        }
    }

    if (picking) {
        PickDateDialog(
            initial = date,
            calendar = calendar,
            onDismiss = { picking = false },
            onPicked = {
                epochDay = it.toEpochDay()
                picking = false
            },
        )
    }
}

/**
 * Everything the app shows for one day: the CWA lunar date, then the app-derived rows
 * (干支, 沖煞, 節氣, festivals, moon by the widget's moon-method
 * setting, and 吉凶／宜忌／吉時). Shows an out-of-range message when the date has no CWA row.
 */
@Composable
private fun DayCard(date: LocalDate, calendar: CalendarData, settings: WidgetSettings) {
    val lunar = calendar.lookup(date)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val rocYear = lunar?.rocYear ?: (date.year - 1911)
            val locale = LocalConfiguration.current.locales[0]
            Text(
                stringResource(
                    R.string.app_header,
                    rocYear,
                    date.format(DateTimeFormatter.ofPattern(stringResource(R.string.app_header_pattern), locale)),
                ),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                date.dayOfMonth.toString(),
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                date.dayOfWeek.getDisplayName(TextStyle.FULL, locale),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))
            if (lunar == null) {
                Text(
                    stringResource(R.string.out_of_range, calendar.firstDate, calendar.lastDate),
                    style = MaterialTheme.typography.bodyLarge,
                )
                return@Column
            }
            DetailRow(stringResource(R.string.label_lunar), Almanac.lunarDateName(lunar, withSize = true))
            SolarTerms.termOn(date)?.let {
                RowDivider()
                DetailRow(stringResource(R.string.label_solar_term), it)
            }
            Festivals.on(date, calendar).takeIf { it.isNotEmpty() }?.let {
                RowDivider()
                FestivalRow(it)
            }
            RowDivider()
            DetailRow(
                stringResource(R.string.label_year),
                "${lunar.yearGanZhi}年 · 生肖${Almanac.zodiacOf(lunar.yearGanZhi)}",
            )
            RowDivider()
            DetailRow(stringResource(R.string.label_day_ganzhi), "${Almanac.dayGanZhi(date)}日")
            RowDivider()
            DetailRow(stringResource(R.string.label_chong_sha), "${Almanac.chong(date)} ${Almanac.sha(date)}")
            MoonPhase.of(settings.moonMethod, date, lunar)?.let {
                RowDivider()
                MoonRow(it)
            }
            AuspiciousRows(remember(date) { Auspicious.of(date) })
        }
    }
}

/** 吉凶 (建除 and 黃道／黑道), 宜忌 and 吉時, all app-derived. */
@Composable
private fun AuspiciousRows(day: Auspicious.Day) {
    RowDivider()
    DetailRow(stringResource(R.string.label_fortune), "${day.jianChu}日 · ${day.deity}（${day.daoName}）")
    RowDivider()
    WrappingRow(stringResource(R.string.label_suitable), day.suitable.joinToString("、"))
    RowDivider()
    WrappingRow(stringResource(R.string.label_unsuitable), day.unsuitable.joinToString("、"))
    RowDivider()
    WrappingRow(
        stringResource(R.string.label_auspicious_hours),
        day.auspiciousHours.joinToString("\n") { "${it.ganZhi}時 ${it.span} ${it.deity}" },
    )
}

/** A faint rule between the detail rows of the day card. */
@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 2.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

/** A label with a value that may wrap or span several lines, aligned to the end. */
@Composable
private fun WrappingRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun FestivalRow(names: List<String>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            stringResource(R.string.label_festivals),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            names.joinToString("、"),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun MoonRow(phase: MoonPhase) {
    val sizePx = with(LocalDensity.current) { 20.dp.roundToPx() }
    val icon = remember(phase, sizePx) { moonBitmap(phase, sizePx).asImageBitmap() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.label_moon_phase),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.moon_phase_value, phase.name, (phase.illumination * 100).roundToInt()),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Attribution() {
    val linkStyles = TextLinkStyles(
        SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline),
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // These strings carry their links as HTML <a> tags.
        for (id in listOf(R.string.attribution_data, R.string.attribution_festivals)) {
            Text(
                AnnotatedString.fromHtml(stringResource(id), linkStyles),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            stringResource(R.string.attribution_derived),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Material 3 date picker limited to the dataset range. It works in UTC-midnight millis, hence the conversions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickDateDialog(
    initial: LocalDate,
    calendar: CalendarData,
    onDismiss: () -> Unit,
    onPicked: (LocalDate) -> Unit,
) {
    // Material DatePicker speaks UTC-midnight millis.
    fun LocalDate.toUtcMillis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val firstMillis = calendar.firstDate.toUtcMillis()
    val lastMillis = calendar.lastDate.toUtcMillis()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.toUtcMillis().coerceIn(firstMillis, lastMillis),
        yearRange = calendar.firstDate.year..calendar.lastDate.year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis in firstMillis..lastMillis
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let {
                    onPicked(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                } ?: onDismiss()
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    ) {
        DatePicker(state = state)
    }
}

@Preview(showBackground = true)
@Composable
private fun AlmanacScreenPreview() {
    SimpleTungShingTheme {
        val context = LocalContext.current
        val calendar = remember { CalendarData.get(context) }
        AlmanacScreen(
            calendar = calendar,
            settings = WidgetSettings(),
            onSettingsChange = {},
        )
    }
}
