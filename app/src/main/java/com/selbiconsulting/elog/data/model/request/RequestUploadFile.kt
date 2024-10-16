package com.selbiconsulting.elog.data.model.request

import com.selbiconsulting.elog.data.model.dto.DtoFile
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class RequestUploadFile(
    var contactId: String? = null,
    @Contextual var file: DtoFile? = null,
)