package com.selbiconsulting.elog.ui.main.home.adapter

import android.os.Parcelable
import android.provider.ContactsContract.Data
import com.selbiconsulting.elog.data.model.dto.DtoLogs
import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.model.enums.getShortDutyStatus
import com.selbiconsulting.elog.data.model.request.RequestSendLogs
import kotlinx.parcelize.Parcelize


@Parcelize
data class DataLog(
    var statusShort: DutyStatus = DutyStatus.OFF_DUTY,
    var timeStart: String? = null,
    var localId: Long? = 0,
    var id: String? = null,
    var timeSpent: String? = null,
    var timeRemaining: String? = null,
    var totalTime: String = "7:20",
    var location: String? = "USA,New York",
    var endTime: String? = null,
    var trailer: String? = null,
    var vehicle: String? = null,
    var createdAt: String? = null,//yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
    var document: String? = null,
    var odometer: String? = null,
    var engineHours: String? = null,
    var isVerified: Boolean = false,//yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
    var note: String? = null,
    var isSent: Int = 0,
    var isUpdated: Int = 0,
    var signatureId: String? = "",
    var violationTypes: String= "",
    var isDrivingTimeResetLog: String? = "false",
) : Parcelable {

    companion object {
        fun DataLog.toRequestSendLog(contactId: String): RequestSendLogs {
            return RequestSendLogs(
                localId = this.localId.toString(),
                contactId = contactId,
                signatureId = "testSignature",
                isVerified = false,
                status = getShortDutyStatus(this.statusShort),
                document = this.document ?: "",
                startTime = this.timeStart ?: "",
                endTime = this.endTime ?: "",
                trailer = this.trailer ?: "",
                note = this.note ?: "Test",
                location = this.location ?: "",
                createdDate = this.createdAt ?: "",
                vehicle = this.vehicle,
                odometer = this.odometer,
                engineHours = this.engineHours,
                isDrivingTimeResetLog = this.isDrivingTimeResetLog.toBoolean(),
                violationTypes = this.violationTypes
            )
        }

        fun DataLog.toEntityLog(): EntityLog {
            return EntityLog(
                localId = this.localId ?: 0,
                status = this.statusShort,
                startTime = this.timeStart ?: "",
                endTime = this.endTime ?: "",
                trailer = this.trailer,
                note = this.note,
                id = this.id,
                location = this.location,
                createdAt = this.createdAt ?: "",
                document = this.document,
                vehicle = this.vehicle,
                odometer = this.odometer,
                engineHours = this.engineHours,
                isVerified = this.isVerified.toString(),
                signatureId = this.signatureId ?: "",
                isDrivingTimeResetLog = this.isDrivingTimeResetLog,
                violationTypes = this.violationTypes,
                isSent = this.isSent,
                isUpdated = this.isUpdated,
            )
        }

        fun EntityLog.toDataLog(): DataLog {
            return DataLog(
                statusShort = this.status,
                timeStart = this.startTime,
                localId = this.localId,
                endTime = this.endTime,
                trailer = this.trailer,
                note = this.note ?: "",
                id = this.id ?: "",
                location = this.location,
                createdAt = this.createdAt,
                isVerified = this.isVerified == "true",
                vehicle = this.vehicle,
                odometer = this.odometer,
                engineHours = this.engineHours,
                isDrivingTimeResetLog = this.isDrivingTimeResetLog,
                violationTypes = this.violationTypes,
                isSent = this.isSent,
                isUpdated = this.isUpdated,
            )
        }

        fun DtoLogs.toDataLog(): DataLog {
            return DataLog(
                statusShort = DutyStatus.getDutyStatusFromString(this.status),
                timeStart = this.startTime,
                localId = this.localId,
                endTime = this.endTime,
                trailer = this.trailer,
                note = this.note ?: "",
                id = this.logId,
                location = this.location,
                createdAt = this.createdDate,
                isVerified = this.isVerified == "true",
                vehicle = this.vehicle,
                odometer = this.odometer,
                engineHours = this.engineHours,
                isDrivingTimeResetLog = this.isDrivingTimeResetLog,
                violationTypes = this.violationTypes,
                isSent = this.isSent ?: 1,
                isUpdated = this.isUpdated ?: 1,
            )
        }

        fun List<EntityLog>.fromEntityLogToDataLogs(): List<DataLog> {
            return map {
                it.toDataLog()
            }
        }
        fun List<DataLog>.toEntityLogs(): List<EntityLog> {
            return map {
                it.toEntityLog()
            }

        }
        fun List<DataLog>.toRequestSendLogs(contactId: String): List<RequestSendLogs> {
            return map {
                it.toRequestSendLog(contactId)
            }

        }
        fun List<DtoLogs>.toDataLogs(): List<DataLog> {
            return map {
                it.toDataLog()
            }
        }
    }
}

fun String.toArrayList(): ArrayList<String> {
    return ArrayList(split(",").map { it.trim() })
}

fun List<String>.toCommaSeparatedString(): String {
    return this.filter { it.isNotBlank() }.joinToString(", ")
}