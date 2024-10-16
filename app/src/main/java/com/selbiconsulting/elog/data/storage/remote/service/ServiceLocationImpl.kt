package com.selbiconsulting.elog.data.storage.remote.service

import com.selbiconsulting.elog.data.model.request.RequestLocation
import com.selbiconsulting.elog.data.model.request.RequestLogin
import com.selbiconsulting.elog.data.model.response.ResponseLocation
import com.selbiconsulting.elog.data.model.response.ResponseLogin
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.data.storage.remote.Endpoints
import com.selbiconsulting.elog.data.storage.remote.KtorClient
import com.selbiconsulting.elog.domain.use_case.UseCaseGetUserInfo
import com.selbiconsulting.elog.domain.use_case.UseCaseSaveUserInfo
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import javax.inject.Inject


/*
 * Created by shagi on 26.03.2024 22:56
 */

class ServiceLocationImpl @Inject constructor(
    private val tokenProvider: () -> String
) : ServiceLocation {
    private var client = KtorClient().getInstanceWithToken(tokenProvider())
    fun refreshClient() {
        client = KtorClient().getInstanceWithToken(tokenProvider())
    }

    override suspend fun postLocation(requestLogin: List<RequestLocation>): ResponseLocation {
        return client.post {
            url(Endpoints.shared.location)
            contentType(ContentType.Application.Json)
            body = requestLogin
        }
    }

}