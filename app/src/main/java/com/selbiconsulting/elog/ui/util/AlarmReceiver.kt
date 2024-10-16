package com.selbiconsulting.elog.ui.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.di.App
import com.selbiconsulting.elog.ui.main.activity_main.ActivityMain
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra("notification_title") ?: return
        val time = intent.getStringExtra("notification_time") ?: return

        val content = intent.getStringExtra("notification_content") ?: ""

        Log.e("TAG_check_notifications not work", "onReceive: " + time)
        if (!isCurrentTimeMatching(time) /*||  !isTotalNotificationSwitched(context, type)*/) return

        context?.let { ctx ->
            showNotification(ctx, title, content)
        }

    }

    private fun isCurrentTimeMatching(compareTime: String): Boolean {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE)

        val parsedTime = try {
            SimpleDateFormat("hh:mm", Locale.getDefault()).parse(compareTime)
        } catch (e: Exception) {
            null
        }

        val compareCalendar = Calendar.getInstance()
        parsedTime?.let { compareCalendar.time = it }

        val compareValue =
            compareCalendar.get(Calendar.HOUR_OF_DAY) * 100 + compareCalendar.get(Calendar.MINUTE)

        return currentTime == compareValue
    }

    private fun showNotification(context: Context, title: String, content: String) {
        val builder = NotificationCompat.Builder(context, App.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Create an explicit intent for an activity in your app
        val notificationIntent = Intent(context, ActivityMain::class.java)

        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        notificationIntent.putExtra("workout_title", title)

        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        builder.setContentIntent(pendingIntent)

        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            notify(title.hashCode(), builder.build())
        }
    }
}