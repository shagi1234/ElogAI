package com.selbiconsulting.elog.data.repository

import android.content.SharedPreferences
import com.selbiconsulting.elog.data.model.response.ResponseLogin
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.domain.repository.RepositoryUserInfo


/*
 * Created by shagi on 23.03.2024 23:28
 */

class RepositoryUserInfoImpl(val sharedPreferences: SharedPreferencesHelper) : RepositoryUserInfo {
    override fun saveToken(token: String) {
        sharedPreferences.token = token
    }

    override fun getToken(): String {
        return sharedPreferences.token ?: ""
    }

    override fun saveContactId(contactID: String?) {
        sharedPreferences.contactId = contactID
    }

    override fun saveUsername(username: String) {
        sharedPreferences.username = username
    }

    override fun savePassword(password: String) {
        sharedPreferences.password = password
    }

}