package org.ghostsinthelab.app.simpletungshing.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import org.ghostsinthelab.app.simpletungshing.data.MoonPhase
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

private const val LIT = 0xFFFFF1C1.toInt()
private const val SHADOW = 0x4D6E6A7A
private const val RIM = 0x998A7F66.toInt()

/**
 * Draws [phase] as seen from the northern hemisphere (waxing lit on the right).
 * The lit part is the lit-side half disc bounded by the terminator, a half
 * ellipse whose half-width is r·cos(2π·cycle).
 */
fun moonBitmap(phase: MoonPhase, sizePx: Int): Bitmap {
    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val stroke = sizePx / 32f
    val r = sizePx / 2f - stroke
    val c = sizePx / 2f
    val disc = RectF(c - r, c - r, c + r, c + r)

    paint.color = SHADOW
    canvas.drawOval(disc, paint)

    // Draw a waxing moon; a waning one is its mirror image.
    val waxingCycle = if (phase.isWaxing) phase.cycle else 1 - phase.cycle
    if (!phase.isWaxing) canvas.scale(-1f, 1f, c, c)
    val w = (r * cos(2 * PI * waxingCycle)).toFloat()
    val lit = Path().apply {
        arcTo(disc, -90f, 180f) // right limb, top to bottom
        // Terminator back to the top: bulging right for a crescent, left for gibbous.
        arcTo(RectF(c - abs(w), c - r, c + abs(w), c + r), 90f, if (w > 0) -180f else 180f)
        close()
    }
    paint.color = LIT
    canvas.drawPath(lit, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = stroke
    paint.color = RIM
    canvas.drawOval(disc, paint)
    return bitmap
}
