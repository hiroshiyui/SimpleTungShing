package org.ghostsinthelab.app.simpletungshing

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.ghostsinthelab.app.simpletungshing.data.CalendarData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Accessibility of the month grid: each day cell is one node that names the date and its
 * lunar day or 節氣, is clickable, and reports whether it is selected.
 */
@RunWith(AndroidJUnit4::class)
class MonthCardTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun dayCellIsOneNamedSelectableNode() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val calendar = CalendarData.get(context)
        val selected = LocalDate.of(2026, 10, 28)
        compose.setContent { MonthCard(selected, calendar, onSelect = {}) }
        val pattern = context.getString(R.string.day_cell_pattern)
        val locale = context.resources.configuration.locales[0]
        fun describe(date: LocalDate, lunar: String) =
            "${date.format(java.time.format.DateTimeFormatter.ofPattern(pattern, locale))}, $lunar"
        compose.onNodeWithContentDescription(describe(selected, "農曆十九"))
            .assertHasClickAction()
            .assertIsSelected()
        compose.onNodeWithContentDescription(describe(LocalDate.of(2026, 10, 8), "寒露"))
            .assertHasClickAction()
            .assertIsNotSelected()
    }
}
