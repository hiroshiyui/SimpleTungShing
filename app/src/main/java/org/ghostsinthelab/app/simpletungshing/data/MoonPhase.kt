package org.ghostsinthelab.app.simpletungshing.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/** How the app derives the moon phase; neither comes from CWA data. */
enum class MoonMethod {
    /** From the lunar day of the CWA calendar: 初一 is the new-moon day. */
    LUNAR_DAY,

    /** From the moon's computed elongation at local noon (Meeus, low precision). */
    ASTRONOMICAL,
}

/**
 * A point in the synodic cycle: [cycle] is 0 at new moon, 0.5 at full moon, and
 * approaches 1 just before the next new moon.
 */
data class MoonPhase(val cycle: Double) {

    /** Illuminated fraction of the disc, 0..1. */
    val illumination: Double get() = (1 - cos(2 * PI * cycle)) / 2

    val isWaxing: Boolean get() = cycle < 0.5

    /** One of eight traditional phase names, each spanning an eighth of the cycle. */
    val name: String get() = NAMES[(floor(cycle * 8 + 0.5).toInt()) % 8]

    companion object {
        private const val SYNODIC_MONTH = 29.530588853
        private val NAMES = listOf("新月", "眉月", "上弦月", "盈凸月", "滿月", "虧凸月", "下弦月", "殘月")

        /** Lunar day 1 (初一) is new moon; each day advances by one day of the mean synodic month. */
        fun fromLunarDay(day: Int): MoonPhase = MoonPhase(((day - 1) / SYNODIC_MONTH).coerceIn(0.0, 0.999))

        /**
         * Phase at local noon of [date], from the moon's phase angle (Meeus,
         * Astronomical Algorithms, ch. 48 low-precision formula). Accurate to well
         * under a day, which is plenty for an icon.
         */
        fun astronomical(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): MoonPhase {
            val instant = date.atTime(LocalTime.NOON).atZone(zone).toInstant()
            // Julian centuries since J2000.0 (2000-01-01 12:00 TT; the UT/TT difference is negligible here).
            val t = (instant.epochSecond / 86400.0 + 2440587.5 - 2451545.0) / 36525.0
            val d = normalize(297.8501921 + 445267.1114034 * t) // mean elongation of the moon
            val m = normalize(357.5291092 + 35999.0502909 * t) // sun's mean anomaly
            val mp = normalize(134.9633964 + 477198.8675055 * t) // moon's mean anomaly
            val phaseAngle = 180 - d -
                6.289 * sinDeg(mp) +
                2.100 * sinDeg(m) -
                1.274 * sinDeg(2 * d - mp) -
                0.658 * sinDeg(2 * d) -
                0.214 * sinDeg(2 * mp) -
                0.110 * sinDeg(d)
            // Phase angle 180° is new moon and 0° is full; the true elongation is 180° minus it.
            return MoonPhase(normalize(180 - phaseAngle) / 360)
        }

        /** Phase by [method]; null when [LUNAR_DAY][MoonMethod.LUNAR_DAY] is chosen but [lunar] is outside the dataset. */
        fun of(method: MoonMethod, date: LocalDate, lunar: LunarDay?): MoonPhase? = when (method) {
            MoonMethod.LUNAR_DAY -> lunar?.let { fromLunarDay(it.day) }
            MoonMethod.ASTRONOMICAL -> astronomical(date)
        }

        private fun normalize(degrees: Double): Double = ((degrees % 360) + 360) % 360

        private fun sinDeg(degrees: Double): Double = sin(degrees * PI / 180)
    }
}
