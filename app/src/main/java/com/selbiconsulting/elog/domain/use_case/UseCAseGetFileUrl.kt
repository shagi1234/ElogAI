package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.request.RequestGetMessage
import com.selbiconsulting.elog.domain.repository.RepositoryMessage
import javax.inject.Inject

class UseCAseGetFileUrl @Inject constructor(
    private val repositoryMessage: RepositoryMessage
) {
    private val handleResponse: UseCaseHandleResponse<RequestGetMessage, ByteArray> =
        UseCaseHandleResponse()

    suspend fun execute(requestGetMessage: RequestGetMessage): Resource<ByteArray> {
        return handleResponse.handleResponse(requestGetMessage) {
            repositoryMessage.getFileUrl(it)
        }
    }
}