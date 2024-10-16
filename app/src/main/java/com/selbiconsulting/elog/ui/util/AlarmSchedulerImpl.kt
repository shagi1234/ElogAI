package com.selbiconsulting.elog.ui.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class AlarmSchedulerImpl(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val sharedPreferences = context.getSharedPreferences("pending_intents", Context.MODE_PRIVATE)

    @SuppressLint("ScheduleExactAlarm")
    override fun schedule(
        notification: NotificationData,
        calendar: Calendar
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("notification_title", notification.title)
            putExtra("notification_content", notification.content)
            putExtra("notification_time", notification.time)
        }

        val requestCode = notification.hashCode()
        storePendingIntent(requestCode)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    override fun cancel(notification: NotificationData) {
        val requestCode = notification.hashCode()
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        removePendingIntent(requestCode)
    }

    override fun cancelAll() {
        if (Build.VERSION.SDK_INT >= 34) {
            alarmManager.cancelAll()
        } else {
            getAllPendingIntents().forEach { pendingIntent ->
                alarmManager.cancel(pendingIntent)
            }
        }
        clearAllStoredPendingIntents()
    }

    override fun checkScheduledAlarms() {
        val pendingIntents = getAllPendingIntents()

        if (pendingIntents.isEmpty()) {
            println("TAG_check_notifications No scheduled alarms.")
        } else {
            println("TAG_check_notifications Scheduled alarms:")
            pendingIntents.forEach { pendingIntent ->
                println(pendingIntent.toString())
            }
        }
    }

    private fun storePendingIntent(requestCode: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(requestCode.toString(), requestCode)
        editor.apply()
    }

    private fun removePendingIntent(requestCode: Int) {
        val editor = sharedPreferences.edit()
        editor.remove(requestCode.toString())
        editor.apply()
    }

    private fun getAllPendingIntents(): List<PendingIntent> {
        val pendingIntents = mutableListOf<PendingIntent>()
        sharedPreferences.all.forEach { entry ->
            val requestCode = entry.value as Int
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntents.add(pendingIntent)
        }
        return pendingIntents
    }

    private fun clearAllStoredPendingIntents() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
