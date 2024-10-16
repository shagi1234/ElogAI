package com.selbiconsulting.elog.data.storage.remote.service

import com.selbiconsulting.elog.data.model.request.RequestGetMessage
import com.selbiconsulting.elog.data.model.request.RequestSendMessage
import com.selbiconsulting.elog.data.model.response.ResponseGetMessage
import com.selbiconsulting.elog.data.model.response.ResponseSendMessage
import com.selbiconsulting.elog.data.storage.remote.Endpoints
import com.selbiconsulting.elog.data.storage.remote.KtorClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ServiceMessageImpl(
    private val tokenProvider: () -> String
) : ServiceMessage {
    private var client = KtorClient().getInstanceWithToken(tokenProvider())
    fun refreshClient() {
        client = KtorClient().getInstanceWithToken(tokenProvider())
    }
    override suspend fun sendMessage(responseSendMessage: RequestSendMessage): ResponseSendMessage {
        return client.post {
            url(Endpoints.shared.sendMsg)
            contentType(ContentType.Application.Json)
            body = responseSendMessage
        }
    }

    override suspend fun getMessage(requestGetMessage: RequestGetMessage, deviceId:String): ResponseGetMessage {
        return client.get {
            url(Endpoints.shared.getMsg)
            contentType(ContentType.Application.Json)
            parameter("contactId", requestGetMessage.contactId)
            parameter("deviceId",  deviceId)
            parameter("inChat", requestGetMessage.inChat)
        }
    }

    override suspend fun getFileUrl(requestGetMessage: RequestGetMessage, deviceId:String): ByteArray {
        return client.get {
            url(Endpoints.shared.file)
            contentType(ContentType.Application.Json)
            parameter("contactId", requestGetMessage.contactId)
            parameter("fileId", requestGetMessage.fileId)
            parameter("deviceId",  deviceId)


        }

    }
}