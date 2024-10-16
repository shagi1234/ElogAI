package com.selbiconsulting.elog.data.model.enums

import kotlinx.serialization.Serializable


/*
 * Created by shagi on 29.02.2024 22:40
 */

@Serializable
enum class DutyStatus {
    ON_DUTY,
    OFF_DUTY,
    SB,
    DR,
    YM,
    PC,
    RESET;

    companion object {
        fun getDutyStatusFromString(status: String?): DutyStatus {
            return when (status?.uppercase()) {
                "ON_DUTY" -> ON_DUTY
                "OFF_DUTY" -> OFF_DUTY
                "SB" -> SB
                "DR" -> DR
                "YM" -> YM
                "PC" -> PC
                "RESET" -> RESET
                else -> ON_DUTY
            }
        }
    }
}

fun getShortDutyStatus(status: DutyStatus): String {
    return when (status) {
        DutyStatus.ON_DUTY -> "ON"
        DutyStatus.OFF_DUTY -> "OFF"
        DutyStatus.SB -> "SB"
        DutyStatus.YM -> "YM"
        DutyStatus.DR -> "DR"
        DutyStatus.PC -> "PC"
        DutyStatus.RESET -> "RESET"
    }
}