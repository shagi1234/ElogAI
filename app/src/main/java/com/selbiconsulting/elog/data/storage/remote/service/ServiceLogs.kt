package com.selbiconsulting.elog.data.storage.remote.service

import com.selbiconsulting.elog.data.model.dto.DtoLogs
import com.selbiconsulting.elog.data.model.request.RequestSendLogs
import com.selbiconsulting.elog.data.model.response.ResponseSendLogs
import com.selbiconsulting.elog.data.storage.remote.KtorClient
import com.selbiconsulting.elog.domain.use_case.RequestCertifyLogs
import com.selbiconsulting.elog.domain.use_case.ResponseCertifyLogs

interface ServiceLogs {


    suspend fun sendLogsToServer(requestSendLogs: List<RequestSendLogs>): List<ResponseSendLogs>
    suspend fun getLogsFromServer(contactId: String, deviceId: String): List<DtoLogs>
    suspend fun certifyLogsToServer(requestCertifyLogs: RequestCertifyLogs): ResponseCertifyLogs
}