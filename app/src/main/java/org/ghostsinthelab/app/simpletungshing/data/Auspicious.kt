package org.ghostsinthelab.app.simpletungshing.data

import java.time.LocalDate

/**
 * App-derived 吉凶／宜忌／吉時, by the fixed rules of the 建除 and 黃道黑道
 * systems as summarised from the《協紀辨方書》tradition. Everything here
 * is keyed to the 干支 calendar: the solar month (switching on each 節,
 * see [SolarTerms.monthBranch]) and the day's stem and branch.
 *
 * This is a simplified reading: the day's 建除 value and 黃道／黑道 deity,
 * the 宜忌 that 通書 commonly attach to each 建除 value, and the 吉時 from the
 * hour deities. The full 神煞 tables (天德、月德、往亡 …) and the
 * 吉凶相抵 arbitration that commercial almanacs add are not applied, so
 * a published 農民曆 may differ. The CWA dataset carries none of this.
 */
object Auspicious {
    private const val JIANCHU = "建除滿平定執破危成收開閉"

    /** 黃道黑道十二神 in cycle order, starting from 青龍. */
    private val DEITIES = listOf(
        "青龍", "明堂", "天刑", "朱雀", "金匱", "天德", "白虎", "玉堂", "天牢", "玄武", "司命", "勾陳",
    )
    private val HUANGDAO = setOf(0, 1, 4, 5, 7, 10) // 青龍、明堂、金匱、天德、玉堂、司命

    /** 宜 and 忌 commonly given for each 建除 value, in [JIANCHU] order. */
    private val ADVICE = listOf(
        listOf("出行", "上任", "會友", "祈福", "求財", "上書") to listOf("動土", "開倉", "安葬", "掘井"),
        listOf("祭祀", "除服", "療病", "出行", "嫁娶", "沐浴", "掃舍") to listOf("求官", "上任", "開張", "搬家"),
        listOf("祈福", "祭祀", "嫁娶", "開市", "交易", "納財", "入學") to listOf("服藥", "栽種", "動土"),
        listOf("修造", "平治道塗", "嫁娶", "出行", "祭祀") to listOf("栽種", "掘井", "求醫"),
        listOf("嫁娶", "入宅", "開市", "交易", "祭祀", "安床") to listOf("訴訟", "出行", "求醫"),
        listOf("捕捉", "納采", "嫁娶", "造屋", "祭祀") to listOf("開市", "出行", "搬家", "納財"),
        listOf("破屋", "壞垣", "治病") to listOf("嫁娶", "開市", "搬家", "祭祀"),
        listOf("安床", "祭祀", "祈福") to listOf("出行", "登高", "嫁娶", "搬家"),
        listOf("嫁娶", "入宅", "開市", "祭祀", "入學", "安葬") to listOf("訴訟"),
        listOf("納財", "收穫", "祭祀", "納畜", "嫁娶") to listOf("開市", "出行", "安葬"),
        listOf("開市", "嫁娶", "入宅", "祭祀", "出行", "求財") to listOf("安葬", "破土"),
        listOf("安葬", "築堤", "填補") to listOf("開市", "出行", "嫁娶", "求醫"),
    )

    class Day(
        /** 建除十二神, e.g. "建". */
        val jianChu: String,
        /** 黃道黑道十二神 of the day, e.g. "青龍". */
        val deity: String,
        /** True for a 黃道 (auspicious) deity, false for 黑道. */
        val isHuangDao: Boolean,
        val suitable: List<String>,
        val unsuitable: List<String>,
        /** The twelve 時辰 from 子 to 亥. */
        val hours: List<Hour>,
    ) {
        /** e.g. "黃道" or "黑道". */
        val daoName: String get() = if (isHuangDao) "黃道" else "黑道"
        val auspiciousHours: List<Hour> get() = hours.filter { it.isAuspicious }
    }

    class Hour(
        /** 0 = 子 … 11 = 亥. */
        val branch: Int,
        /** 時干支 by 五鼠遁, e.g. "甲子". */
        val ganZhi: String,
        val deity: String,
        val isHuangDao: Boolean,
        /** True when the hour branch clashes with the day branch (六沖). */
        val clashesDay: Boolean,
    ) {
        /** 黃道 and not clashing with the day. */
        val isAuspicious: Boolean get() = isHuangDao && !clashesDay
        /** e.g. "子時". */
        val name: String get() = "${Almanac.branchName(branch)}時"
        /** Clock span of the double hour, e.g. "23–01". */
        val span: String get() {
            val start = (branch * 2 + 23) % 24
            return "%02d–%02d".format(start, (start + 2) % 24)
        }
    }

    /** The 青龍 branch for a cycle keyed by [branch]: 寅申 → 子, 卯酉 → 寅, 辰戌 → 辰, … */
    private fun qingLongStart(branch: Int) = Math.floorMod(branch - 2, 6) * 2

    fun of(date: LocalDate): Day {
        val monthBranch = SolarTerms.monthBranch(date)
        val dayStem = Almanac.dayStem(date)
        val dayBranch = Almanac.dayBranch(date)
        val jianChu = Math.floorMod(dayBranch - monthBranch, 12)
        val dayDeity = Math.floorMod(dayBranch - qingLongStart(monthBranch), 12)
        val hourStart = qingLongStart(dayBranch)
        val hours = (0 until 12).map { h ->
            val deity = Math.floorMod(h - hourStart, 12)
            Hour(
                branch = h,
                ganZhi = Almanac.stemName((dayStem % 5 * 2 + h) % 10) + Almanac.branchName(h),
                deity = DEITIES[deity],
                isHuangDao = deity in HUANGDAO,
                clashesDay = h == (dayBranch + 6) % 12,
            )
        }
        return Day(
            jianChu = JIANCHU[jianChu].toString(),
            deity = DEITIES[dayDeity],
            isHuangDao = dayDeity in HUANGDAO,
            suitable = ADVICE[jianChu].first,
            unsuitable = ADVICE[jianChu].second,
            hours = hours,
        )
    }
}
