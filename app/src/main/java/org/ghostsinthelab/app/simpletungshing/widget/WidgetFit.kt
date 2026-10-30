package org.ghostsinthelab.app.simpletungshing.widget

import kotlin.math.min

/**
 * One text line of the widget as the fit calculation sees it: its base font size in sp
 * and its text, whose width is estimated from the characters.
 */
data class WidgetLine(val text: String, val sizeSp: Float, val bold: Boolean = false) {
    /** Estimated width in dp at scale 1 and font scale 1: an em for Han script, about half for Latin. */
    val widthDp: Float
        get() = text.sumOf { c -> if (c.code >= 0x2E80) 1.0 else 0.55 }.toFloat() * sizeSp
}

/**
 * The layout chosen for a widget size: which lines show, how much the base font sizes
 * are shrunk, and whether the lines must scroll instead.
 *
 * Glance cannot measure text before it lays out, and a launcher may give the widget
 * nearly any size, so the fit is estimated. [scale] shrinks every font size and the
 * moon a little, down to [MIN_SCALE]; below that, lines are dropped from the bottom (沖煞
 * and 干支 first, with the day number stepping down to [COMPACT_NUMBER_SP], then the
 * festival line). When even the date lines alone cannot fit, for a tiny widget or a
 * large system font, [scroll] is set and the widget shows every line in a list the user
 * scrolls, at the floor size, rather than clipping or shrinking the text further.
 */
data class WidgetLayout(
    val scale: Float,
    /** Font size of the day number in sp before [scale]: [WidgetFit.NUMBER_SP], or smaller once 干支 is dropped. */
    val numberSp: Float,
    val showObservance: Boolean,
    val showGanZhi: Boolean,
    val scroll: Boolean,
    /** Whether the festival line is allowed to wrap onto a second line. */
    val observanceWraps: Boolean,
)

/** Fits the widget's text lines into the room it has. Pure, so it is unit-tested on the JVM. */
object WidgetFit {
    /** Font sizes in sp at scale 1. */
    const val HEADER_SP = 13f
    const val NUMBER_SP = 52f
    const val COMPACT_NUMBER_SP = 40f
    const val LUNAR_SP = 16f
    const val OBSERVANCE_SP = 13f
    const val GANZHI_SP = 13f
    const val MOON_DP = 32f

    /**
     * Smallest shrink before lines are dropped. It applies to the scale alone, never to the
     * system font scale: a user who enlarged their font did so to read, and the widget
     * must not quietly undo that. Lines are dropped, wrapped or scrolled instead.
     */
    const val MIN_SCALE = 0.85f

    /**
     * Height of a text line relative to its font size, measured on a device: Han script in
     * Noto Sans CJK takes about 1.45 em per line, and the large digits about 1.32 em.
     */
    private const val LINE_HEIGHT = 1.45f
    private const val NUMBER_LINE_HEIGHT = 1.32f

    /** Space Glance's Column leaves around the lines, in dp, that no scale recovers. */
    private const val SLACK_DP = 4f

    /**
     * Chooses the layout for a content area of [widthDp] × [heightDp] (after margin and
     * padding), at the system [fontScale]. [header], [lunar] and [observance] are the texts
     * actually drawn; [observance] is null when the day has no festival or 節氣.
     */
    fun fit(
        widthDp: Float,
        heightDp: Float,
        fontScale: Float,
        header: WidgetLine,
        lunar: WidgetLine,
        observance: WidgetLine?,
        ganZhi: List<WidgetLine>,
        showMoon: Boolean,
    ): WidgetLayout {
        val tiers = listOf(
            Tier(observance = observance != null, ganZhi = true),
            Tier(observance = observance != null, ganZhi = false),
            Tier(observance = false, ganZhi = false),
        ).distinct()
        for (tier in tiers) {
            // Try the festival on one line, then wrapped onto two when it is too wide.
            for (wrap in listOf(false, true)) {
                if (wrap && !tier.observance) continue
                val lines = buildList {
                    add(header)
                    if (tier.observance) add(observance!!)
                    if (tier.ganZhi) addAll(ganZhi)
                }
                val widest = (lines + lunar).maxOf { line ->
                    if (wrap && line === observance) line.widthDp / 2 else line.widthDp
                }
                val numberSp = if (tier.ganZhi) NUMBER_SP else COMPACT_NUMBER_SP
                val numberHeight = maxOf(numberSp * NUMBER_LINE_HEIGHT, if (showMoon) MOON_DP else 0f)
                val height = SLACK_DP + fontScale * (
                    numberHeight +
                        lines.sumOf { (it.sizeSp * LINE_HEIGHT).toDouble() }.toFloat() +
                        lunar.sizeSp * LINE_HEIGHT +
                        (if (wrap) OBSERVANCE_SP * LINE_HEIGHT else 0f)
                    )
                val scale = min(1f, min(heightDp / height, widthDp / (widest * fontScale)))
                if (scale >= MIN_SCALE) {
                    return WidgetLayout(scale, numberSp, tier.observance, tier.ganZhi, scroll = false, observanceWraps = wrap)
                }
            }
        }
        return WidgetLayout(MIN_SCALE, COMPACT_NUMBER_SP, observance != null, true, scroll = true, observanceWraps = true)
    }

    private data class Tier(val observance: Boolean, val ganZhi: Boolean)
}
