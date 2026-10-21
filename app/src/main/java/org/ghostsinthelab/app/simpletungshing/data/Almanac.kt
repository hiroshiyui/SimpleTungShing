package org.ghostsinthelab.app.simpletungshing.data

import java.time.LocalDate

/**
 * Traditional almanac (通書) items derived from the date by fixed rules.
 * These are computed by this app, not provided by the CWA dataset.
 */
object Almanac {
    private const val STEMS = "甲乙丙丁戊己庚辛壬癸"
    private const val BRANCHES = "子丑寅卯辰巳午未申酉戌亥"
    private const val ZODIAC = "鼠牛虎兔龍蛇馬羊猴雞狗豬"
    private const val NUMERALS = "一二三四五六七八九十"

    /** A known 甲子 day, the start of the 60-day cycle. */
    private val JIAZI_DAY = LocalDate.of(1949, 10, 1)

    private fun ganZhi(stem: Int, branch: Int) = "${STEMS[stem]}${BRANCHES[branch]}"

    private fun dayCycleIndex(date: LocalDate): Int =
        Math.floorMod(date.toEpochDay() - JIAZI_DAY.toEpochDay(), 60L).toInt()

    /** The day's heavenly stem, 0 = 甲 … 9 = 癸. */
    fun dayStem(date: LocalDate): Int = dayCycleIndex(date) % 10

    /** The day's earthly branch, 0 = 子 … 11 = 亥. */
    fun dayBranch(date: LocalDate): Int = dayCycleIndex(date) % 12

    fun stemName(stem: Int): String = STEMS[stem].toString()
    fun branchName(branch: Int): String = BRANCHES[branch].toString()

    /** 日干支, e.g. "甲子". */
    fun dayGanZhi(date: LocalDate): String {
        val i = dayCycleIndex(date)
        return ganZhi(i % 10, i % 12)
    }

    /** 生肖 of a sexagenary name, e.g. "丙午" -> "馬". */
    fun zodiacOf(ganZhi: String): String = ZODIAC[BRANCHES.indexOf(ganZhi[1])].toString()

    /** 沖, e.g. 甲子日 -> "沖馬(戊午)": opposing branch, stem the day stem overcomes. */
    fun chong(date: LocalDate): String {
        val i = dayCycleIndex(date)
        val branch = (i + 6) % 12
        return "沖${ZODIAC[branch]}(${ganZhi((i + 4) % 10, branch)})"
    }

    /** 煞方: 申子辰煞南、寅午戌煞北、巳酉丑煞東、亥卯未煞西. */
    fun sha(date: LocalDate): String {
        val direction = when (dayCycleIndex(date) % 12 % 4) {
            0 -> "南" // 申子辰
            1 -> "東" // 巳酉丑
            2 -> "北" // 寅午戌
            else -> "西" // 亥卯未
        }
        return "煞$direction"
    }

    fun monthName(month: Int, isLeap: Boolean): String {
        val name = when (month) {
            1 -> "正"
            in 2..10 -> NUMERALS[month - 1].toString()
            else -> "十" + NUMERALS[month - 11]
        }
        return (if (isLeap) "閏" else "") + name + "月"
    }

    fun dayName(day: Int): String = when {
        day <= 10 -> "初" + NUMERALS[day - 1]
        day < 20 -> "十" + NUMERALS[day - 11]
        day == 20 -> "二十"
        day < 30 -> "廿" + NUMERALS[day - 21]
        else -> "三十"
    }

    /** e.g. "閏二月初一"; with [withSize], "九月(小)初七". */
    fun lunarDateName(lunar: LunarDay, withSize: Boolean = false): String {
        val size = when (lunar.monthLength) {
            30 -> "(大)"
            29 -> "(小)"
            else -> ""
        }
        return monthName(lunar.month, lunar.isLeapMonth) +
            (if (withSize) size else "") +
            dayName(lunar.day)
    }
}
