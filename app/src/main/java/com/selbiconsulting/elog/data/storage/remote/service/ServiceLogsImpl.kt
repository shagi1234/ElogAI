package com.selbiconsulting.elog.data.storage.remote.service

import android.devicelock.DeviceId
import android.util.Log
import com.selbiconsulting.elog.data.model.dto.DtoLogs
import com.selbiconsulting.elog.data.model.request.RequestSendLogs
import com.selbiconsulting.elog.data.model.response.ResponseSendLogs
import com.selbiconsulting.elog.data.storage.remote.Endpoints
import com.selbiconsulting.elog.data.storage.remote.KtorClient
import com.selbiconsulting.elog.domain.use_case.RequestCertifyLogs
import com.selbiconsulting.elog.domain.use_case.ResponseCertifyLogs
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ServiceLogsImpl(
    private val tokenProvider: () -> String
) : ServiceLogs {

    private var client = KtorClient().getInstanceWithToken(tokenProvider())
    fun refreshClient() {
        client = KtorClient().getInstanceWithToken(tokenProvider())
    }

    override suspend fun sendLogsToServer(requestSendLogs: List<RequestSendLogs>): List<ResponseSendLogs> {
        return client.post {
            url(Endpoints.shared.logs)
            contentType(ContentType.Application.Json)
            body = requestSendLogs
        }
    }

    override suspend fun getLogsFromServer(contactId: String, deviceId: String): List<DtoLogs> {
        return client.get {
            url(Endpoints.shared.logs)
            contentType(ContentType.Application.Json)
            parameter("contactId", contactId)
            parameter("deviceId", deviceId)
        }
    }

    override suspend fun certifyLogsToServer(requestCertifyLogs: RequestCertifyLogs): ResponseCertifyLogs {
        return client.post {
            url(Endpoints.shared.logs)
            contentType(ContentType.Application.Json)
            body = requestCertifyLogs
        }
    }

}