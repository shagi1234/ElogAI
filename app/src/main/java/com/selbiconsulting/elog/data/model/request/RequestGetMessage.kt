package com.selbiconsulting.elog.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestGetMessage(
    var contactId: String,
    var inChat: Int?=1,
    var fileId: String? = null,
)
