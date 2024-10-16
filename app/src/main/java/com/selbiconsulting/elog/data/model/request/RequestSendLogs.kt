package com.selbiconsulting.elog.data.model.request

import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import kotlinx.serialization.Serializable

@Serializable
data class RequestSendLogs(
    var localId: String,
    var logId: String = "",
    var signatureId: String = "signatureId",
    var isVerified: Boolean,
    var vehicle: String?,
    var contactId: String,
    var status: String,
    var document: String,
    var startTime: String,
    var endTime: String,
    var trailer: String,
    var note: String,
    var odometer: String? = null,
    var engineHours: String? = null,
    var createdDate: String,
    var violationTypes: String?,
    var isDrivingTimeResetLog: Boolean = false,
    var location: String
) {



    private fun checkDutyStatus(status: String?): DutyStatus {
        return when (status) {
            "ON" -> DutyStatus.ON_DUTY
            "OFF" -> DutyStatus.OFF_DUTY
            "SB" -> DutyStatus.SB
            "DR" -> DutyStatus.DR
            "YM" -> DutyStatus.YM
            "PC" -> DutyStatus.PC
            else -> DutyStatus.OFF_DUTY
        }
    }
}