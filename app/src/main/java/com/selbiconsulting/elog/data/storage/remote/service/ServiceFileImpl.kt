package com.selbiconsulting.elog.data.storage.remote.service

import com.selbiconsulting.elog.data.model.request.RequestGetMessage
import com.selbiconsulting.elog.data.model.request.RequestUploadFile
import com.selbiconsulting.elog.data.model.response.ResponseUploadFile
import com.selbiconsulting.elog.data.storage.remote.Endpoints
import com.selbiconsulting.elog.data.storage.remote.KtorClient
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ServiceFileImpl(
    private val tokenProvider: () -> String
) : ServiceFile {
    private val endpoints = Endpoints.shared

    private var client = KtorClient().getInstanceWithToken(tokenProvider())
    fun refreshClient() {
        client = KtorClient().getInstanceWithToken(tokenProvider())
    }
    override suspend fun getFileUrl(requestGetMessage: RequestGetMessage): ByteArray {
        return ByteArray(0)
    }

    override suspend fun uploadFile(requestUploadFile: RequestUploadFile): ResponseUploadFile {
        return client.post {
            url(endpoints.file)
            contentType(ContentType.Application.Json)
            body = requestUploadFile
        }
    }
}