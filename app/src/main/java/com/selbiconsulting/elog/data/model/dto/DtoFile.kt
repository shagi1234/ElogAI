package com.selbiconsulting.elog.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class DtoFile(
    val data: String,
    val filename: String,
    val contentType: String,
    val fileSize: Int
)