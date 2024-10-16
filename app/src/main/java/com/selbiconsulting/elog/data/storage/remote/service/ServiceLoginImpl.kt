package com.selbiconsulting.elog.data.storage.remote.service

import com.selbiconsulting.elog.data.model.request.RequestLogin
import com.selbiconsulting.elog.data.model.response.ResponseLogin
import com.selbiconsulting.elog.data.storage.remote.Endpoints
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType


/*
 * Created by shagi on 23.03.2024 22:33
 */

class ServiceLoginImpl(private val client: HttpClient) : ServiceLogin {
    override suspend fun login(requestLogin: RequestLogin): ResponseLogin {
        return client.post {
            url(Endpoints.shared.login)
            contentType(ContentType.Application.Json)
            body = requestLogin
        }
    }

    override suspend fun hasAnotherDevice(requestLogin: RequestLogin): ResponseLogin {
        return client.get {
            url(Endpoints.shared.device)
            contentType(ContentType.Application.Json)
            parameter("username", requestLogin.username)
            parameter("password", requestLogin.password)
        }
    }
}