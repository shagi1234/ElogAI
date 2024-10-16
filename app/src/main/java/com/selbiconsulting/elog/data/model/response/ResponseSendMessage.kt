package com.selbiconsulting.elog.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseSendMessage(
    var id: String,
    var fileId: String?,
    var createdDate: String
)
