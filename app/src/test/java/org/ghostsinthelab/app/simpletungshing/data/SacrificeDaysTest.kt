package org.ghostsinthelab.app.simpletungshing.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.time.LocalDate

/** [SacrificeDays] against the real dataset. */
class SacrificeDaysTest {
    private val calendar = File("src/main/assets/${CalendarData.ASSET_NAME}").bufferedReader()
        .use { CalendarData.parse(it) }

    private fun on(year: Int, month: Int, day: Int) =
        SacrificeDays.isSacrificeDay(calendar.lookup(LocalDate.of(year, month, day))!!)

    @Test
    fun monthlyDays() {
        assertEquals(true, on(2025, 1, 29))   // 乙巳年正月初一
        assertEquals(true, on(2025, 1, 30))   // 初二
        assertEquals(true, on(2025, 2, 12))   // 十五
        assertEquals(true, on(2025, 2, 13))   // 十六
        assertEquals(false, on(2025, 2, 14))  // 十七
        assertEquals(false, on(2025, 2, 11))  // 十四
    }

    @Test
    fun leapMonthStillHasMonthlyDays() {
        // 2025 閏六月初一 = 2025-07-25
        assertEquals(true, on(2025, 7, 25))
        assertEquals(true, calendar.lookup(LocalDate.of(2025, 7, 25))!!.isLeapMonth)
    }

    @Test
    fun festivals() {
        assertEquals(true, on(2025, 5, 31))   // 端午
        assertEquals(true, on(2025, 10, 6))   // 中秋
        assertEquals(true, on(2025, 10, 29))  // 重陽
        assertEquals(false, on(2025, 10, 28)) // 九月初八
    }
}
