package com.selbiconsulting.elog.data.model.response

import com.selbiconsulting.elog.data.model.dto.GetMessage
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ResponseGetMessage(
    @Contextual
    var messages: List<GetMessage>,
    var lastReadDate: String? = null,
    var isLogEdited: Boolean? = false,
    var isPcAllowed: Boolean? = false
)
