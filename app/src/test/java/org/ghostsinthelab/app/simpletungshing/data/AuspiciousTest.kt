package org.ghostsinthelab.app.simpletungshing.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AuspiciousTest {
    @Test
    fun monthBranchFollowsJie() {
        // 2024: 小寒 Jan 6, 立春 Feb 4, 大雪 Dec 6.
        assertEquals(0, SolarTerms.monthBranch(LocalDate.of(2024, 1, 1))) // 子
        assertEquals(1, SolarTerms.monthBranch(LocalDate.of(2024, 1, 6))) // 丑 from the 節 day
        assertEquals(1, SolarTerms.monthBranch(LocalDate.of(2024, 2, 3)))
        assertEquals(2, SolarTerms.monthBranch(LocalDate.of(2024, 2, 4))) // 寅
        assertEquals(11, SolarTerms.monthBranch(LocalDate.of(2024, 12, 5))) // 亥
        assertEquals(0, SolarTerms.monthBranch(LocalDate.of(2024, 12, 31))) // 子
    }

    @Test
    fun lunarNewYear2024() {
        // 2024-02-10 is 甲辰日 in 寅月: 滿日, 金匱 (黃道).
        val day = Auspicious.of(LocalDate.of(2024, 2, 10))
        assertEquals("滿", day.jianChu)
        assertEquals("金匱", day.deity)
        assertTrue(day.isHuangDao)
        assertEquals("黃道", day.daoName)
        assertTrue("嫁娶" in day.suitable)
        assertTrue("動土" in day.unsuitable)
        // 辰日: 青龍 starts at 辰; 五鼠遁 from 甲 gives 甲子時.
        assertEquals("甲子", day.hours[0].ganZhi)
        assertEquals("戊辰", day.hours[4].ganZhi)
        assertEquals("青龍", day.hours[4].deity)
        assertEquals(listOf("寅", "辰", "巳", "申", "酉", "亥"), day.auspiciousHours.map { Almanac.branchName(it.branch) })
        assertTrue(day.hours[10].clashesDay) // 戌 clashes 辰
        assertEquals("23–01", day.hours[0].span)
        assertEquals("09–11", day.hours[5].span)
    }

    @Test
    fun jianDay() {
        // 2024-03-03 is 丙寅日 in 寅月: 建日, 天刑 (黑道); 寅日 has 青龍 at 子.
        val day = Auspicious.of(LocalDate.of(2024, 3, 3))
        assertEquals("丙寅", Almanac.dayGanZhi(LocalDate.of(2024, 3, 3)))
        assertEquals("建", day.jianChu)
        assertEquals("天刑", day.deity)
        assertFalse(day.isHuangDao)
        assertEquals("黑道", day.daoName)
        assertEquals("戊子", day.hours[0].ganZhi)
        assertEquals(listOf("子", "丑", "辰", "巳", "未", "戌"), day.auspiciousHours.map { Almanac.branchName(it.branch) })
    }

    @Test
    fun clashingHourIsExcluded() {
        // 2024-02-18 is 壬子日 in 寅月: 開日, 青龍; 子日 has 青龍 at 申, so 午 (司命, 黃道) clashes the day.
        val day = Auspicious.of(LocalDate.of(2024, 2, 18))
        assertEquals("壬子", Almanac.dayGanZhi(LocalDate.of(2024, 2, 18)))
        assertEquals("開", day.jianChu)
        assertEquals("青龍", day.deity)
        val wu = day.hours[6]
        assertEquals("司命", wu.deity)
        assertTrue(wu.isHuangDao)
        assertTrue(wu.clashesDay)
        assertFalse(wu.isAuspicious)
        assertEquals(listOf("子", "丑", "卯", "申", "酉"), day.auspiciousHours.map { Almanac.branchName(it.branch) })
    }

    @Test
    fun poDayOpposesMonth() {
        // 破 is always the branch clashing with the 月建: 2024-02-26 is 庚申日 in 寅月.
        assertEquals("破", Auspicious.of(LocalDate.of(2024, 2, 26)).jianChu)
        assertEquals("庚申", Almanac.dayGanZhi(LocalDate.of(2024, 2, 26)))
        // On the 節 day the value repeats the previous day's: 2024-02-03 (丁酉, 丑月) and 2024-02-04 (戊戌, 寅月) are both 成.
        assertEquals("成", Auspicious.of(LocalDate.of(2024, 2, 3)).jianChu)
        assertEquals("成", Auspicious.of(LocalDate.of(2024, 2, 4)).jianChu)
    }
}
