package com.selbiconsulting.elog.ui.util

import android.util.Log
import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.entity.calculateDurationInMinutes
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.domain.repository.RepositoryLogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class LogsHelperFunctions(private val repositoryLogs: RepositoryLogs) {

    private suspend fun getShiftStartTime(): Long {
        val currentTime = System.currentTimeMillis()
        val fourteenHoursAgo = currentTime - (14 * 60 * 60 * 1000)

        // Get logs for the last 14 hours
        val recentLogs = repositoryLogs.getLogsBetween(fourteenHoursAgo, currentTime)

        // Reverse the list to start from the most recent log
        for (i in recentLogs.indices.reversed()) {
            val log = recentLogs[i]
            if (log.status == DutyStatus.OFF_DUTY || log.status == DutyStatus.SB) {
                // Check if this off-duty or sleeper berth period lasted at least 10 hours
                if (i == 0 || getDurationBetweenLogs(
                        recentLogs[i],
                        recentLogs[i - 1]
                    ) >= 10 * 60 * 60 * 1000
                ) {
                    return log.startTime.toDateMillis()
                }
            }
        }

        // If no valid shift start found, return 14 hours ago as default
        return fourteenHoursAgo
    }

    private suspend fun getShiftDuration(): Double {
        val currentTime = System.currentTimeMillis()
        val shiftStartTime = getShiftStartTime()

        val durationMillis = currentTime - shiftStartTime

        // Convert milliseconds to hours
        return durationMillis / (1000.0 * 60 * 60)
    }

    private fun getDurationBetweenLogs(log1: EntityLog, log2: EntityLog): Long {
        return log2.startTime.toDateMillis() - log1.startTime.toDateMillis()
    }

    private fun getDrivingMinutesInShift(shiftLogs: List<EntityLog>): Double {
        val currentTime = System.currentTimeMillis()
        var totalDrivingTime = 0L

        for (i in shiftLogs.indices) {
            val log = shiftLogs[i]
            if (log.status == DutyStatus.DR) {
                val startTimeMillis = log.startTime.toDateMillis()
                val endTimeMillis = if (i == shiftLogs.lastIndex) {
                    currentTime
                } else {
                    shiftLogs[i + 1].startTime.toDateMillis()
                }
                totalDrivingTime += endTimeMillis - startTimeMillis
            }
        }

        // Convert milliseconds to minutes
        return totalDrivingTime / (1000.0 * 60)
    }

    suspend fun hasUninterruptedBreakViolation(currentShiftLogs: List<EntityLog>): Boolean {
        return getDrivingMinutesInShift(currentShiftLogs) > 11 && !hasHad10HourBreak()
    }

    private suspend fun hasHad10HourBreak(): Boolean {
        val currentTime = System.currentTimeMillis()
        val twentyFourHoursAgo = currentTime - (24 * 60 * 60 * 1000)

        // Get logs for the last 24 hours
        val recentLogs = repositoryLogs.getLogsBetween(twentyFourHoursAgo, currentTime)

        var offDutyStartTime: Long? = null
        var consecutiveOffDutyTime: Long = 0

        for (log in recentLogs) {
            when (log.status) {
                DutyStatus.OFF_DUTY, DutyStatus.SB -> {
                    if (offDutyStartTime == null) {
                        offDutyStartTime = log.startTime.toDateMillis()
                    }
                }

                else -> {
                    if (offDutyStartTime != null) {
                        consecutiveOffDutyTime = log.startTime.toDateMillis() - offDutyStartTime
                        if (consecutiveOffDutyTime >= 10 * 60 * 60 * 1000) {
                            return true
                        }
                        offDutyStartTime = null
                        consecutiveOffDutyTime = 0
                    }
                }
            }
        }

        // Check if the driver is currently in an off-duty or sleeper berth status
        if (offDutyStartTime != null) {
            consecutiveOffDutyTime = currentTime - offDutyStartTime
            if (consecutiveOffDutyTime >= 10 * 60 * 60 * 1000) {
                return true
            }
        }
        return false
    }

    suspend fun getDrivingDurationAfterBreak(): Long = withContext(Dispatchers.IO) {
        var drivingDurationAfterRest = 0L
        val drivingLOgsAfterRest = repositoryLogs.findDrivingLogsAfterLastBreak()
        Log.e("DRIVING_LOGS", "getDrivingDurationAfterRest: ${repositoryLogs.findDrivingLogsAfterLastBreak().size}", )
        drivingLOgsAfterRest.forEach { log ->
            val durationInHours = log.calculateDurationInMinutes()
            drivingDurationAfterRest += durationInHours
        }

        drivingDurationAfterRest

    }

    fun calculateWeeklyShiftHours(logs: List<EntityLog>): Double {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val now = ZonedDateTime.now()
        val startOfWeek = now.minusDays(now.dayOfWeek.value - 1L).withHour(0).withMinute(0).withSecond(0)

        return logs
            .filter { log ->
                log.status == DutyStatus.ON_DUTY || log.status == DutyStatus.DR
            }
            .filter { log ->
                ZonedDateTime.parse(log.startTime, formatter).isAfter(startOfWeek)
            }
            .sumOf { log ->
                val start = LocalDateTime.parse(log.startTime, formatter)
                val end = LocalDateTime.parse(log.endTime, formatter)
                ChronoUnit.MINUTES.between(start, end).toDouble() / 60.0 // Convert to hours
            }
    }
}