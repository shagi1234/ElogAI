package com.selbiconsulting.elog.ui.util

import java.util.Calendar

interface AlarmScheduler {
    fun schedule(notification: NotificationData, calendar: Calendar)
    fun cancel(notification: NotificationData)
    fun checkScheduledAlarms()
    fun cancelAll()
}

