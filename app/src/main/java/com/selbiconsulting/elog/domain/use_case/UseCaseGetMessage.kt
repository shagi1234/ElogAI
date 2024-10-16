package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.request.RequestGetMessage
import com.selbiconsulting.elog.data.model.response.ResponseGetMessage
import com.selbiconsulting.elog.domain.repository.RepositoryMessage
import javax.inject.Inject

class UseCaseGetMessage @Inject constructor(private val repositoryMessage: RepositoryMessage) {
    private val handleResponse: UseCaseHandleResponse<RequestGetMessage, ResponseGetMessage> =
        UseCaseHandleResponse()

    suspend fun execute(requestGetMessage: RequestGetMessage): Resource<ResponseGetMessage> {
        return handleResponse.handleResponse(requestGetMessage) {
            repositoryMessage.getMessagesFromServer(it)
        }
    }
}