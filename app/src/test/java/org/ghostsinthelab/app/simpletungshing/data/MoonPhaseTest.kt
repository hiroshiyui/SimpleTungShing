package org.ghostsinthelab.app.simpletungshing.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.min

/** Both [MoonMethod]s: fixed names from the lunar day, and the Meeus phase angle against known new and full moons. */
class MoonPhaseTest {
    private val taipei = ZoneId.of("Asia/Taipei")

    /** 初一 is new, 十五 full, and the quarters fall on about the 8th and 23rd. */
    @Test
    fun lunarDayNames() {
        assertEquals("新月", MoonPhase.fromLunarDay(1).name)
        assertEquals("上弦月", MoonPhase.fromLunarDay(8).name)
        assertEquals("滿月", MoonPhase.fromLunarDay(15).name)
        assertEquals("下弦月", MoonPhase.fromLunarDay(23).name)
        assertEquals("殘月", MoonPhase.fromLunarDay(28).name)
    }

    /** Published syzygy times, read at Taipei noon the same day. */
    @Test
    fun astronomicalFullAndNewMoon() {
        // Full moon 2025-10-07 03:48 UTC; new moon 2025-10-21 12:25 UTC.
        val full = MoonPhase.astronomical(LocalDate.of(2025, 10, 7), taipei)
        assertEquals("滿月", full.name)
        assertTrue(full.illumination > 0.98)
        val new = MoonPhase.astronomical(LocalDate.of(2025, 10, 21), taipei)
        assertEquals("新月", new.name)
        assertTrue(new.illumination < 0.02)
        assertTrue(MoonPhase.astronomical(LocalDate.of(2025, 10, 1), taipei).isWaxing)
        assertTrue(!MoonPhase.astronomical(LocalDate.of(2025, 10, 14), taipei).isWaxing)
    }

    /** Guards both methods at once: over every day of the dataset they never drift more than a tenth of a cycle apart. */
    @Test
    fun methodsAgreeAcrossDataset() {
        val calendar = File("src/main/assets/${CalendarData.ASSET_NAME}").bufferedReader()
            .use { CalendarData.parse(it) }
        var date = calendar.firstDate
        while (date <= calendar.lastDate) {
            val byLunar = MoonPhase.fromLunarDay(calendar.lookup(date)!!.day).cycle
            val byAstronomy = MoonPhase.astronomical(date, taipei).cycle
            val diff = abs(byLunar - byAstronomy)
            // About two days of the cycle: the moon's uneven speed plus noon-versus-day rounding.
            assertTrue("$date: $byLunar vs $byAstronomy", min(diff, 1 - diff) < 0.1)
            date = date.plusDays(1)
        }
    }
}
