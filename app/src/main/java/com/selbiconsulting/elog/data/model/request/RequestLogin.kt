package com.selbiconsulting.elog.data.model.request

import android.devicelock.DeviceId
import kotlinx.serialization.Serializable

@Serializable
data class RequestLogin(
    val username: String,
    val password: String,
    val deviceId: String? = "",
    val deviceToken: String? = "",
)