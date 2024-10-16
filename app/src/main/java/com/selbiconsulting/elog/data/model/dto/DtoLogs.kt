package com.selbiconsulting.elog.data.model.dto

import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import kotlinx.serialization.Serializable

@Serializable
data class DtoLogs(
    val status: String?,
    val document: String?,
    val endTime: String?,
    val startTime: String?,
    val vehicle: String?,
    val trailer: String?,
    val note: String?,
    val location: String?,
    val createdDate: String?,
    val contactId: String?,
    val logId: String,
    val signatureId: String? = "",
    var localId: Long = 0,
    var odometer: String? = "0.0",
    var engineHours: String? = null,
    var isVerified: String? = "false",
    var violationTypes: String =" ",
    var isDrivingTimeResetLog: String? = "false",
    var isSent: Int? = 1,
    var isUpdated: Int? = 1,
) {

    companion object {
        private fun DtoLogs.toEntityLog(): EntityLog {
            return EntityLog(
                localId = this.localId,
                status = checkDutyStatus(this.status),
                startTime = this.startTime ?: "",
                endTime = this.endTime ?: "",
                trailer = this.trailer ?: "",
                signatureId = this.signatureId ?: "",
                vehicle = this.vehicle ?: "",
                note = this.note,
                id = this.logId,
                location = this.location ?: "",
                createdAt = this.createdDate ?: "",
                document = this.document ?: "",
                odometer = this.odometer ?: "",
                engineHours = this.engineHours ?: "",
                isVerified = this.isVerified ?: "",
                isDrivingTimeResetLog = this.isDrivingTimeResetLog ?: "false",
                violationTypes = this.violationTypes ?: "",
                isSent = this.isSent ?: 1,
                isUpdated = this.isUpdated ?: 1,
            )
        }

        fun List<DtoLogs>.toListOfEntityLog(): List<EntityLog> {
            return this.map { dtoLog ->
                dtoLog.toEntityLog()
            }
        }

        private fun checkDutyStatus(status: String?): DutyStatus {
            return when (status) {
                "ON" -> DutyStatus.ON_DUTY
                "OFF" -> DutyStatus.OFF_DUTY
                "SB" -> DutyStatus.SB
                "DR" -> DutyStatus.DR
                "YM" -> DutyStatus.YM
                "PC" -> DutyStatus.PC
                "RESET" -> DutyStatus.RESET
                else -> DutyStatus.OFF_DUTY
            }
        }
    }

}
