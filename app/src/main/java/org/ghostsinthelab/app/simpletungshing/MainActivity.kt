package org.ghostsinthelab.app.simpletungshing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ghostsinthelab.app.simpletungshing.data.Almanac
import org.ghostsinthelab.app.simpletungshing.data.CalendarData
import org.ghostsinthelab.app.simpletungshing.data.Festivals
import org.ghostsinthelab.app.simpletungshing.data.MoonPhase
import org.ghostsinthelab.app.simpletungshing.data.SolarTerms
import org.ghostsinthelab.app.simpletungshing.ui.theme.SimpleTungShingTheme
import org.ghostsinthelab.app.simpletungshing.widget.TungShingWidgetReceiver
import org.ghostsinthelab.app.simpletungshing.widget.WidgetSettings
import org.ghostsinthelab.app.simpletungshing.widget.moonBitmap
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

private const val DATASET_URL = "https://opendata.cwa.gov.tw/dataset/astronomy/A-A0087-001"
private const val RULES_URL = "https://opendata.cwa.gov.tw/about/rules"
private const val FESTIVALS_URL = "https://zh.wikipedia.org/zh-tw/漢族傳統節日"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TungShingWidgetReceiver.scheduleMidnightRefresh(this)
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
                    if (currentCalendar != null && currentSettings != null) {
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

@Composable
fun AlmanacScreen(
    calendar: CalendarData,
    settings: WidgetSettings,
    onSettingsChange: (WidgetSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var epochDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var picking by rememberSaveable { mutableStateOf(false) }
    val date = LocalDate.ofEpochDay(epochDay)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
        WidgetSettingsSection(settings, onSettingsChange)
        Spacer(Modifier.height(8.dp))
        Attribution()
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
            Text(
                "民國${rocYear}年（${date.year}）${date.monthValue}月",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                date.dayOfMonth.toString(),
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(Almanac.weekdayName(date.dayOfWeek), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            if (lunar == null) {
                Text(
                    stringResource(R.string.out_of_range, calendar.firstDate, calendar.lastDate),
                    style = MaterialTheme.typography.bodyLarge,
                )
                return@Column
            }
            DetailRow(stringResource(R.string.label_lunar), Almanac.lunarDateName(lunar, withSize = true))
            SolarTerms.termOn(date)?.let { DetailRow(stringResource(R.string.label_solar_term), it) }
            Festivals.on(date, calendar).takeIf { it.isNotEmpty() }?.let { FestivalRow(it) }
            DetailRow(
                stringResource(R.string.label_year),
                "${lunar.yearGanZhi}年 · 生肖${Almanac.zodiacOf(lunar.yearGanZhi)}",
            )
            DetailRow(stringResource(R.string.label_day_ganzhi), "${Almanac.dayGanZhi(date)}日")
            DetailRow(stringResource(R.string.label_chong_sha), "${Almanac.chong(date)} ${Almanac.sha(date)}")
            MoonPhase.of(settings.moonMethod, date, lunar)?.let { MoonRow(it) }
        }
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
    val source = stringResource(R.string.attribution_source)
    val license = stringResource(R.string.attribution_license)
    val festivals = stringResource(R.string.attribution_festivals)
    val text = buildAnnotatedString {
        withLink(LinkAnnotation.Url(DATASET_URL, linkStyles)) { append(source) }
        append("（A-A0087-001），")
        withLink(LinkAnnotation.Url(RULES_URL, linkStyles)) { append(license) }
        append("使用。")
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text, style = MaterialTheme.typography.bodySmall)
        Text(
            buildAnnotatedString {
                withLink(LinkAnnotation.Url(FESTIVALS_URL, linkStyles)) { append(festivals) }
                append("（CC BY-SA 4.0）。")
            },
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            stringResource(R.string.attribution_derived),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

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
