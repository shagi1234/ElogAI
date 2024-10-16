package com.selbiconsulting.elog.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseSendLogs(
    var createdDate: String? = null,
    var logId: String? = null,
    var localId: String?
)
