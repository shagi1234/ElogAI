package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.dto.DtoLogs
import com.selbiconsulting.elog.domain.repository.RepositoryLogs
import javax.inject.Inject

class UseCaseGetLogs @Inject constructor(
    private val repositoryLogs: RepositoryLogs
) {
    private val handleResponse: UseCaseHandleResponse<String, List<DtoLogs>> =
        UseCaseHandleResponse()

    suspend fun execute(contactId: String): Resource<List<DtoLogs>> {
        return handleResponse.handleResponse(contactId) {
            repositoryLogs.getLogsFromServer(it)
        }
    }
}