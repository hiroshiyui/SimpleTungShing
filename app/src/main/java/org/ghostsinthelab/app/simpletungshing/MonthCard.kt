package org.ghostsinthelab.app.simpletungshing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ghostsinthelab.app.simpletungshing.data.Almanac
import org.ghostsinthelab.app.simpletungshing.data.CalendarData
import org.ghostsinthelab.app.simpletungshing.data.SolarTerms
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

/** One day of the month grid; [label] is null outside the dataset range. */
private class MonthCell(val date: LocalDate, val label: String?, val highlighted: Boolean)

/**
 * The month of [selected] as a Sunday-first grid, each day with its 節氣, or else its
 * lunar day (the month name on 初一). Tapping a day or stepping months calls [onSelect].
 */
@Composable
fun MonthCard(selected: LocalDate, calendar: CalendarData, onSelect: (LocalDate) -> Unit) {
    val month = YearMonth.from(selected)
    val today = LocalDate.now()
    val locale = LocalConfiguration.current.locales[0]
    val cells = remember(month, calendar) {
        (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            val lunar = calendar.lookup(date)
            val term = lunar?.let { SolarTerms.termOn(date) }
            when {
                lunar == null -> MonthCell(date, null, false)
                term != null -> MonthCell(date, term, true)
                lunar.day == 1 -> MonthCell(date, Almanac.monthName(lunar.month, lunar.isLeapMonth), true)
                else -> MonthCell(date, Almanac.dayName(lunar.day), false)
            }
        }
    }
    // Blank cells before the 1st so that columns run Sunday to Saturday.
    val weeks = (List(month.atDay(1).dayOfWeek.value % 7) { null } + cells).chunked(7)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MonthStep("‹", stringResource(R.string.previous_month), month > YearMonth.from(calendar.firstDate)) {
                    onSelect(clamp(selected.minusMonths(1), calendar))
                }
                Text(
                    month.format(DateTimeFormatter.ofPattern(stringResource(R.string.month_title_pattern), locale)),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                MonthStep("›", stringResource(R.string.next_month), month < YearMonth.from(calendar.lastDate)) {
                    onSelect(clamp(selected.plusMonths(1), calendar))
                }
            }
            Row {
                for (i in 0L..6L) {
                    Text(
                        DayOfWeek.SUNDAY.plus(i).getDisplayName(TextStyle.SHORT, locale),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            for (week in weeks) {
                Row {
                    for (i in 0..6) {
                        val cell = week.getOrNull(i)
                        val cellModifier = Modifier.weight(1f).height(52.dp).padding(2.dp)
                        if (cell == null) {
                            Column(cellModifier) {}
                        } else {
                            DayCell(cell, cell.date == selected, cell.date == today, cellModifier) {
                                onSelect(cell.date)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthStep(symbol: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.semantics { contentDescription = description },
    ) { Text(symbol, fontSize = 24.sp) }
}

@Composable
private fun DayCell(cell: MonthCell, isSelected: Boolean, isToday: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(8.dp)
    val inRange = cell.label != null
    Column(
        modifier = modifier
            .clip(shape)
            .background(if (isSelected) colors.primary else Color.Transparent)
            .border(1.dp, if (isToday && !isSelected) colors.primary else Color.Transparent, shape)
            .clickable(enabled = inRange, onClick = onClick)
            .alpha(if (inRange) 1f else 0.38f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            cell.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colors.onPrimary else colors.onSurface,
        )
        cell.label?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    isSelected -> colors.onPrimary
                    cell.highlighted -> colors.primary
                    else -> colors.onSurfaceVariant
                },
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/** Keeps month stepping inside the dataset range. */
private fun clamp(date: LocalDate, calendar: CalendarData): LocalDate = when {
    date < calendar.firstDate -> calendar.firstDate
    date > calendar.lastDate -> calendar.lastDate
    else -> date
}
