package org.ghostsinthelab.app.simpletungshing.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class TungShingWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = TungShingWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        scheduleMidnightRefresh(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        context.getSystemService(AlarmManager::class.java).cancel(midnightIntent(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MIDNIGHT,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            // Also sent for a per-app language change. Glance would handle it with a plain
            // update(), which leaves a running session stale, and would take goAsync() first.
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                scheduleMidnightRefresh(context)
                val pending = goAsync()
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        TungShingWidget.refreshAll(context)
                    } finally {
                        pending.finish()
                    }
                }
            }
            else -> super.onReceive(context, intent)
        }
    }

    companion object {
        private const val ACTION_MIDNIGHT = "org.ghostsinthelab.app.simpletungshing.action.MIDNIGHT"

        /** Window allowed without the exact-alarm permission (minimum enforced since API 31). */
        private const val WINDOW_MILLIS = 10 * 60 * 1000L

        private fun midnightIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, TungShingWidgetReceiver::class.java).setAction(ACTION_MIDNIGHT),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        /**
         * Non-wakeup alarm just after the next local midnight: if the device is asleep,
         * it fires on wake, which is when the widget can next be seen anyway.
         */
        fun scheduleMidnightRefresh(context: Context) {
            val nextMidnight = LocalDate.now().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            context.getSystemService(AlarmManager::class.java)
                .setWindow(AlarmManager.RTC, nextMidnight + 1000, WINDOW_MILLIS, midnightIntent(context))
        }
    }
}
