package com.selbiconsulting.elog.data.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class MessageType(val value: String) {
    IMAGE("image"),
    TEXT("text"),
    FILE("file"),
    ALARM("alarm"),
    VOICE("voice")
}