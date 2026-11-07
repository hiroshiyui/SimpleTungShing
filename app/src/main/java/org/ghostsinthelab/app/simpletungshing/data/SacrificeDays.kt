package org.ghostsinthelab.app.simpletungshing.data

/** A group of customary offering days (拜拜) the user can mark in the month grid. */
enum class SacrificeGroup {
    /** 初一 and 十五, the household offerings to the gods and ancestors. */
    FIRST_AND_FIFTEENTH,
    /** 初二 and 十六, 做牙 to 土地公, kept mainly by shops and businesses. */
    SECOND_AND_SIXTEENTH,
    /** 端午, 中秋 and 重陽. */
    FESTIVALS,
}

/**
 * Days on which Taiwanese households and shops customarily make offerings, by
 * [SacrificeGroup]. App-derived from the lunar date, not CWA data. The monthly groups
 * count in leap months too; the festivals only in regular months.
 */
object SacrificeDays {
    private val FESTIVALS = setOf(5 to 5, 8 to 15, 9 to 9)

    /** The groups [lunar] belongs to; 中秋 is in both the festival and the 十五 group. */
    fun groupsOf(lunar: LunarDay): Set<SacrificeGroup> = buildSet {
        if (lunar.day == 1 || lunar.day == 15) add(SacrificeGroup.FIRST_AND_FIFTEENTH)
        if (lunar.day == 2 || lunar.day == 16) add(SacrificeGroup.SECOND_AND_SIXTEENTH)
        if (!lunar.isLeapMonth && (lunar.month to lunar.day) in FESTIVALS) add(SacrificeGroup.FESTIVALS)
    }

    /** Whether [lunar] is an offering day in any of [enabled]. */
    fun isSacrificeDay(lunar: LunarDay, enabled: Set<SacrificeGroup>): Boolean =
        enabled.isNotEmpty() && groupsOf(lunar).any { it in enabled }
}
