package org.ghostsinthelab.app.simpletungshing.data

/**
 * Days on which Taiwanese households and shops customarily make offerings (拜拜): 初一 and
 * 十五 for the gods and ancestors, 初二 and 十六 for 做牙 (土地公), and the three festivals
 * 端午, 中秋 and 重陽. App-derived from the lunar date, not CWA data. The monthly 初一/十五
 * and 做牙 days count in leap months too; the festivals only in regular months.
 */
object SacrificeDays {
    private val MONTHLY_DAYS = setOf(1, 2, 15, 16)
    private val FESTIVALS = setOf(5 to 5, 8 to 15, 9 to 9)

    fun isSacrificeDay(lunar: LunarDay): Boolean =
        lunar.day in MONTHLY_DAYS || (!lunar.isLeapMonth && (lunar.month to lunar.day) in FESTIVALS)
}
