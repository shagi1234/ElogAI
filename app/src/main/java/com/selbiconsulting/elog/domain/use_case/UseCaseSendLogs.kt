package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.request.RequestSendLogs
import com.selbiconsulting.elog.data.model.response.ResponseSendLogs
import com.selbiconsulting.elog.domain.repository.RepositoryLogs
import javax.inject.Inject

class UseCaseSendLogs @Inject constructor(
    private val repositoryLogs: RepositoryLogs
) {
    private val handleResponse: UseCaseHandleResponse<List<RequestSendLogs>, List<ResponseSendLogs>> =
        UseCaseHandleResponse()

    suspend fun execute(requestSendLogs: List<RequestSendLogs>): Resource<List<ResponseSendLogs>> {
        return handleResponse.handleResponse(requestSendLogs) {
            repositoryLogs.sendLogsToServer(it)
        }
    }
}