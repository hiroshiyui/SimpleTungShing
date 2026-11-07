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
    }

    /** Whether [lunar] is an offering day in any of [enabled]. */
    fun isWorshipDay(lunar: LunarDay, enabled: Set<WorshipGroup>): Boolean =
        enabled.isNotEmpty() && groupsOf(lunar).any { it in enabled }
}
