package org.ghostsinthelab.app.simpletungshing.data

import java.time.LocalDate

/**
 * Han traditional festivals and religious observances, following the tables in
 * Wikipedia's 「漢族傳統節日」 article. Where it lists several dates, the one
 * customary in Taiwan is used, otherwise the first; 十成節 (only "around 十月初十")
 * is left out, and 盂蘭節 is folded into 盂蘭盆節. Festivals fall in regular
 * (non-leap) lunar months only.
 */
object Festivals {

    private class Day(val date: LocalDate, val lunar: LunarDay, val calendar: CalendarData) {
        fun isLunar(month: Int, day: Int) = !lunar.isLeapMonth && lunar.month == month && lunar.day == day

        fun isLastDayOf(month: Int) = !lunar.isLeapMonth && lunar.month == month &&
            calendar.lookup(date.plusDays(1))?.day == 1

        fun isTerm(longitude: Double) = date == SolarTerms.dateOf(date.year, longitude)

        /** Whether this is the [n]th 庚 day counting from the solar term (inclusive). */
        fun isNthGengDayFrom(longitude: Double, n: Int): Boolean {
            val term = SolarTerms.dateOf(date.year, longitude)
            // 庚 days repeat every 10 days, so the nth falls 10·(n−1) days after the first.
            val first = (0L..9L).map { term.plusDays(it) }.first { Almanac.dayGanZhi(it)[0] == '庚' }
            return date == first.plusDays(10L * (n - 1))
        }
    }

    private class Festival(val name: String, val matches: Day.() -> Boolean)

    private fun lunar(name: String, month: Int, day: Int) = Festival(name) { isLunar(month, day) }

    private val FOLK = listOf(
        lunar("春節", 1, 1),
        lunar("接神日", 1, 4),
        lunar("隔開日", 1, 5),
        lunar("人日", 1, 7),
        lunar("穀日", 1, 8),
        lunar("順星節", 1, 8),
        lunar("天日節", 1, 9),
        lunar("地日節", 1, 10),
        lunar("元宵節", 1, 15),
        lunar("天穿節", 1, 20),
        lunar("填倉節", 1, 25),
        Festival("正月晦") { isLastDayOf(1) },
        lunar("中和節", 2, 1),
        lunar("龍抬頭", 2, 2),
        lunar("花朝節", 2, 2),
        lunar("春社", 2, 2),
        Festival("寒食節") { date.plusDays(1) == SolarTerms.dateOf(date.year, SolarTerms.QINGMING) },
        Festival("清明節") { isTerm(SolarTerms.QINGMING) },
        lunar("上巳節", 3, 3),
        lunar("端午節", 5, 5),
        lunar("分龍節", 5, 20),
        lunar("會龍節", 5, 25),
        Festival("頭伏日") { isNthGengDayFrom(SolarTerms.XIAZHI, 3) },
        lunar("半年節", 6, 15),
        lunar("天貺節", 6, 6),
        Festival("二伏日") { isNthGengDayFrom(SolarTerms.XIAZHI, 4) },
        lunar("觀蓮節", 6, 24),
        lunar("五穀母節", 6, 25),
        Festival("終伏日") { isNthGengDayFrom(SolarTerms.LIQIU, 1) },
        lunar("七夕", 7, 7),
        lunar("盂蘭盆節", 7, 14),
        lunar("中元節", 7, 15),
        lunar("財神節", 7, 22),
        lunar("地藏節", 7, 29),
        lunar("天灸日", 8, 1),
        lunar("秋社", 8, 15),
        lunar("中秋節", 8, 15),
        lunar("重陽節", 9, 9),
        lunar("寒衣節", 10, 1),
        lunar("下元節", 10, 15),
        Festival("冬至") { isTerm(SolarTerms.DONGZHI) },
        lunar("驅儺日", 12, 7),
        lunar("臘八節", 12, 8),
        lunar("尾牙", 12, 16),
        lunar("祭灶日", 12, 24),
        // The last day of the year, even when a leap twelfth month follows the regular one.
        Festival("除夕") {
            calendar.lookup(date.plusDays(1))?.let { !it.isLeapMonth && it.month == 1 && it.day == 1 } == true
        },
    )

    private val RELIGIOUS = listOf(
        lunar("元始天尊誕", 1, 1),
        lunar("車公誕", 1, 2),
        lunar("清水祖師生日", 1, 6),
        lunar("玉皇誕", 1, 9),
        lunar("丘處機誕", 1, 19),
        lunar("土地神生日", 2, 2),
        lunar("文昌誕", 2, 3),
        lunar("釋迦牟尼出家日", 2, 8),
        lunar("佛滅日", 2, 15),
        lunar("太上老君誕", 2, 15),
        lunar("觀音誕", 2, 19),
        lunar("天后誕", 3, 23),
        lunar("浴佛節", 4, 8),
        lunar("呂祖誕", 4, 14),
        lunar("關帝誕", 6, 16),
        lunar("觀音得道日", 6, 19),
        Festival("川主誕") { isLunar(6, 24) || isLunar(6, 26) },
        lunar("太乙救苦天尊得道日", 8, 8),
        lunar("觀音出家日", 9, 19),
        lunar("太乙救苦天尊誕", 11, 11),
        lunar("彌陀誕", 11, 17),
    )

    /** Names of the festivals on [date], folk festivals first; empty outside the dataset range. */
    fun on(date: LocalDate, calendar: CalendarData): List<String> {
        val lunar = calendar.lookup(date) ?: return emptyList()
        val day = Day(date, lunar, calendar)
        return (FOLK + RELIGIOUS).filter { it.matches(day) }.map { it.name }
    }
}
