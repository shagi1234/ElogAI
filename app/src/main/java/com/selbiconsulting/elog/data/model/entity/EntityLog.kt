package com.selbiconsulting.elog.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selbiconsulting.elog.data.model.dto.DtoLogs
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Serializable
@Entity(tableName = "logs")
data class EntityLog(
    @PrimaryKey(autoGenerate = true)
    var localId: Long = 0,
    var id: String? = null,
    var status: DutyStatus,
    var document: String? = null,
    var startTime: String = "",//yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
    var endTime: String = "",
    var trailer: String? = null,
    var vehicle: String? = null,
    var note: String? = "",
    var odometer: String? = null,
    var engineHours: String? = null,
    var location: String? = "N/A",
    var createdAt: String = "",//yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
    var isVerified: String = "false",
    var isSent: Int = 0,
    var isUpdated: Int = 0,
    var signatureId: String = "",
    var violationTypes: String = "",
    var isDrivingTimeResetLog: String? = "false",
)

fun EntityLog.calculateDurationInMinutes(): Long {
    if (startTime.isEmpty() || endTime.isEmpty()) {
        return 0L
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val start = LocalDateTime.parse(startTime, formatter)
    val end = LocalDateTime.parse(endTime, formatter)
    val duration = Duration.between(start, end)
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()

    return hours * 60 + minutes
}

