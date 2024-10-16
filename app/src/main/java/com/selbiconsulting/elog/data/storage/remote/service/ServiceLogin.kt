package com.selbiconsulting.elog.data.storage.remote.service

import com.selbiconsulting.elog.data.model.request.RequestLogin
import com.selbiconsulting.elog.data.model.response.ResponseLogin
import com.selbiconsulting.elog.data.storage.remote.KtorClient


/*
 * Created by shagi on 23.03.2024 22:32
 */

interface ServiceLogin {
    companion object {
        fun create(): ServiceLogin {
            return ServiceLoginImpl(
                client = KtorClient().getInstance
            )
        }
    }

    suspend fun login(requestLogin: RequestLogin): ResponseLogin
    suspend fun hasAnotherDevice(requestLogin: RequestLogin): ResponseLogin
}
