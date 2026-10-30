package org.ghostsinthelab.app.simpletungshing.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Re-renders the widgets after the system font size changes. Android gives widget
 * providers no broadcast for that: the launcher re-inflates the last layout at the new
 * font scale, which overflows the fit computed for the old one. So this periodic job, at
 * WorkManager's 15-minute minimum, compares the current font scale with the one recorded
 * at the last render ([noteRendered]) and calls [TungShingWidget.refreshAll] on a change.
 * It costs one comparison per run and needs no wakeup or permission.
 */
class FontScaleWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        if (lastRendered(context) != context.resources.configuration.fontScale) {
            TungShingWidget.refreshAll(context)
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "font-scale-check"
        private const val PREFS = "widget_refresh"
        private const val KEY_FONT_SCALE = "font_scale"

        /** Records the font scale a render used, so the worker can tell when it changes. */
        fun noteRendered(context: Context) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putFloat(KEY_FONT_SCALE, context.resources.configuration.fontScale)
                .apply()
        }

        private fun lastRendered(context: Context): Float =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat(KEY_FONT_SCALE, 1f)

        /** Starts the periodic check if it is not already scheduled. */
        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<FontScaleWorker>(15, TimeUnit.MINUTES).build(),
            )
        }

        /** Stops the check once the last widget is removed. */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
