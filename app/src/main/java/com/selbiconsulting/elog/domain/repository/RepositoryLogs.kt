package com.selbiconsulting.elog.domain.repository

import com.selbiconsulting.elog.data.model.dto.DtoLogs
import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.model.request.RequestSendLogs
import com.selbiconsulting.elog.data.model.response.ResponseSendLogs
import com.selbiconsulting.elog.domain.use_case.RequestCertifyLogs
import com.selbiconsulting.elog.domain.use_case.ResponseCertifyLogs
import kotlinx.coroutines.flow.Flow


/*
 * Created by shagi on 03.04.2024 23:55
 */

interface RepositoryLogs {
    fun getAllLogs(): Flow<List<EntityLog>>
    suspend fun getLogsFromServer(contactId: String): List<DtoLogs>
    suspend fun certifyDailyLogsServer(requestCertifyLogs: RequestCertifyLogs): ResponseCertifyLogs
    suspend fun sendLogsToServer(requestSendLogs: List<RequestSendLogs>): List<ResponseSendLogs>
    suspend fun addLog(log: EntityLog): Long
    suspend fun getLogById(logId: Long): EntityLog?
    suspend fun upsertLogs(logs: List<EntityLog>)
    suspend fun deleteLog(log: EntityLog)
    suspend fun deleteAllLog()
    suspend fun updateLog(log: EntityLog)
    fun getLastByStatus(status: DutyStatus): Flow<EntityLog?>
    suspend fun addLogs(logs: List<EntityLog>)
    fun getDailyLog(date: String): Flow<List<EntityLog>>
    suspend fun getNotSentLogs(): List<EntityLog>
    fun getLogsAfterReset(): Flow<List<EntityLog>>
    suspend fun checkDayLogsAllVerified(date: String): Boolean
    suspend fun certifyDailyLogs(date: String)

    fun getDurationAfterIsDrivingResetTrue(): Flow<String>

    fun findPTILogs(): List<EntityLog>
    fun findDrivingLogsAfterLastBreak(): List<EntityLog>
   suspend fun getLogsBetween(startTime: Long, endTime: Long): List<EntityLog>
   suspend fun calculateWeeklyShiftHours(): Double
   suspend fun checkResetCycleLog():Boolean


}