package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.request.RequestLogin
import com.selbiconsulting.elog.data.model.response.ResponseLogin
import com.selbiconsulting.elog.data.repository.RepositoryUserInfoImpl
import com.selbiconsulting.elog.domain.repository.RepositoryUserInfo
import com.selbiconsulting.elog.ui.login.ActivityLogin
import javax.inject.Inject


/*
 * Created by shagi on 23.03.2024 23:25
 */

class UseCaseSaveUserInfo @Inject constructor(
    private val repositoryUserInfo: RepositoryUserInfo
) {
    fun saveUserInfo(responseLogin: ResponseLogin, requestLogin: RequestLogin) {
        if (responseLogin.accessToken.isNullOrEmpty()) return
        repositoryUserInfo.saveToken(responseLogin.accessToken)
        repositoryUserInfo.saveContactId(responseLogin.id)
        repositoryUserInfo.saveUsername(requestLogin.username)
        repositoryUserInfo.savePassword(requestLogin.password)
        //more
    }
}