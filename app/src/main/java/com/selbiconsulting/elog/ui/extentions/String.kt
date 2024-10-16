package com.selbiconsulting.elog.ui.extentions

import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun String.divide(divider: Long): String {
    val parts = this.split(":")
    val hours = parts[0].toLong()
    val minutes = parts.getOrElse(1) { "0" }.toLong()

    // Convert total time to minutes
    val totalMinutes = hours * 60 + minutes

    // Divide total minutes
    val dividedMinutes = totalMinutes / divider

    // Calculate hours and minutes from divided minutes
    val resultHours = dividedMinutes / 60
    val resultMinutes = dividedMinutes % 60

    return String.format("%02d:%02d", resultHours, resultMinutes)
}