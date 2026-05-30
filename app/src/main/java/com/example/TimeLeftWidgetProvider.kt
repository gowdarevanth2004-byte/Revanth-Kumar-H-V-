package com.example

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import java.util.*
import kotlin.math.max

class TimeLeftWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Force update when launcher triggers or settings are altered
        if (intent.action == "com.example.timeleft.UPDATE_WIDGET") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, TimeLeftWidgetProvider::class.java)
            val allIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, allIds)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Calculate countdowns
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val min = calendar.get(Calendar.MINUTE)
            val sec = calendar.get(Calendar.SECOND)

            val currentDaySecs = hour * 3600 + min * 60 + sec
            val totalDaySecs = 24 * 3600
            val secondsLeft = max(0, totalDaySecs - currentDaySecs)

            val hh = secondsLeft / 3600
            val mm = (secondsLeft % 3600) / 60

            val daysLeftYear = 365 - calendar.get(Calendar.DAY_OF_YEAR)
            val currentYear = calendar.get(Calendar.YEAR)

            // Extract Shared Preferences
            val sharedPref = context.getSharedPreferences("TimeLeftPrefs", Context.MODE_PRIVATE)
            val customMotto = sharedPref.getString("user_motto", "MAKE EVERY SECOND COUNT.") ?: "MAKE EVERY SECOND COUNT."
            val themeId = sharedPref.getString("theme_id", "neon_matrix") ?: "neon_matrix"

            // Theme visual adaptation mappings
            val primaryColorStr = when (themeId) {
                "cyberpunk" -> "#FF007F"
                "aurora" -> "#A855F7"
                "oled" -> "#F59E0B"
                else -> "#00FFCC"
            }
            val primaryColor = Color.parseColor(primaryColorStr)

            // Apply calculated data fields
            views.setTextViewText(R.id.widget_time_left, "${hh}h ${mm}m")
            views.setTextViewText(R.id.widget_year_left, "$daysLeftYear Days Left in $currentYear")
            views.setTextViewText(R.id.widget_motto, customMotto.uppercase())

            // Adapt text color to match the selected premium theme
            views.setTextColor(R.id.widget_time_left, primaryColor)
            views.setTextColor(R.id.widget_motto, primaryColor)

            // Setup Launch Intent: clicking the widget opens the main application
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_time_left, pendingIntent)

            // Commit views
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
