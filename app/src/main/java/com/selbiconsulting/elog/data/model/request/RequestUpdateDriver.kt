package com.selbiconsulting.elog.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class RequestUpdateDriver(
    var contactId: String,
    var ducumentId: String?,
    var trailerId: String?,
    var note: String?
)