package com.selbiconsulting.elog.data.storage.remote.service

import android.util.Log
import com.selbiconsulting.elog.data.model.request.RequestCreateDvir
import com.selbiconsulting.elog.data.model.response.ResponseCreateDvir
import com.selbiconsulting.elog.data.storage.remote.Endpoints
import com.selbiconsulting.elog.data.storage.remote.KtorClient
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonElement

class ServiceDvirImpl(
    private val tokenProvider: () -> String
) : ServiceDvir {
    private val endpoints = Endpoints.shared

    private var client = KtorClient().getInstanceWithToken(tokenProvider())
    fun refreshClient() {
        client = KtorClient().getInstanceWithToken(tokenProvider())
    }

    override suspend fun createDvir(requestCreateDvir: RequestCreateDvir): List<ResponseCreateDvir> {
        val arrayRequestBody = listOf(requestCreateDvir)
        return client.post {
            url(endpoints.dvir)
            contentType(ContentType.Application.Json)
            body = arrayRequestBody
        }
    }

    override suspend fun getAllDvir(contactId: String, deviceId:String): List<RequestCreateDvir> {
        return client.get {
            url(endpoints.dvir)
            contentType(ContentType.Application.Json)
            parameter("contactId", contactId)
            parameter("deviceId", deviceId)

        }
    }

    override suspend fun deleteDvir(requestCreateDvir: RequestCreateDvir): JsonElement {
        return client.delete {
            url(endpoints.dvir)
            contentType(ContentType.Application.Json)
            parameter("contactId", requestCreateDvir.contactId)
            parameter("id", requestCreateDvir.id)
        }
    }
}