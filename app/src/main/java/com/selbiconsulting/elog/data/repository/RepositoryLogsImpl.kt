package com.selbiconsulting.elog.data.repository

import android.util.Log
import com.selbiconsulting.elog.data.model.dto.DtoLogs
import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.model.request.RequestSendLogs
import com.selbiconsulting.elog.data.model.response.ResponseSendLogs
import com.selbiconsulting.elog.data.storage.local.AppDatabase
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.data.storage.remote.service.ServiceLogs
import com.selbiconsulting.elog.domain.repository.RepositoryLogs
import com.selbiconsulting.elog.domain.use_case.RequestCertifyLogs
import com.selbiconsulting.elog.domain.use_case.ResponseCertifyLogs
import com.selbiconsulting.elog.ui.util.Const
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject


/*
 * Created by shagi on 04.04.2024 00:24
 */

class RepositoryLogsImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val serviceLogs: ServiceLogs,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : RepositoryLogs {
    override fun getAllLogs(): Flow<List<EntityLog>> {
        return appDatabase.logsDao().getAllLogs()
    }

    override suspend fun getLogsFromServer(contactId: String): List<DtoLogs> {
        return serviceLogs.getLogsFromServer(contactId, sharedPreferencesHelper.deviceID.toString())
    }

    override suspend fun certifyDailyLogsServer(requestCertifyLogs: RequestCertifyLogs): ResponseCertifyLogs {
        return serviceLogs.certifyLogsToServer(requestCertifyLogs)
    }

    override suspend fun sendLogsToServer(requestSendLogs: List<RequestSendLogs>): List<ResponseSendLogs> {
        return serviceLogs.sendLogsToServer(requestSendLogs)
    }

    override suspend fun addLog(log: EntityLog): Long {
        return appDatabase.logsDao().insert(log)
    }

    override suspend fun getLogById(logId: Long): EntityLog? {
        return appDatabase.logsDao().getLogByLogId(logId)
    }

    override suspend fun deleteLog(log: EntityLog) {
        appDatabase.logsDao().delete(log)
    }

    override suspend fun deleteAllLog() {
        appDatabase.logsDao().deleteAllLogs()
    }

    override suspend fun updateLog(log: EntityLog) {
        appDatabase.logsDao().update(log)
    }

    override suspend fun upsertLogs(logs: List<EntityLog>) {
        appDatabase.logsDao().upsert(logs)
    }

    override fun getDurationAfterIsDrivingResetTrue(): Flow<String> = flow {
        val resetLog = appDatabase.logsDao().getMostRecentResetLog()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        if (resetLog != null) {
            val resetStartTime = resetLog.startTime
            var totalDuration = Duration.ZERO

            appDatabase.logsDao().getDrivingLogsAfterReset(DutyStatus.DR, resetStartTime)
                .collect { logs ->
                    logs.forEach { log ->
                        val startTime = LocalDateTime.parse(log.startTime, dateTimeFormatter)
                        val endTime = LocalDateTime.parse(log.endTime, dateTimeFormatter)
                        totalDuration = totalDuration.plus(Duration.between(startTime, endTime))
                    }

                    // Convert total duration to hours and minutes
                    val hours = totalDuration.toHours()
                    val minutes = totalDuration.minusHours(hours).toMinutes()

                    // Format the duration as "HH:mm"
                    val formattedDuration = String.format("%02d:%02d", hours, minutes)
                    emit(formattedDuration)
                }
        } else {
            // No reset log found, emit "00:00"
            emit("00:00")
        }
    }

    override fun findPTILogs(): List<EntityLog> {
        return appDatabase.logsDao().findLogsByNote(Const.notePTI)
    }

    override fun findDrivingLogsAfterLastBreak(): List<EntityLog> {
        return appDatabase.logsDao().findDrivingLogsAfterLastBreak()
    }

    override suspend fun getLogsBetween(startTime: Long, endTime: Long): List<EntityLog> {
        return appDatabase.logsDao().getLogsBetween(startTime, endTime)
    }


    override fun getLastByStatus(status: DutyStatus): Flow<EntityLog?> {
        return appDatabase.logsDao().getLastByStatus(status)
    }

    override suspend fun addLogs(logs: List<EntityLog>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                appDatabase.logsDao().insert(logs)
            } catch (e: Exception) {
                Log.e("Error inserting logs", "error: $e")
            }
        }

    }

    override fun getDailyLog(date: String): Flow<List<EntityLog>> {
        val offsetStr = getOffsetString(date)
        return appDatabase.logsDao().getDailyLogs(date, offsetStr)
    }

    override suspend fun getNotSentLogs(): List<EntityLog> {
        return appDatabase.logsDao().getNotSentLogs()
    }

    override fun getLogsAfterReset(): Flow<List<EntityLog>> {
        return appDatabase.logsDao().getLogsAfterLastReset()
    }

    private fun getOffsetString(dateStr: String): String {
        try {
            // Parse the input date string
            val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)

            // Get the current timezone offset
            val zoneOffset = ZoneId.systemDefault().rules.getOffset(date.atStartOfDay())

            // Format the timezone offset for SQLite
            val offsetHours = zoneOffset.totalSeconds / 3600
            val offsetMinutes = (zoneOffset.totalSeconds % 3600) / 60
            return String.format(Locale.US, "%+03d:%02d", offsetHours, offsetMinutes)
        } catch (e: Exception) {
            // Handle parsing exceptions or invalid date format
            e.printStackTrace()
            return ""
        }
    }

    override suspend fun checkDayLogsAllVerified(date: String): Boolean {
        return appDatabase.logsDao().checkDayLogsAllVerified(date)
    }

    override suspend fun certifyDailyLogs(date: String) {
        return appDatabase.logsDao().certifyDailyLogs(date)
    }

    override suspend fun calculateWeeklyShiftHours(): Double {
        val lastResetLog = appDatabase.logsDao().getLastCycleLog() ?: return 0.0


        val relevantStatuses = listOf(DutyStatus.DR, DutyStatus.ON_DUTY, DutyStatus.YM)
        val logsAfterReset =
            appDatabase.logsDao().getLogsAfterTime(lastResetLog.startTime, relevantStatuses)

        return calculateTotalDuration(logsAfterReset)
    }

    override suspend fun checkResetCycleLog(): Boolean {
        val last7Logs = appDatabase.logsDao().getLast7Logs()
        val resetLogs = last7Logs.filter { it.status == DutyStatus.RESET }
        val offDutyLogs = last7Logs.filter { it.status == DutyStatus.OFF_DUTY }

        if (resetLogs.size != 3 || offDutyLogs.size != 4) {
            return false
        }

        // Check if the last OFF_DUTY log duration is more than 4 hours
        val lastOffDutyLog = offDutyLogs.first()
        val lastOffDutyDuration = calculateDuration(lastOffDutyLog.startTime, lastOffDutyLog.endTime)

        return (resetLogs.size == 3 && offDutyLogs.size == 4 && lastOffDutyDuration >= 4)
    }

    private fun calculateTotalDuration(logs: List<EntityLog>): Double {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        return logs.sumOf { log ->
            val start = LocalDateTime.parse(log.startTime, formatter)
            val end = LocalDateTime.parse(log.endTime, formatter)
            ChronoUnit.MINUTES.between(start, end).toDouble() / 60.0 // Convert to hours
        }
    }

    private fun calculateDuration(startTime: String, endTime: String): Double {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val start = LocalDateTime.parse(startTime, formatter)
        val end = LocalDateTime.parse(endTime, formatter)

        return ChronoUnit.MINUTES.between(start, end).toDouble() / 60.0 // Convert to hours
    }
}