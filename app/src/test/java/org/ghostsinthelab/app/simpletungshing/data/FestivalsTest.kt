package org.ghostsinthelab.app.simpletungshing.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

class FestivalsTest {
    private val calendar = File("src/main/assets/${CalendarData.ASSET_NAME}").bufferedReader()
        .use { CalendarData.parse(it) }

    private fun on(year: Int, month: Int, day: Int) = Festivals.on(LocalDate.of(year, month, day), calendar)

    @Test
    fun solarTermDates() {
        // Taiwan-time dates published by CWA.
        assertEquals(LocalDate.of(2025, 4, 4), SolarTerms.dateOf(2025, SolarTerms.QINGMING))
        assertEquals(LocalDate.of(2026, 4, 5), SolarTerms.dateOf(2026, SolarTerms.QINGMING))
        assertEquals(LocalDate.of(2025, 6, 21), SolarTerms.dateOf(2025, SolarTerms.XIAZHI))
        assertEquals(LocalDate.of(2025, 8, 7), SolarTerms.dateOf(2025, SolarTerms.LIQIU))
        assertEquals(LocalDate.of(2024, 12, 21), SolarTerms.dateOf(2024, SolarTerms.DONGZHI))
        assertEquals(LocalDate.of(2025, 12, 21), SolarTerms.dateOf(2025, SolarTerms.DONGZHI))
    }

    @Test
    fun solarTermNames() {
        assertEquals("立春", SolarTerms.termOn(LocalDate.of(2025, 2, 3)))
        assertEquals("春分", SolarTerms.termOn(LocalDate.of(2025, 3, 20)))
        assertEquals("霜降", SolarTerms.termOn(LocalDate.of(2025, 10, 23)))
        assertEquals("立春", SolarTerms.termOn(LocalDate.of(2026, 2, 4)))
        assertEquals(null, SolarTerms.termOn(LocalDate.of(2026, 10, 17)))
        for (year in 2022..2050) {
            val terms = generateSequence(LocalDate.of(year, 1, 1)) { it.plusDays(1) }
                .takeWhile { it.year == year }
                .mapNotNull { SolarTerms.termOn(it) }
                .toList()
            assertEquals("$year", 24, terms.toSet().size)
            assertEquals("$year", 24, terms.size)
        }
    }

    @Test
    fun lunarFestivals() {
        assertEquals(listOf("除夕"), on(2026, 2, 16))
        assertEquals(listOf("春節", "元始天尊誕"), on(2026, 2, 17))
        assertEquals(listOf("秋社", "中秋節"), on(2025, 10, 6))
        assertEquals(listOf("天后誕"), on(2026, 5, 9))
        assertEquals(emptyList<String>(), on(2026, 10, 17))
    }

    @Test
    fun solarFestivals() {
        assertEquals(listOf("寒食節"), on(2025, 4, 3))
        assertEquals(listOf("清明節"), on(2025, 4, 4))
        assertTrue("冬至" in on(2025, 12, 21))
        // 三伏 2025: 7/20, 7/30, 8/9.
        assertTrue("頭伏日" in on(2025, 7, 20))
        assertTrue("二伏日" in on(2025, 7, 30))
        assertTrue("終伏日" in on(2025, 8, 9))
    }

    @Test
    fun eachYearHasOneOfEverySolarFestival() {
        val solar = listOf("寒食節", "清明節", "頭伏日", "二伏日", "終伏日", "冬至")
        for (year in calendar.firstDate.year..calendar.lastDate.year) {
            val names = generateSequence(LocalDate.of(year, 1, 1)) { it.plusDays(1) }
                .takeWhile { it.year == year }
                .flatMap { Festivals.on(it, calendar) }
                .toList()
            solar.forEach { assertEquals("$year $it", 1, names.count { name -> name == it }) }
            assertEquals("$year 春節", 1, names.count { it == "春節" })
        }
    }
}
