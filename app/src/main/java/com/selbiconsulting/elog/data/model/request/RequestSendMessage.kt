package com.selbiconsulting.elog.data.model.request

import com.selbiconsulting.elog.data.model.dto.DtoFile
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class RequestSendMessage (
    var contactId: String,
    var message: String,
    var messageType: String,
    var fileSize: Long,
   @Contextual var file: DtoFile?
)