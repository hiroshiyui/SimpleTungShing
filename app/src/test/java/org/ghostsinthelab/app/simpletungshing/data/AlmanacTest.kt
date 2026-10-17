package org.ghostsinthelab.app.simpletungshing.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AlmanacTest {
    @Test
    fun dayGanZhi() {
        assertEquals("甲子", Almanac.dayGanZhi(LocalDate.of(1949, 10, 1)))
        assertEquals("戊午", Almanac.dayGanZhi(LocalDate.of(2000, 1, 1)))
        assertEquals("甲子", Almanac.dayGanZhi(LocalDate.of(1949, 10, 1).plusDays(600)))
        assertEquals("癸亥", Almanac.dayGanZhi(LocalDate.of(1949, 9, 30)))
    }

    @Test
    fun chongSha() {
        val jiazi = LocalDate.of(1949, 10, 1)
        assertEquals("沖馬(戊午)", Almanac.chong(jiazi))
        assertEquals("煞南", Almanac.sha(jiazi))
        assertEquals("沖猴(庚申)", Almanac.chong(jiazi.plusDays(2))) // 丙寅
        assertEquals("煞北", Almanac.sha(jiazi.plusDays(2)))
        assertEquals("煞東", Almanac.sha(jiazi.plusDays(1))) // 乙丑
        assertEquals("煞西", Almanac.sha(jiazi.plusDays(3))) // 丁卯
    }

    @Test
    fun names() {
        assertEquals("馬", Almanac.zodiacOf("丙午"))
        assertEquals("正月", Almanac.monthName(1, false))
        assertEquals("十二月", Almanac.monthName(12, false))
        assertEquals("閏六月", Almanac.monthName(6, true))
        assertEquals(
            listOf("初一", "初十", "十一", "十九", "二十", "廿一", "廿九", "三十"),
            listOf(1, 10, 11, 19, 20, 21, 29, 30).map(Almanac::dayName),
        )
    }
}
