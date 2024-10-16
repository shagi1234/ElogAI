package com.selbiconsulting.elog.data.model.common

import androidx.annotation.StringRes

sealed class Resource<T>(
    val data: T? = null,
    val responseMessage: ResponseMessage? = null,
  val  statusCode:Int?=0
) {

    companion object{
        const val LOGOUT_CODE =428
        const val LOGIN_CODE =401
    }
    class Success<T>(data: T? = null) : Resource<T>(data = data)
    class Loading<T> : Resource<T>()
    class Error<T>(responseMessage: ResponseMessage?=null, statusCode:Int?=0) :
        Resource<T>(responseMessage = responseMessage, statusCode = statusCode)

    class Failure<T>(responseMessage: ResponseMessage) :
        Resource<T>(responseMessage = responseMessage)
}

data class ResponseMessage(
    val stringMessage: String? = null,
    @StringRes val intMessage: Int? = null,
)