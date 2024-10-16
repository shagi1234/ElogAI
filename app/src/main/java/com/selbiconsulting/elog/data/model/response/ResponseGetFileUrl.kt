package com.selbiconsulting.elog.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseGetFileUrl(
    var title: String? = null,
    var createdDate: String? = null,
    var id: String? = null,
)