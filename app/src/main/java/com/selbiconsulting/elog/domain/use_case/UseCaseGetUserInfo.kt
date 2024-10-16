package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.response.ResponseLogin
import com.selbiconsulting.elog.domain.repository.RepositoryUserInfo


/*
 * Created by shagi on 26.03.2024 23:05
 */

class UseCaseGetUserInfo(
    private val repositoryUserInfo: RepositoryUserInfo
) {

    fun getToken() {
        repositoryUserInfo.getToken()
    }
}