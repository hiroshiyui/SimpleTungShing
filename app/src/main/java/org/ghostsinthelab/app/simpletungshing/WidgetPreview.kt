package org.ghostsinthelab.app.simpletungshing

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.preferences.core.emptyPreferences
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.appwidget.runComposition
import org.ghostsinthelab.app.simpletungshing.widget.TungShingWidget
import org.ghostsinthelab.app.simpletungshing.widget.TungShingWidgetReceiver

/** About a 2×2-cell widget, used until one is placed. */
private val DEFAULT_SIZE = DpSize(180.dp, 180.dp)

/**
 * The real widget, rendered off-screen by Glance and inflated here. Its composition collects
 * [org.ghostsinthelab.app.simpletungshing.widget.WidgetSettings] itself, so it re-emits on
 * every saved change.
 */
@OptIn(ExperimentalGlanceApi::class)
@Composable
fun WidgetPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val portrait = LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE
    val size = remember(portrait) { placedSize(context, portrait) ?: DEFAULT_SIZE }
    val views by produceState<RemoteViews?>(null, size) {
        // Empty state rather than loading it for the fake preview id, which would leave a
        // state file behind.
        TungShingWidget()
            .runComposition(context, sizes = listOf(size), state = emptyPreferences())
            .collect { value = it }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { NoTouchFrameLayout(it) },
            update = { frame ->
                frame.removeAllViews()
                views?.let { frame.addView(it.apply(frame.context, frame)) }
            },
            modifier = Modifier.size(size),
        )
    }
}

/**
 * The size of the first placed widget as the launcher reports it, picked per orientation the
 * way Glance does for [androidx.glance.appwidget.SizeMode.Exact]: narrowest width and tallest
 * height in portrait, the reverse in landscape.
 */
private fun placedSize(context: Context, portrait: Boolean): DpSize? {
    val manager = AppWidgetManager.getInstance(context)
    val id = manager.getAppWidgetIds(ComponentName(context, TungShingWidgetReceiver::class.java))
        .firstOrNull() ?: return null
    val options = manager.getAppWidgetOptions(id)
    val width = options.getInt(
        if (portrait) AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH else AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
    )
    val height = options.getInt(
        if (portrait) AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
    )
    return if (width > 0 && height > 0) DpSize(width.dp, height.dp) else null
}

/** Swallows taps so the widget's own click action (open the app) does not fire in the preview. */
@SuppressLint("ViewConstructor")
private class NoTouchFrameLayout(context: Context) : FrameLayout(context) {
    override fun onInterceptTouchEvent(ev: MotionEvent) = true
}
