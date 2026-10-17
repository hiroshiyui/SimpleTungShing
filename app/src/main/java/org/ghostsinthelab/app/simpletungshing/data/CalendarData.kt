package org.ghostsinthelab.app.simpletungshing.data

import android.content.Context
import java.io.Reader
import java.time.LocalDate

/** One row of the CWA "日曆資料-國農曆對照" dataset (A-A0087-001). */
data class LunarDay(
    val date: LocalDate,
    val rocYear: Int,
    /** Sexagenary name of the lunar year, e.g. "丙午". */
    val yearGanZhi: String,
    val month: Int,
    val isLeapMonth: Boolean,
    val day: Int,
    /** 29 or 30, or null when the month is cut off at either end of the dataset. */
    val monthLength: Int?,
)

/**
 * Solar-to-lunar lookup table backed by the CWA open data CSV bundled in assets.
 * Rows are contiguous by date, so a lookup is an index offset from the first day.
 */
class CalendarData private constructor(private val days: List<LunarDay>) {

    val firstDate: LocalDate get() = days.first().date
    val lastDate: LocalDate get() = days.last().date

    fun lookup(date: LocalDate): LunarDay? {
        val index = date.toEpochDay() - firstDate.toEpochDay()
        return if (index in days.indices) days[index.toInt()] else null
    }

    companion object {
        const val ASSET_NAME = "A-A0087-001.csv"

        @Volatile
        private var instance: CalendarData? = null

        /** Loads (once) the bundled dataset. Does blocking I/O; call off the main thread. */
        fun get(context: Context): CalendarData =
            instance ?: synchronized(this) {
                instance ?: context.applicationContext.assets.open(ASSET_NAME)
                    .bufferedReader()
                    .use { parse(it) }
                    .also { instance = it }
            }

        /** Parses CSV columns: 日期,民國年,農曆年,農曆月,農曆日,星期 (leap months as "閏N"). */
        fun parse(reader: Reader): CalendarData {
            val rows = reader.readLines()
                .drop(1) // header (carries the UTF-8 BOM)
                .mapNotNull { line ->
                    val fields = line.trim().split(',')
                    if (fields.size < 5 || fields[0].isBlank()) return@mapNotNull null
                    val month = fields[3]
                    LunarDay(
                        date = LocalDate.parse(fields[0]),
                        rocYear = fields[1].toInt(),
                        yearGanZhi = fields[2],
                        month = month.removePrefix("閏").toInt(),
                        isLeapMonth = month.startsWith("閏"),
                        day = fields[4].toInt(),
                        monthLength = null,
                    )
                }
            require(rows.isNotEmpty()) { "Empty calendar data" }
            rows.zipWithNext().forEach { (a, b) ->
                require(b.date == a.date.plusDays(1)) { "Calendar data not contiguous at ${a.date}" }
            }
            return CalendarData(withMonthLengths(rows))
        }

        private fun withMonthLengths(rows: List<LunarDay>): List<LunarDay> {
            val result = ArrayList<LunarDay>(rows.size)
            var start = 0
            while (start < rows.size) {
                var end = start
                while (end + 1 < rows.size && rows[end + 1].day != 1) end++
                // Only a month seen from its 初一 up to the next month's 初一 has a known length.
                val length = if (rows[start].day == 1 && end + 1 < rows.size) rows[end].day else null
                for (i in start..end) result += rows[i].copy(monthLength = length)
                start = end + 1
            }
            return result
        }
    }
}
