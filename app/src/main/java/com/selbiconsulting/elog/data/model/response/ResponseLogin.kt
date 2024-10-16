package com.selbiconsulting.elog.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseLogin(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("instance_url") val instanceUrl: String? = null,
    @SerialName("device_id") val deviceId: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("signature") val signature: String? = null
)