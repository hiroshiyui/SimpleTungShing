package org.ghostsinthelab.app.simpletungshing.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [WidgetFit] against the sizes the reviewer of F-Droid MR 54384 saw clip, and the extremes. */
class WidgetFitTest {
    private val header = WidgetLine("2026年10月 星期三", WidgetFit.HEADER_SP)
    private val lunar = WidgetLine("農曆九月(大)十九", WidgetFit.LUNAR_SP, bold = true)
    private val observance = WidgetLine("觀音出家日", WidgetFit.OBSERVANCE_SP, bold = true)
    private val ganZhi = listOf(
        WidgetLine("丙午年（馬）乙亥日", WidgetFit.GANZHI_SP),
        WidgetLine("沖蛇(己巳) 煞西", WidgetFit.GANZHI_SP),
    )

    private fun fit(width: Float, height: Float, fontScale: Float = 1f, withObservance: Boolean = true) =
        WidgetFit.fit(width, height, fontScale, header, lunar, observance.takeIf { withObservance }, ganZhi, showMoon = true)

    /** The 180 dp preview at 12 dp padding: everything shows, shrunk a little on a festival day. */
    @Test
    fun previewFestivalDayKeepsEveryLine() {
        val layout = fit(156f, 156f)
        assertFalse(layout.scroll)
        assertTrue(layout.showGanZhi)
        assertTrue(layout.showObservance)
        assertEquals(WidgetFit.NUMBER_SP, layout.numberSp)
        assertTrue(layout.scale in WidgetFit.MIN_SCALE..1f)
    }

    /** Without a festival the same size needs no shrinking. */
    @Test
    fun previewPlainDayIsFullSize() {
        assertEquals(1f, fit(156f, 156f, withObservance = false).scale)
    }

    /** A short 2×2 cell: the 干支 lines go before the festival does. */
    @Test
    fun shortWidgetDropsGanZhiFirst() {
        val layout = fit(156f, 100f)
        assertFalse(layout.scroll)
        assertFalse(layout.showGanZhi)
        assertTrue(layout.showObservance)
    }

    /** Smaller still: only the date lines with the compact day number, still readable. */
    @Test
    fun tinyWidgetKeepsDateLines() {
        val layout = fit(120f, 75f)
        assertFalse(layout.scroll)
        assertFalse(layout.showGanZhi)
        assertFalse(layout.showObservance)
        assertEquals(WidgetFit.COMPACT_NUMBER_SP, layout.numberSp)
        assertTrue(layout.scale >= WidgetFit.MIN_SCALE)
    }

    /** Nothing readable fits: every line is kept and the widget scrolls instead. */
    @Test
    fun hopelessSizeScrolls() {
        val layout = fit(90f, 50f)
        assertTrue(layout.scroll)
        assertTrue(layout.showGanZhi)
        assertTrue(layout.showObservance)
        assertEquals(WidgetFit.MIN_SCALE, layout.scale, 0.001f)
    }

    /** A large system font counts: the preview size that fits at 1.0 drops lines at 1.5. */
    @Test
    fun fontScaleShrinksOrDrops() {
        val normal = fit(156f, 156f, fontScale = 1f)
        val large = fit(156f, 156f, fontScale = 1.5f)
        assertTrue(large.scale < normal.scale || !large.showGanZhi)
    }

    /** At font scale 2.0 the preview size still fits the date lines without scrolling: the floor is on the effective size. */
    @Test
    fun hugeFontScaleShrinksInsteadOfScrolling() {
        val layout = fit(156f, 156f, fontScale = 2f)
        assertFalse(layout.scroll)
        assertTrue(layout.scale * 2f >= WidgetFit.MIN_SCALE)
    }

    /** A long festival line wraps onto two lines before the width forces a shrink below the floor. */
    @Test
    fun longObservanceWraps() {
        val long = WidgetLine("霜降、觀音出家日、重陽節、寒衣節", WidgetFit.OBSERVANCE_SP, bold = true)
        val layout = WidgetFit.fit(130f, 200f, 1f, header, lunar, long, ganZhi, showMoon = true)
        assertFalse(layout.scroll)
        assertTrue(layout.showObservance)
        assertTrue(layout.observanceWraps)
    }

    /** Han characters count as one em, Latin as about half. */
    @Test
    fun widthEstimate() {
        assertEquals(26f, WidgetLine("農曆", 13f).widthDp, 0.01f)
        assertEquals(13f * 0.55f * 2, WidgetLine("ab", 13f).widthDp, 0.01f)
    }
}
