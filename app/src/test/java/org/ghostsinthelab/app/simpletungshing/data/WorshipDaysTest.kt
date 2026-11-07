package org.ghostsinthelab.app.simpletungshing.data

import org.ghostsinthelab.app.simpletungshing.data.WorshipGroup.FESTIVALS
import org.ghostsinthelab.app.simpletungshing.data.WorshipGroup.FIRST_AND_FIFTEENTH
import org.ghostsinthelab.app.simpletungshing.data.WorshipGroup.SECOND_AND_SIXTEENTH
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

/** [WorshipDays] against the real dataset. */
class WorshipDaysTest {
    private val calendar = File("src/main/assets/${CalendarData.ASSET_NAME}").bufferedReader()
        .use { CalendarData.parse(it) }

    private fun groups(year: Int, month: Int, day: Int) =
        WorshipDays.groupsOf(calendar.lookup(LocalDate.of(year, month, day))!!)

    @Test
    fun monthlyDays() {
        assertEquals(setOf(FIRST_AND_FIFTEENTH), groups(2025, 1, 29))   // 乙巳年正月初一
        assertEquals(setOf(SECOND_AND_SIXTEENTH), groups(2025, 1, 30))  // 初二
        assertEquals(setOf(FIRST_AND_FIFTEENTH), groups(2025, 3, 14))   // 二月十五
        assertEquals(setOf(SECOND_AND_SIXTEENTH), groups(2025, 2, 13))  // 十六
        assertEquals(emptySet<WorshipGroup>(), groups(2025, 2, 14))   // 十七
        assertEquals(emptySet<WorshipGroup>(), groups(2025, 2, 11))   // 十四
    }

    @Test
    fun leapMonthStillHasMonthlyDays() {
        // 2025 閏六月初一 = 2025-07-25
        assertTrue(calendar.lookup(LocalDate.of(2025, 7, 25))!!.isLeapMonth)
        assertEquals(setOf(FIRST_AND_FIFTEENTH), groups(2025, 7, 25))
    }

    @Test
    fun festivals() {
        assertEquals(setOf(FESTIVALS), groups(2025, 5, 31))                       // 端午
        assertEquals(setOf(FIRST_AND_FIFTEENTH, FESTIVALS), groups(2025, 10, 6))  // 中秋, also 十五
        assertEquals(setOf(FESTIVALS), groups(2025, 10, 29))                      // 重陽
        assertEquals(setOf(FESTIVALS), groups(2025, 2, 6))                        // 天公生 正月初九
        assertEquals(setOf(FIRST_AND_FIFTEENTH, FESTIVALS), groups(2025, 2, 12))  // 元宵
        assertEquals(setOf(FESTIVALS), groups(2025, 4, 4))                        // 清明
        assertEquals(setOf(FESTIVALS), groups(2025, 8, 29))                       // 七夕
        assertEquals(setOf(FIRST_AND_FIFTEENTH, FESTIVALS), groups(2025, 9, 6))   // 中元
        assertEquals(setOf(SECOND_AND_SIXTEENTH, FESTIVALS), groups(2025, 12, 21)) // 冬至, also 十一月初二
        assertEquals(setOf(FESTIVALS), groups(2026, 2, 11))                       // 送神 十二月廿四
        assertEquals(setOf(FESTIVALS), groups(2026, 2, 16))                       // 除夕
        assertEquals(emptySet<WorshipGroup>(), groups(2026, 2, 15))               // 十二月廿八
        assertEquals(emptySet<WorshipGroup>(), groups(2025, 10, 28))            // 九月初八
    }

    @Test
    fun enabledGroupsFilter() {
        val zuoYa = calendar.lookup(LocalDate.of(2025, 1, 30))!!
        assertTrue(WorshipDays.isWorshipDay(zuoYa, setOf(SECOND_AND_SIXTEENTH)))
        assertFalse(WorshipDays.isWorshipDay(zuoYa, setOf(FIRST_AND_FIFTEENTH, FESTIVALS)))
        assertFalse(WorshipDays.isWorshipDay(zuoYa, emptySet()))
    }
}
