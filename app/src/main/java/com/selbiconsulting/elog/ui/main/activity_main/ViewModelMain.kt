package com.selbiconsulting.elog.ui.main.activity_main

import android.app.Notification
import android.app.UiModeManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.selbiconsulting.elog.data.model.enums.SharedPrefsKeys
import com.selbiconsulting.elog.domain.use_case.UseCaseGetVarFromSharedPrefs
import com.selbiconsulting.elog.domain.use_case.UseCaseSetAppTheme
import com.selbiconsulting.elog.ui.util.AlarmScheduler
import com.selbiconsulting.elog.ui.util.AlarmSchedulerImpl
import com.selbiconsulting.elog.ui.util.NotificationData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ViewModelMain @Inject constructor(
    private val useCaseSetAppTheme: UseCaseSetAppTheme,
    private val uiModeManager: UiModeManager,
    private val alarmScheduler: AlarmScheduler,
    private val useCaseGetVarFromSharedPrefs: UseCaseGetVarFromSharedPrefs

) : ViewModel() {
    val isKeepScreenEnabled: MutableLiveData<Boolean> = MutableLiveData(
        useCaseGetVarFromSharedPrefs.getVariable(SharedPrefsKeys.KEEP_SCREEN_MODE, false)
    )


    fun checkScheduledAlarms() {
        alarmScheduler.checkScheduledAlarms()
    }

    fun scheduleNotifications(notification: NotificationData) {
        val currentTime = Calendar.getInstance()
        val scheduledTime = (currentTime.clone() as Calendar).apply {
            add(Calendar.HOUR_OF_DAY, parseHour(notification.time))
            add(Calendar.MINUTE, parseMinute(notification.time))
            set(Calendar.SECOND, 0)
        }

        println("TAG_check_notifications Current time: ${currentTime.time}")
        println(" TAG_check_notifications Alarm scheduled at: ${scheduledTime.time}")
    }

    private fun parseHour(timeStr: String): Int {
        val parts = timeStr.split(":")
        return parts[0].toInt()
    }

    private fun parseMinute(timeStr: String): Int {
        val parts = timeStr.split(":")
        return parts[1].toInt()
    }

    private fun parseDate(dateStr: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(dateStr)
        return date?.time ?: 0L
    }

    fun cancelAllNotification() {
        alarmScheduler.cancelAll()
    }

}