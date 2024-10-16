package com.selbiconsulting.elog.domain.repository

import com.selbiconsulting.elog.data.model.request.RequestLogin
import com.selbiconsulting.elog.data.model.response.ResponseLogin


/*
 * Created by shagi on 23.03.2024 22:40
 */

interface RepositoryLogin {
    suspend fun login(requestLogin: RequestLogin): ResponseLogin
    suspend fun hasAnotherDevice(requestLogin: RequestLogin): ResponseLogin
}