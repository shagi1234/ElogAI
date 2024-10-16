package com.selbiconsulting.elog.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseCreateDvir(
    var createdDate: String? = null,
    var id: String? = null,
    var localId: String? = null,
)