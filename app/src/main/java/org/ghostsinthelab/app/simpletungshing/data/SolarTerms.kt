package org.ghostsinthelab.app.simpletungshing.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.sin

/**
 * App-derived solar terms (節氣): the date the sun reaches a given apparent
 * ecliptic longitude, from Meeus, Astronomical Algorithms, ch. 25 (low
 * accuracy, about 0.01°, i.e. some 15 minutes). A term within minutes of
 * midnight can therefore land on the neighbouring day.
 *
 * Dates are taken in Taiwan time, matching the CWA lunar calendar.
 */
object SolarTerms {
    const val QINGMING = 15.0 // 清明
    const val XIAZHI = 90.0 // 夏至
    const val LIQIU = 135.0 // 立秋
    const val DONGZHI = 270.0 // 冬至

    /** The 24 terms in order of longitude, 0° (春分) to 345° (驚蟄). */
    private val NAMES = listOf(
        "春分", "清明", "穀雨", "立夏", "小滿", "芒種", "夏至", "小暑", "大暑", "立秋", "處暑", "白露",
        "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至", "小寒", "大寒", "立春", "雨水", "驚蟄",
    )

    private val ZONE: ZoneId = ZoneId.of("Asia/Taipei")
    private val termsByYear = ConcurrentHashMap<Int, Map<LocalDate, String>>()
    private const val TROPICAL_YEAR = 365.2422
    private const val MILLIS_PER_DAY = 86_400_000.0

    /** The name of the solar term falling on [date], if any. */
    fun termOn(date: LocalDate): String? =
        termsByYear.getOrPut(date.year) {
            NAMES.withIndex().associate { (i, name) -> dateOf(date.year, i * 15.0) to name }
        }[date]

    /** The date in [year] on which the sun reaches [longitude] degrees. */
    fun dateOf(year: Int, longitude: Double): LocalDate {
        // Start from the mean position: 0° near March 20, with the early-year terms in January to March.
        val march20 = LocalDate.of(year, 3, 20).atStartOfDay(ZONE).toInstant().toEpochMilli()
        val offsetDays = (if (longitude >= 285) longitude - 360 else longitude) / 360 * TROPICAL_YEAR
        var millis = march20 + offsetDays * MILLIS_PER_DAY
        repeat(5) {
            val behind = ((longitude - sunLongitude(millis)) % 360 + 540) % 360 - 180
            millis += behind / 360 * TROPICAL_YEAR * MILLIS_PER_DAY
        }
        return Instant.ofEpochMilli(millis.toLong()).atZone(ZONE).toLocalDate()
    }

    /** The sun's apparent geocentric longitude in degrees at [epochMillis]. */
    internal fun sunLongitude(epochMillis: Double): Double {
        // Julian centuries since J2000.0; the UT/TT difference (about a minute) is negligible here.
        val t = (epochMillis / MILLIS_PER_DAY + 2440587.5 - 2451545.0) / 36525.0
        val l0 = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
        val m = 357.52911 + 35999.05029 * t - 0.0001537 * t * t
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sinDeg(m) +
            (0.019993 - 0.000101 * t) * sinDeg(2 * m) +
            0.000289 * sinDeg(3 * m)
        val omega = 125.04 - 1934.136 * t
        val apparent = l0 + c - 0.00569 - 0.00478 * sinDeg(omega)
        return (apparent % 360 + 360) % 360
    }

    private fun sinDeg(degrees: Double): Double = sin(degrees * PI / 180)
}
