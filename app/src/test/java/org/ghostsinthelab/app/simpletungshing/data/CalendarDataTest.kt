package org.ghostsinthelab.app.simpletungshing.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.time.LocalDate

/**
 * Parses the real bundled CSV (relative path, so run from the `app/` module as Gradle does)
 * and checks the parser against facts about the dataset: its range, its contiguity (via
 * the weekday column), leap-month handling and the 大/小 month lengths.
 */
class CalendarDataTest {
    private val csv = File("src/main/assets/${CalendarData.ASSET_NAME}")
    private val calendar = csv.bufferedReader().use { CalendarData.parse(it) }

    /** The range CWA published, and null just outside it. */
    @Test
    fun coversWholeDataset() {
        assertEquals(LocalDate.of(2022, 1, 1), calendar.firstDate)
        assertEquals(LocalDate.of(2050, 12, 31), calendar.lastDate)
        assertNull(calendar.lookup(LocalDate.of(2021, 12, 31)))
        assertNull(calendar.lookup(LocalDate.of(2051, 1, 1)))
    }

    /** Cross-checks the 星期 column with java.time, which also catches a corrupted or shifted date column. */
    @Test
    fun weekdayColumnMatchesDates() {
        csv.readLines().drop(1).filter { it.isNotBlank() && !it.startsWith(",") }.forEach { line ->
            val fields = line.split(',')
            assertEquals(line, fields[5].toInt(), LocalDate.parse(fields[0]).dayOfWeek.value)
        }
    }

    @Test
    fun runsOutAfterLastDate() {
        assertEquals(false, calendar.hasRunOut(LocalDate.of(2050, 12, 31)))
        assertEquals(true, calendar.hasRunOut(LocalDate.of(2051, 1, 1)))
    }

    /** A known row: 2026-02-17 is 民國115年 丙午年 正月初一. */
    @Test
    fun lunarNewYear2026() {
        val day = calendar.lookup(LocalDate.of(2026, 2, 17))!!
        assertEquals(115, day.rocYear)
        assertEquals("丙午", day.yearGanZhi)
        assertEquals("正月初一", Almanac.lunarDateName(day))
    }

    /** 2023 has a 閏二月 of 29 days; the regular 二月 before it must not be flagged leap. */
    @Test
    fun leapMonthAndLength() {
        val day = calendar.lookup(LocalDate.of(2023, 3, 22))!!
        assertEquals(true, day.isLeapMonth)
        assertEquals(29, day.monthLength)
        assertEquals("閏二月(小)初一", Almanac.lunarDateName(day, withSize = true))
        assertEquals(false, calendar.lookup(LocalDate.of(2023, 4, 20))!!.isLeapMonth)
    }

    /** The first and last lunar months are cut off, so their length cannot be derived. */
    @Test
    fun monthLengthUnknownAtDatasetEdges() {
        assertNull(calendar.lookup(LocalDate.of(2022, 1, 1))!!.monthLength)
        assertNull(calendar.lookup(LocalDate.of(2050, 12, 31))!!.monthLength)
    }
}
