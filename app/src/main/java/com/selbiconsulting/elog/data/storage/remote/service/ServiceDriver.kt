package com.selbiconsulting.elog.data.storage.remote.service

import android.util.Log
import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.model.request.RequestUpdateDriver
import com.selbiconsulting.elog.data.storage.remote.KtorClient
import io.ktor.client.HttpClient

interface ServiceDriver {

    suspend fun getDriverInfo(contactId: String, deviceId: String): EntityDriver
    suspend fun updateDriver(requestUpdateDriver: RequestUpdateDriver): String
}