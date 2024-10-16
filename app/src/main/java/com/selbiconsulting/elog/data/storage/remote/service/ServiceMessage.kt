package com.selbiconsulting.elog.data.storage.remote.service

import com.selbiconsulting.elog.data.model.request.RequestGetMessage
import com.selbiconsulting.elog.data.model.request.RequestSendMessage
import com.selbiconsulting.elog.data.model.response.ResponseGetMessage
import com.selbiconsulting.elog.data.model.response.ResponseSendMessage
import com.selbiconsulting.elog.data.storage.remote.KtorClient

interface ServiceMessage {

    suspend fun sendMessage(responseSendMessage: RequestSendMessage): ResponseSendMessage
    suspend fun getMessage(requestGetMessage: RequestGetMessage, deviceId:String): ResponseGetMessage
    suspend fun getFileUrl(requestGetMessage: RequestGetMessage, deviceId:String): ByteArray
}
//00DHo000001mEI1!AQEAQNryGvCqwnWo0dY_x0yP44c7ZsfweiwixjFq8S53BpbamqvykDM8427ThAIEYnIOcLaEqRYK6WXmFVo0uTz7fE5x8XEu
//00DHo000001mEI1!AQEAQNryGvCqwnWo0dY_x0yP44c7ZsfweiwixjFq8S53BpbamqvykDM8427ThAIEYnIOcLaEqRYK6WXmFVo0uTz7fE5x8XEu