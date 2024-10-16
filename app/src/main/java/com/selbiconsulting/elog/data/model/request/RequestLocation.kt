package com.selbiconsulting.elog.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestLocation(
    @SerialName("contactId") val contactC: String,
    @SerialName("name") val name: String,
    @SerialName("locationType") val locationType: String,
    @SerialName("latitude") val latitude: String,
    @SerialName("longitude") val longitude: String,
    @SerialName("degree") val degree: String,
    @SerialName("speed") val speed: String,
    @SerialName("createdAt") val createdAt: String
)
