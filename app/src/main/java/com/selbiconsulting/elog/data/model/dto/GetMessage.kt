package com.selbiconsulting.elog.data.model.dto

import com.selbiconsulting.elog.data.model.enums.MessageType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class GetMessage(
    var id: String,
    var sended: String,
    var message: String,
    var messageType: String,
    var fileSize: Int,
    var createdDate: String,
    var messageStatus: String? = null
)
