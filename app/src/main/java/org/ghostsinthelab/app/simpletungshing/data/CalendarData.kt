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

    /** Past the end of the CWA data: the app and widget then stop and only say so. */
    fun hasRunOut(today: LocalDate): Boolean = today > lastDate

    /** The lunar date of [date], or null outside [firstDate]..[lastDate]; callers must show an out-of-range message then. */
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
            // Hand-rolled rather than split()/LocalDate.parse(): this runs on every cold widget
            // render, and the formatter-based version took well over a second on a phone.
            val lines = reader.readLines()
            val rocYears = IntArray(lines.size)
            val yearNames = arrayOfNulls<String>(lines.size)
            val months = IntArray(lines.size)
            val leaps = BooleanArray(lines.size)
            val days = IntArray(lines.size)
            val names = HashMap<String, String>()
            var firstDate: LocalDate? = null
            var count = 0
            for (line in lines.subList(1, lines.size)) { // the header carries the UTF-8 BOM
                if (line.isBlank() || line[0] == ',') continue // trailing ",,,,," row
                val c1 = line.indexOf(',')
                val c2 = line.indexOf(',', c1 + 1)
                val c3 = line.indexOf(',', c2 + 1)
                val c4 = line.indexOf(',', c3 + 1)
                require(c1 == 10 && c4 > c3) { "Malformed calendar row: $line" }
                val date = LocalDate.of(line.int(0, 4), line.int(5, 7), line.int(8, 10))
                if (firstDate == null) firstDate = date
                require(date == firstDate.plusDays(count.toLong())) { "Calendar data not contiguous at $date" }
                rocYears[count] = line.int(c1 + 1, c2)
                yearNames[count] = line.substring(c2 + 1, c3).let { names.getOrPut(it) { it } }
                val leap = line.startsWith("閏", c3 + 1)
                leaps[count] = leap
                months[count] = line.int(if (leap) c3 + 2 else c3 + 1, c4)
                val c5 = line.indexOf(',', c4 + 1).let { if (it < 0) line.length else it }
                days[count] = line.int(c4 + 1, c5)
                count++
            }
            require(firstDate != null) { "Empty calendar data" }

            // A lunar month's length is known only when both its 初一 and the next month's 初一 are present.
            val lengths = arrayOfNulls<Int>(count)
            var start = 0
            while (start < count) {
                var end = start
                while (end + 1 < count && days[end + 1] != 1) end++
                val length = if (days[start] == 1 && end + 1 < count) days[end] else null
                for (i in start..end) lengths[i] = length
                start = end + 1
            }
            return CalendarData(
                List(count) { i ->
                    LunarDay(
                        date = firstDate.plusDays(i.toLong()),
                        rocYear = rocYears[i],
                        yearGanZhi = yearNames[i]!!,
                        month = months[i],
                        isLeapMonth = leaps[i],
                        day = days[i],
                        monthLength = lengths[i],
                    )
                },
            )
        }

        private fun String.int(from: Int, to: Int): Int {
            var value = 0
            for (i in from until to) value = value * 10 + (this[i] - '0')
            return value
        }
    }
}
