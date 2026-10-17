package org.ghostsinthelab.app.simpletungshing.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.time.LocalDate

class CalendarDataTest {
    private val csv = File("src/main/assets/${CalendarData.ASSET_NAME}")
    private val calendar = csv.bufferedReader().use { CalendarData.parse(it) }

    @Test
    fun coversWholeDataset() {
        assertEquals(LocalDate.of(2022, 1, 1), calendar.firstDate)
        assertEquals(LocalDate.of(2050, 12, 31), calendar.lastDate)
        assertNull(calendar.lookup(LocalDate.of(2021, 12, 31)))
        assertNull(calendar.lookup(LocalDate.of(2051, 1, 1)))
    }

    @Test
    fun weekdayColumnMatchesDates() {
        csv.readLines().drop(1).filter { it.isNotBlank() && !it.startsWith(",") }.forEach { line ->
            val fields = line.split(',')
            assertEquals(line, fields[5].toInt(), LocalDate.parse(fields[0]).dayOfWeek.value)
        }
    }

    @Test
    fun lunarNewYear2026() {
        val day = calendar.lookup(LocalDate.of(2026, 2, 17))!!
        assertEquals(115, day.rocYear)
        assertEquals("丙午", day.yearGanZhi)
        assertEquals("正月初一", Almanac.lunarDateName(day))
    }

    @Test
    fun leapMonthAndLength() {
        val day = calendar.lookup(LocalDate.of(2023, 3, 22))!!
        assertEquals(true, day.isLeapMonth)
        assertEquals(29, day.monthLength)
        assertEquals("閏二月(小)初一", Almanac.lunarDateName(day, withSize = true))
        assertEquals(false, calendar.lookup(LocalDate.of(2023, 4, 20))!!.isLeapMonth)
    }

    @Test
    fun monthLengthUnknownAtDatasetEdges() {
        assertNull(calendar.lookup(LocalDate.of(2022, 1, 1))!!.monthLength)
        assertNull(calendar.lookup(LocalDate.of(2050, 12, 31))!!.monthLength)
    }
}
