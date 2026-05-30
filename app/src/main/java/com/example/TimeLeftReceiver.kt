package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

class TimeLeftReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "time_left_daily_channel"

        // Build channel descriptor for newer SDK versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Countdown Alarm",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Night motivation alarms summarizing remainder days"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Calculate days left in year
        val calendar = Calendar.getInstance()
        val daysLeft = 365 - calendar.get(Calendar.DAY_OF_YEAR)

        // Launch Intent
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 102, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Formulate Notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // fall back on standard system clock resource vector
            .setContentTitle("Time Left Reflection")
            .setContentText("Today is over. You have $daysLeft days left in this year.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(4242, notification)
    }
}
