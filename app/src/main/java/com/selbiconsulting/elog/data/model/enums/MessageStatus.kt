package com.selbiconsulting.elog.data.model.enums

import kotlinx.serialization.Serializable


@Serializable
enum class MessageStatus(val value: String) {
    ERROR("error"),
    SENDING("sending"),
    SENT("sent"),
    READ("read")
}