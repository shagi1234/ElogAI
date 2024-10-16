package com.selbiconsulting.elog.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import kotlinx.coroutines.flow.Flow

/*
 * Created by shagi on 03.04.2024 23:52
 */

@Dao
interface DaoLogs : DaoBase<EntityLog> {
    @Query(
        "SELECT * " +
                "FROM logs " +
                "ORDER BY createdAt ASC "
    )
    fun getAllLogs(): Flow<List<EntityLog>>

    @Query(
        """
        SELECT * FROM logs 
        ORDER BY createdAt DESC 
        LIMIT 7
    """
    )
    fun getLast7Logs(): List<EntityLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: EntityLog)

    @Query(
        "SELECT * " +
                "FROM logs " +
                "WHERE localId = :localId " +
                "ORDER BY createdAt ASC " +
                "LIMIT 1"
    )
    fun getLogByLogId(localId: Long): EntityLog?


    @Query("SELECT * FROM logs WHERE isDrivingTimeResetLog = 'true' ORDER BY startTime DESC LIMIT 1")
    suspend fun getMostRecentResetLog(): EntityLog?

    @Query("SELECT * FROM logs WHERE status = :status AND startTime > :resetStartTime")
    fun getDrivingLogsAfterReset(status: DutyStatus, resetStartTime: String): Flow<List<EntityLog>>

    @Query(
        "SELECT *" +
                " FROM logs WHERE status = :status" +
                " ORDER BY createdAt ASC" +
                " LIMIT 1"
    )
    fun getLastByStatus(status: DutyStatus): Flow<EntityLog?>

    @Query("DELETE FROM logs")
    suspend fun deleteAllLogs()

    @Transaction
    suspend fun upsert(logs: List<EntityLog>) {
        for (log in logs) {
            val existingMessage = getLogByRemoteId(log.id ?: "")

            if (existingMessage != null) {
                log.localId = existingMessage.localId
            }

            insertOrUpdate(log)
        }
    }

    @Query("SELECT * FROM logs WHERE id = :id LIMIT 1")
    suspend fun getLogByRemoteId(id: String): EntityLog?


    @Query(
        """
        SELECT * 
        FROM logs 
        WHERE datetime(startTime) >= (
            SELECT datetime(endTime) 
            FROM logs 
            WHERE status = 'RESET' 
            ORDER BY datetime(endTime) DESC 
            LIMIT 1
        )
        ORDER BY datetime(startTime) ASC
    """
    )
    fun getLogsAfterLastReset(): Flow<List<EntityLog>>

    @Query(
        """
    SELECT * FROM logs 
    WHERE (
        datetime(startTime, :offsetStr) < datetime(:date || ' 23:59:59')
        AND datetime(endTime, :offsetStr) > datetime(:date || ' 00:00:00')
    )
    ORDER BY datetime(startTime, :offsetStr) ASC
"""
    )
    fun getDailyLogs(date: String, offsetStr: String): Flow<List<EntityLog>>

    @Query(
        "UPDATE logs SET isVerified = 'true' WHERE substr(createdAt, 1, 10) = :date"
    )
    suspend fun certifyDailyLogs(date: String)

    @Query(
        "SELECT COUNT(*) FROM logs WHERE substr(createdAt, 1, 10) = :date AND isVerified != 'true'"
    )
    suspend fun countUnverifiedLogsForDate(date: String): Int

    suspend fun checkDayLogsAllVerified(date: String): Boolean {
        return countUnverifiedLogsForDate(date) == 0
    }

//    @Query("SELECT * FROM logs WHERE note = :noteText")
//    fun findLogsByNote(noteText: String): List<EntityLog>

    @Query(
        """
    SELECT * FROM logs 
    WHERE note = :noteText 
    AND datetime(startTime) >= (
        SELECT datetime(endTime) 
        FROM logs 
        WHERE status = 'RESET' 
        ORDER BY datetime(endTime) DESC 
        LIMIT 1
    )
    ORDER BY datetime(startTime) ASC
"""
    )
    fun findLogsByNote(noteText: String): List<EntityLog>

    @Query(
        """
        WITH last_reset AS (
            SELECT MAX(startTime) as reset_time
            FROM logs
            WHERE isDrivingTimeResetLog = 'true'
        )
        SELECT * FROM logs
        WHERE status = 'DR'
        AND (
            (SELECT reset_time FROM last_reset) IS NULL
            OR datetime(startTime) > (SELECT datetime(reset_time) FROM last_reset)
        )
        ORDER BY datetime(startTime) ASC
    """
    )
    fun findDrivingLogsAfterLastBreak(): List<EntityLog>


    @Query("SELECT * FROM logs WHERE startTime BETWEEN :startTime AND :endTime ORDER BY startTime ASC")
    suspend fun getLogsBetween(startTime: Long, endTime: Long): List<EntityLog>


    @Query("SELECT * FROM logs WHERE status = :resetStatus AND note = 'cycle' ORDER BY startTime DESC LIMIT 1")
    suspend fun getLastCycleLog(resetStatus: DutyStatus = DutyStatus.RESET): EntityLog?

    @Query("SELECT * FROM logs WHERE startTime > :startTime AND status IN (:statuses) ORDER BY startTime ASC")
    suspend fun getLogsAfterTime(startTime: String, statuses: List<DutyStatus>): List<EntityLog>


    @Query("SELECT * FROM logs WHERE isSent = 0 OR isUpdated = 0")
    suspend fun getNotSentLogs(): List<EntityLog>

}