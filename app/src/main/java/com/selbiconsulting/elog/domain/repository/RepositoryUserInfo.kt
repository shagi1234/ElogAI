package com.selbiconsulting.elog.domain.repository

import com.selbiconsulting.elog.data.model.response.ResponseLogin


/*
 * Created by shagi on 23.03.2024 23:26
 */

interface RepositoryUserInfo {
    fun saveToken(token: String)
    fun getToken(): String
    fun saveContactId(contactID: String?)
    fun saveUsername(username: String)
    fun savePassword(password: String)
}