package com.selbiconsulting.elog.data.storage.remote.service

import com.selbiconsulting.elog.data.model.request.RequestLocation
import com.selbiconsulting.elog.data.model.request.RequestLogin
import com.selbiconsulting.elog.data.model.response.ResponseLocation
import com.selbiconsulting.elog.data.model.response.ResponseLogin
import com.selbiconsulting.elog.data.storage.remote.KtorClient
import io.ktor.client.HttpClient


/*
 * Created by shagi on 26.03.2024 22:54
 */

interface ServiceLocation {

    suspend fun postLocation(requestLogin: List<RequestLocation>): ResponseLocation
}
