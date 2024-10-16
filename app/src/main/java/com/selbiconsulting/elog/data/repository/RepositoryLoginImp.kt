package com.selbiconsulting.elog.data.repository

import com.selbiconsulting.elog.data.model.request.RequestLogin
import com.selbiconsulting.elog.data.model.response.ResponseLogin
import com.selbiconsulting.elog.data.storage.remote.service.ServiceLogin
import com.selbiconsulting.elog.domain.repository.RepositoryLogin


/*
 * Created by shagi on 23.03.2024 22:41
 */

class RepositoryLoginImp(private val serviceLogin: ServiceLogin = ServiceLogin.create()) : RepositoryLogin {
    override suspend fun login(requestLogin: RequestLogin): ResponseLogin {
        return serviceLogin.login(requestLogin)
    }

    override suspend fun hasAnotherDevice(requestLogin: RequestLogin): ResponseLogin {
        return serviceLogin.hasAnotherDevice(requestLogin)
    }
}