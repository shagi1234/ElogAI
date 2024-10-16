package com.selbiconsulting.elog.data.storage.remote.service

import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.model.request.RequestUpdateDriver
import com.selbiconsulting.elog.data.storage.remote.Endpoints
import com.selbiconsulting.elog.data.storage.remote.KtorClient
import com.selbiconsulting.elog.data.storage.remote.KtorClientProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class ServiceDriverImpl @Inject constructor(
    private val tokenProvider: () -> String
) : ServiceDriver {
    private var client = KtorClient().getInstanceWithToken(tokenProvider())
    fun refreshClient() {
        client = KtorClient().getInstanceWithToken(tokenProvider())
    }

    override suspend fun getDriverInfo(contactId: String, deviceId: String): EntityDriver {
        return client.get {
            url(Endpoints.shared.driverInfo)
            contentType(ContentType.Application.Json)
            parameter("contactId", contactId)
            parameter("deviceId", deviceId)
        }
    }

    override suspend fun updateDriver(requestUpdateDriver: RequestUpdateDriver): String {
        return client.post {
            url(Endpoints.shared.driverInfo)
            contentType(ContentType.Application.Json)
            body = requestUpdateDriver
        }
    }
}