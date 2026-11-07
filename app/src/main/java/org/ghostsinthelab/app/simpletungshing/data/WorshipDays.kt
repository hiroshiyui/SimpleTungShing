package org.ghostsinthelab.app.simpletungshing.data

/** A group of customary offering days (拜拜) the user can mark in the month grid. */
enum class WorshipGroup {
    /** 初一 and 十五, the household offerings to the gods and ancestors. */
    FIRST_AND_FIFTEENTH,
    /** 初二 and 十六, 做牙 to 土地公, kept mainly by shops and businesses. */
    SECOND_AND_SIXTEENTH,
    /**
     * The festivals on which households make offerings: 除夕, 天公生, 元宵, 清明, 端午, 七夕,
     * 中元, 中秋, 重陽, 冬至 and 送神.
     */
    FESTIVALS,
    /**
     * Birthdays of the deities most temples in Taiwan celebrate (神明聖誕), on the dates
     * customary in Taiwan (e.g. 關聖帝君 on 六月廿四). Individual temples may differ.
     */
    DEITY_BIRTHDAYS,
}

/**
 * Days on which Taiwanese households and shops customarily make offerings, by
 * [WorshipGroup]. App-derived from the lunar date, not CWA data. The monthly groups
 * count in leap months too; the festivals only in regular months.
 */
object WorshipDays {
    /** Lunar (month, day) festivals; 除夕, 清明 and 冬至 are matched separately. */
    private val FESTIVALS = setOf(
        1 to 9,   // 天公生
        1 to 15,  // 元宵
        5 to 5,   // 端午
        7 to 7,   // 七夕
        7 to 15,  // 中元
        8 to 15,  // 中秋
        9 to 9,   // 重陽
        12 to 24, // 送神
    )

    /** Lunar (month, day) deity birthdays; 地藏王 (the last day of the 7th month) is matched separately. */
    private val DEITY_BIRTHDAYS = setOf(
        1 to 6,   // 清水祖師
        2 to 2,   // 土地公
        2 to 3,   // 文昌帝君
        2 to 19,  // 觀音佛祖
        3 to 3,   // 玄天上帝
        3 to 15,  // 保生大帝
        3 to 20,  // 註生娘娘
        3 to 23,  // 媽祖
        4 to 8,   // 釋迦佛祖
        4 to 26,  // 神農大帝
        6 to 19,  // 觀音得道
        6 to 24,  // 關聖帝君
        7 to 18,  // 瑤池金母
        8 to 3,   // 灶君
        9 to 9,   // 中壇元帥
        9 to 19,  // 觀音出家
    )

    private fun isDeityBirthday(lunar: LunarDay): Boolean = !lunar.isLeapMonth &&
        ((lunar.month to lunar.day) in DEITY_BIRTHDAYS || (lunar.month == 7 && lunar.day == lunar.monthLength))

    private fun isFestival(lunar: LunarDay): Boolean {
        if (lunar.isLeapMonth) return false
        if ((lunar.month to lunar.day) in FESTIVALS) return true
        // 除夕: the last day of the 12th month; unknown (null) only at the dataset's edges.
        if (lunar.month == 12 && lunar.day == lunar.monthLength) return true
        val date = lunar.date
        return date == SolarTerms.dateOf(date.year, SolarTerms.QINGMING) ||
            date == SolarTerms.dateOf(date.year, SolarTerms.DONGZHI)
    }

    /** The groups [lunar] belongs to; 元宵, 中元 and 中秋 are in both the festival and the 十五 group. */
    fun groupsOf(lunar: LunarDay): Set<WorshipGroup> = buildSet {
        if (lunar.day == 1 || lunar.day == 15) add(WorshipGroup.FIRST_AND_FIFTEENTH)
        if (lunar.day == 2 || lunar.day == 16) add(WorshipGroup.SECOND_AND_SIXTEENTH)
        if (isFestival(lunar)) add(WorshipGroup.FESTIVALS)
        if (isDeityBirthday(lunar)) add(WorshipGroup.DEITY_BIRTHDAYS)
    }

    /** The groups of [enabled] that [lunar] belongs to; empty when it is not a marked day. */
    fun enabledGroupsOf(lunar: LunarDay, enabled: Set<WorshipGroup>): Set<WorshipGroup> =
        if (enabled.isEmpty()) emptySet() else groupsOf(lunar).filterTo(mutableSetOf()) { it in enabled }

    /** Whether [lunar] is an offering day in any of [enabled]. */
    fun isWorshipDay(lunar: LunarDay, enabled: Set<WorshipGroup>): Boolean =
        enabledGroupsOf(lunar, enabled).isNotEmpty()
}
